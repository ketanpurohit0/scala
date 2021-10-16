import com.typesafe.scalalogging.Logger
import org.apache.spark.sql.functions.{col, explode, lit, split, sum, when}
import org.scalatest.funsuite.AnyFunSuite
import org.slf4j.LoggerFactory

class tests extends AnyFunSuite {
  val sparkSession = SparkHelper.getSparkSession("local","")

  def secondTimer[R] (text :String, block:R): R = {
    val t0 = System.nanoTime()
    val result = block
    val t1 = System.nanoTime()
    println(s"ELAPSED TIME ${text} (sec): " + (t1-t0)/10E9)
    return result

  }


  test("foo") {
    val df = SparkHelper.getQueryDf(sparkSession, "SELECT * FROM foo_left", "org.postgresql.Driver", "postgres","foobar_secret","jdbc:postgresql://localhost/postgres?user=postgres&password=foobar_secret")
    df.show()
    assert(df.columns.contains("name_left"))

  }

  test("comma") {
    val s = "1,2,105,221,1031"
    val maxval = s.split(",").map(_.toInt).max
    assert(maxval.toInt == 1031)
  }

  test("parallelLoop") {
    val loopSize : Int = 10
    import scala.collection.parallel.ForkJoinTaskSupport
    val poolSizes = Seq[Int](1,2,4,6,8)
    /*
        val pcol = ((1 to loopSize)).par
        val calc = new calculator()

        secondTimer("test0",(1 to loopSize).foreach (x => {calc.calc(x)}))
        println(calc.result())

        secondTimer("test1",pcol.foreach (x => {calc.calc(x)}))
        println(calc.result())


        val calc2 = new calculator()

        for (poolSize <- poolSizes) {
            pcol.tasksupport = new ForkJoinTaskSupport(new scala.concurrent.forkjoin.ForkJoinPool(poolSize))
            secondTimer(s"Parallel Test ${poolSize}", pcol.foreach(x => {calc2.calc(x)}))
        }
        println(calc2.result())
    */
    val scenarios = Seq("A","B","C","D","E","F").par
    for (poolSize <- poolSizes) {
      scenarios.tasksupport = new ForkJoinTaskSupport(new scala.concurrent.forkjoin.ForkJoinPool(poolSize))
      secondTimer(s"Parallel Test ${poolSize}", scenarios.foreach(scenario => {
        val s=new scenario_handler(scenario)
        s.handler()
      }))
    }


  }

  class scenario_handler(scenario: String) {
    val m_scenario = scenario
    val logger = Logger(LoggerFactory.getLogger(this.getClass))

    def handler() : Unit = {
      println(s"Process ${m_scenario}, ${Thread.currentThread().getId}, ${Thread.currentThread().getName}")
      //logger.info("ff")
      println(logger.underlying.isDebugEnabled)
      println(logger.underlying.isWarnEnabled())
      println(logger.underlying.isInfoEnabled)
      println(logger.underlying.isTraceEnabled())
      println(logger.underlying.isErrorEnabled())

    }
  }

  class calculator() {
    var seed: BigInt = 0

    def calc(input: Int) : Unit = {
      println(input)
      seed+=input
    }

    def result() : BigInt = {
      return seed
    }
  }

  test("logging") {
    val logger = Logger("foo")
    logger.debug("foo")
    logger.error("error")
  }

  test("Seq") {
    val scenarios = Seq[String]("GENERIC", "A", "B","C")
    val process = scenarios.filter(_ != "GENERIC")
    val foo = process.map(p => Seq[String]("GENERIC",p))
    var scenarios_in_par = foo.par
    import scala.collection.parallel.ForkJoinTaskSupport
    scenarios_in_par.tasksupport = new ForkJoinTaskSupport(new scala.concurrent.forkjoin.ForkJoinPool(3))
    scenarios_in_par.foreach(pseq => println(Thread.currentThread().getId,pseq))
  }

  test("InternaliseSeq") {
    val scenarios = Seq[(String,Long)] (("t1",1),("t2",2),("t1",3),("t2",4))
    import sparkSession.implicits._
    val df = scenarios.toDF("TABLE","RULE_ORDER").orderBy("RULE_ORDER")
    val df2 = df.orderBy("TABLE","RULE_ORDER")
    df.show(false)
    df2.show(false)
  }

  test("rollUp") {
    val data = Seq[(String, Long)](("ENGLAND",1000),("WALES", 400), ("SCOTLAND", 100), ("IRELAND", 600))
    val rollup = Seq[(String, String)](("MAINLAND","ENGLAND;WALES;SCOTLAND"),("UK","IRELAND;MAINLAND"))

    import sparkSession.implicits._

    var dataDf = data.toDF("COUNTRY","GDP")
    var rollupDf = rollup.toDF("TARGET_FIELD","COMPOSITION_FIELDS")

    dataDf.show()
    rollupDf.show()

    // split the ; delimited column
    rollupDf = rollupDf.withColumn("COMPOSITION_FIELDS_SPLIT", split(col("COMPOSITION_FIELDS"), ";"))
    rollupDf.show()

    //
    rollupDf = rollupDf.withColumn("COMPOSITION_FIELDS_SPLIT", explode(col("COMPOSITION_FIELDS_SPLIT")))
    rollupDf.show()

    //
    dataDf = dataDf.join(rollupDf, col("COUNTRY") === col("COMPOSITION_FIELDS_SPLIT"), "left")

    // aggregate
    dataDf.groupBy(col("COUNTRY").as("TARGET_FIELD")).sum("GDP").as("GDP").show()

    dataDf.groupBy(col("TARGET_FIELD")).sum("GDP").as("GDP").show()
  }

  test("rollUp2") {
    val data = Seq[(String, Long)](("ENGLAND",1000),("WALES", 400), ("SCOTLAND", 100), ("IRELAND", 600))
    val rollup = Seq[(String, String, String)](("COUNTRY","MAINLAND","ENGLAND;WALES;SCOTLAND"),("LEVEL1","UK","IRELAND;MAINLAND"))

    import sparkSession.implicits._


    var dataDf = data.toDF("COUNTRY","GDP")

    rollup.foreach(
      r => {
        val rollup_level = r._1
        val target_field = r._2
        val composition_fields = r._3
        val seq = Seq[(String, String)]((target_field, composition_fields))
        val rollupDf = seq
                      .toDF(s"ROLLUP_${rollup_level}","COMPOSITION_FIELDS")
                      .withColumn("COMPOSITION_FIELDS_SPLIT", split(col("COMPOSITION_FIELDS"), ";"))
                      .withColumn("COMPOSITION_FIELDS_SPLIT", explode(col("COMPOSITION_FIELDS_SPLIT")))

        dataDf = dataDf.join(rollupDf, col(rollup_level) === col("COMPOSITION_FIELDS_SPLIT"), "left")
        .drop(Seq("COMPOSITION_FIELDS", "COMPOSITION_FIELDS_SPLIT"):_*)
        dataDf.show()
      }
    )


  }

  test("rollUp3") {
    val data = Seq[(String, Long)](("ENGLAND",1000),("WALES", 400), ("SCOTLAND", 100), ("IRELAND", 600), ("FRANCE", 211))
    val rollup = Seq[(String, String)](("ENGLAND","MAINLAND;UK"),
                                       ("SCOTLAND","MAINLAND;UK"),
                                        ("WALES","MAINLAND;UK"),
                                        ("IRELAND","UK;EU"),
                                        ("FRANCE", "EU")
    )

    import sparkSession.implicits._

    var dataDf = data.toDF("COUNTRY","GDP")
    var rollupDf = rollup.toDF("TARGET_FIELD","COMPOSITION_FIELDS")

    dataDf.show()
    rollupDf.show()

    // split the ; delimited column
    rollupDf = rollupDf.withColumn("COMPOSITION_FIELDS_SPLIT", split(col("COMPOSITION_FIELDS"), ";"))
    rollupDf.show()

    // build the rollup
    rollupDf = rollupDf.withColumn("COMPOSITION_FIELDS_SPLIT", explode(col("COMPOSITION_FIELDS_SPLIT")))
    rollupDf.show()

    //

    dataDf.show()

    // aggregate - base
    dataDf.groupBy(col("COUNTRY").as("TARGET_FIELD")).agg(sum("GDP").as("GDP")).show()

    // rollup
    dataDf = dataDf.join(rollupDf, col("COUNTRY") === col("TARGET_FIELD"), "left")

    dataDf.show()

    // aggregate
    dataDf.groupBy(col("COMPOSITION_FIELDS_SPLIT").as("TARGET_FIELD")).agg(sum("GDP").as("GDP")).show()

  }

  test("round") {
    (1 to 100).foreach(n => {
      var r = (math.random()*1000000).toLong
      val r1  = (r.toDouble * n).toLong/100
      val r2 =  (r.toDouble * n)/100
      val r3 = r2.round  //setScale(2, BigDecimal.RoundingMode.HALF_UP).toLong
      println(r, n, r1, r2, r3, r2 - r1, r3 - r1)
    })
  }

  test("sparkVersion") {
    println(s"Spark version: ${sparkSession.sparkContext.version}")
  }

  test("stateChange") {
    import org.apache.spark.sql.{functions => F}
    import org.apache.spark.sql.expressions.Window
    val data = Seq[(String, Int, Long)](("ENGLAND",1, 1000),("ENGLAND", 2, 400), ("ENGLAND", 3, 600), ("SCOTLAND",1, 100), ("SCOTLAND", 2, 100), ("SCOTLAND", 3, 211), ("SCOTLAND", 4, 1000))
    import sparkSession.implicits._
    var df = data.toDF("COUNTRY", "INDEX", "GDP")
    val w = Window.orderBy("COUNTRY","INDEX").partitionBy("COUNTRY")

    val stateDelta = F.udf( (lag0 : Long, lag1: Long) => {lag0 - lag1})
    df = df.withColumn("DELTA", stateDelta(F.lag("GDP", 0).over(w), F.lag("GDP", 1).over(w)))
    df.show(100, false)

    val w1 = w.rangeBetween(Window.unboundedPreceding, 0)

    df = df.withColumn("RUNNING_DELTA", F.sum("DELTA").over(w1))
    df.show(100, false)

  }

}
