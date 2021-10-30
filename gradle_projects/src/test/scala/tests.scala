import com.typesafe.scalalogging.Logger
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.{abs, asc, bround, col, concat, concat_ws, explode, lit, round, split, sum, to_date}
import org.apache.spark.sql.types.{DataType, DoubleType, IntegerType, StringType}
import org.scalatest.funsuite.AnyFunSuite
import org.slf4j.LoggerFactory

class tests extends AnyFunSuite {
  val sparkSession: SparkSession = SparkHelper.getSparkSession("local","")

  def secondTimer[R] (text :String, block:R): R = {
    val t0 = System.nanoTime()
    val result = block
    val t1 = System.nanoTime()
    println(s"ELAPSED TIME $text (sec): " + (t1-t0)/10E9)
    result

  }


  test("foo") {
    val df = SparkHelper.getQueryDf(sparkSession, "SELECT * FROM foo_left", "org.postgresql.Driver", "postgres","foobar_secret","jdbc:postgresql://localhost/postgres?user=postgres&password=foobar_secret")
    df.show()
    assert(df.columns.contains("name_left"))

  }

  test("comma") {
    val s = "1,2,105,221,1031"
    val maxval = s.split(",").map(_.toInt).max
    assert(maxval == 1031)
  }

  test("parallelLoop") {
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
      secondTimer(s"Parallel Test $poolSize", scenarios.foreach(scenario => {
        val s=new scenario_handler(scenario)
        s.handler()
      }))
    }


  }

  class scenario_handler(scenario: String) {
    val m_scenario: String = scenario
    val logger: Logger = Logger(LoggerFactory.getLogger(this.getClass))

    def handler() : Unit = {
      println(s"Process $m_scenario, ${Thread.currentThread().getId}, ${Thread.currentThread().getName}")
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
      seed
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
    val scenarios_in_par = foo.par
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
                      .toDF(s"ROLLUP_$rollup_level","COMPOSITION_FIELDS")
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
      val r = (math.random() * 1000000).toLong
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
    import org.apache.spark.sql.expressions.Window
    import org.apache.spark.sql.{functions => F}
    val data = Seq[(String, Int, Long)](("ENGLAND",1, 1000),("ENGLAND", 2, 400), ("ENGLAND", 3, 600), ("SCOTLAND",1, 100), ("SCOTLAND", 2, 100), ("SCOTLAND", 3, 211), ("SCOTLAND", 4, 1000))
    import sparkSession.implicits._
    var df = data.toDF("COUNTRY", "INDEX", "GDP")
    val w = Window.orderBy("COUNTRY","INDEX").partitionBy("COUNTRY")

    val stateDelta = F.udf( (lag0 : Long, lag1: Long) => {lag0 - lag1})
    df = df.withColumn("DELTA", stateDelta(F.lag("GDP", 0).over(w), F.lag("GDP", 1).over(w)))
    df.show(100, truncate = false)

    val w1 = w.rangeBetween(Window.unboundedPreceding, 0)

    df = df.withColumn("RUNNING_DELTA", F.sum("DELTA").over(w1))
    df.show(100, truncate = false)

  }

  test("rollUpViaJoins") {
    val data = Seq[(String, Double)](("F1", 100.0), ("F2", 200.00), ("F3", 300.0),("F4",1000.0))
    val rollupMetaData = Seq[(String, Double, String)](("F1", 10.0, "F4"),("F2", 10, "F4"),("F3", 10, "F4"),("F1", 1, "F5"),("F3", -2.0/3, "F5"),("M",1,"F5"))
    import sparkSession.implicits._
    var df = data.toDF("FIELD_NAME", "FIELD_VALUE")
    val rollupMetaDataDf = rollupMetaData.toDF("SOURCE_FIELD", "WEIGHT", "TARGET_FIELD")

    df.show()
    rollupMetaDataDf.show()

    val rolledUpDf = rollupMetaDataDf.join(df, rollupMetaDataDf.col("SOURCE_FIELD") === df.col("FIELD_NAME"))
                          .withColumn("TARGET_VALUE", col("WEIGHT")*col("FIELD_VALUE"))
                          .groupBy(col("TARGET_FIELD").as("FIELD_NAME")).agg(sum("TARGET_VALUE").as("FIELD_VALUE"))

    val resultDf = df.union(rolledUpDf).groupBy("FIELD_NAME").agg(sum("FIELD_VALUE").as("FIELD_VALUE"))

    resultDf.show()

  }

  test("cloneAndChangeCol") {

    val rules = Seq[(String, Int, Int)](("O",100,101),("P",200,201),("Q",300,301))
    val data = Seq[(String, Int)](("O",100),("O",100),("P",200),("P",220),("Q",300),("Q",330),("S",0),("T",0),("U",0))
    import sparkSession.implicits._
    val rulesDf = rules.toDF("TAG", "SRC_VAL","TARGET_VAL")
    val dataDf = data.toDF("TAG", "SRC_VAL")

    rulesDf.show()
    dataDf.show()
    dataDf.printSchema()

    val origDataType = dataDf.schema("SRC_VAL").dataType
    val result = dataDf.alias("dataDf").join(rulesDf.alias("rulesDf"), Seq("TAG","SRC_VAL"), "left_outer").
      select("dataDf.*", "rulesDf.TARGET_VAL").
      withColumn("SRC_VAL",dataDf.col("SRC_VAL").cast("string")).
      withColumn("SRC_VAL", concat_ws(",", col("SRC_VAL"), col("TARGET_VAL").cast("string"))).
      drop("TARGET_VAL").
      withColumn("SRC_VAL", explode(split(col("SRC_VAL"), ","))).
      withColumn("SRC_VAL", col("SRC_VAL").cast(origDataType))

    result.show()
    result.printSchema()
  }

  test("rollUpViaJoinsMultiLevel") {
    
    // FIELD_NAME, FIELD_VALUE
    val data = Seq[(String, Double)](("F1", 100.0), ("F2", 200.00), ("F3", 300.0))
    
    // LEVEL, Seq(SOURCE_FIELD, WEIGHT, TARGET_FIELD)
    // note: A LEVEL can be broken across several lines for same results
    // note: This is a PoC - the following data could come from a static table source
    // note: LEVEL represents some hierarchical level, where fields are generated at a lower hierarchy
    // then contribute to higher levels
    val rollupMetaData = Seq[(Int, Seq[(String, Double, String)])](
      (1,     Seq(("F1", 1.0, "F4"))),
      (1,     Seq(("F2", 1, "F4"))),
      (1,     Seq(("F3", 1, "F4"))),
      (1,     Seq(("F1", 1, "F5"))),
      (1,     Seq(("F3", -2.0/3, "F5"))),
      (1,     Seq(("M",1,"F5"))),

      (2,     Seq(("F4", 1, "F6"),("F5", -1, "F6"))),
      (3,     Seq(("F6", -3.1415926535, "PI"))),
      (3,     Seq(("F5", -3.1415926535, "PI2")))
    )
    import sparkSession.implicits._

    // FIELD_NAME, FIELD_VALUE
    var df = data.toDF("FIELD_NAME", "FIELD_VALUE")
    
    // LEVEL, SOURCE_FIELD, WEIGHT, TARGET_FIELD
    val rollupMetaDataDf = rollupMetaData.toDF("LEVEL", "SEQ")
      .withColumn("SEQ", explode(col("SEQ")))
      .select("LEVEL","SEQ.*")
      .toDF("LEVEL", "SOURCE_FIELD", "WEIGHT", "TARGET_FIELD")

    // for example (example if sourcing from database)
    //    rollupMetaDataDf.printSchema()
    //    root
    //    |-- LEVEL: integer (nullable = false)
    //    |-- SOURCE_FIELD: string (nullable = true)
    //    |-- WEIGHT: double (nullable = true)
    //    |-- TARGET_FIELD: string (nullable = true)
    //
    //    rollupMetaDataDf.show()
    //    +-----+------------+-------------------+------------+
    //    |LEVEL|SOURCE_FIELD|             WEIGHT|TARGET_FIELD|
    //    +-----+------------+-------------------+------------+
    //    |    1|          F1|                1.0|          F4|
    //    |    1|          F2|                1.0|          F4|
    //    |    1|          F3|                1.0|          F4|
    //    |    1|          F1|                1.0|          F5|
    //    |    1|          F3|-0.6666666666666666|          F5|
    //    |    1|           M|                1.0|          F5|
    //    |    2|          F4|                1.0|          F6|
    //    |    2|          F5|               -1.0|          F6|
    //    |    3|          F6|      -3.1415926535|          PI|
    //    |    3|          F5|      -3.1415926535|         PI2|
    //    +-----+------------+-------------------+------------+
    val distinctRollupsByLevel = rollupMetaDataDf.select("LEVEL").distinct().orderBy(asc("LEVEL"))

    val levels  = distinctRollupsByLevel.as[Int].collect()
    
    val rollups = levels.foldLeft(df){(accumulateDf, level) =>
      accumulateDf
        .union(rollupMetaDataDf.filter(col("LEVEL") === level).join(accumulateDf, rollupMetaDataDf.col("SOURCE_FIELD") === accumulateDf.col("FIELD_NAME"))
          .withColumn("TARGET_VALUE", col("WEIGHT")*col("FIELD_VALUE"))
          .groupBy(col("TARGET_FIELD").as("FIELD_NAME")).agg(sum("TARGET_VALUE").as("FIELD_VALUE")))
    }


    df.show()
    rollups.printSchema()
    rollups.show()
  }

  test("pivotStyle") {

    // Here we have a pretend SEC5 file for MAS_FORM2 which are presently loaded as adjustmnets
    // in world of MARIO it may be different. But the code for doing so can be adapted for
    // different column names etc

    // load the data - [will not be relevant one replaced with actual source]
    val df = sparkSession.read.format("csv")
              .option("header", "true")
              .option("inferSchema", "true")
              .load("file:/MyWork/GIT/scala/gradle_projects/src/test/scala/sec5.csv")

    // name of enumerator field indicating its position in template
    val nameOfEnumeratorColumn = "ENUMERATOR"
    // 1:scaling factor required - I think no scale factor is required, hence 1
    val SCALE_FACTOR = 1

    // convert to suit purpose [may or may not be required once correct source available]
    val df2 = df
              .withColumn(nameOfEnumeratorColumn, col(nameOfEnumeratorColumn).cast("int"))
              .withColumn("ASOF_DATE", to_date(col("ASOF_DATE"),"dd/MM/yyyy HH:mm:ss"))
              .withColumn("AMOUNT_SGD", col("AMOUNT_SGD").cast(DoubleType))
              .withColumn("RECVALUE", col("AMOUNT_SGD")*(lit(1.0)-col("MAS_FORM5_HAIRCUT").cast(DoubleType)))
              .withColumn("MAS_FORM5_HAIRCUT", bround(lit(100)*col("MAS_FORM5_HAIRCUT").cast(DoubleType),1))
              //also round if required (here i assume rounding is required, but scaling down by 1000 is not required
              .withColumn("AMOUNT_SGD", abs(round(col("AMOUNT_SGD")/SCALE_FACTOR)).cast(IntegerType))
              .withColumn("RECVALUE", abs(round(col("RECVALUE")/SCALE_FACTOR)).cast(IntegerType))

    //    Now map field names like so
      //    MAS_FORM5_ASSET_TYPE -> OS1_F2S5_UNENCUMASSET_TYPE<ENUMERATORCOL>
      //    MAS_FORM5_PLATFORM -> OS1_F2S5_UNENCUMASSET_PLAT<ENUMERATORCOL>
      //    MAS_FORM5_LOCATION -> OS1_F2S5_UNENCUMASSET_LOC<ENUMERATORCOL>
      //    AMOUNT_SGD -> ON0_F2S5_UNENCUMASSET_AMT<ENUMERATORCOL>
      //    MAS_FORM5_HAIRCUT -> ON1_F2S5_UNENCUMASSET_HAIRCUT<ENUMERATORCOL>
      //    AMOUNT_SGD*(1 - MAS_FORM5_HAIRCUT) -> ON0_F2S5_UNENCUMASSET_RECVALUE<ENUMERATORCOL>


    // name of column requiring mapping
    val mapToFieldNameValues = Map("MAS_FORM5_ASSET_TYPE" -> "OS1_F2S5_UNENCUMASSET_TYPE",
                                "MAS_FORM5_PLATFORM" -> "OS1_F2S5_UNENCUMASSET_PLAT",
                                "MAS_FORM5_LOCATION" -> "OS1_F2S5_UNENCUMASSET_LOC",
                                "AMOUNT_SGD" -> "ON0_F2S5_UNENCUMASSET_AMT",
                                "MAS_FORM5_HAIRCUT" -> "ON1_F2S5_UNENCUMASSET_HAIRCUT",
                                "RECVALUE" -> "ON0_F2S5_UNENCUMASSET_RECVALUE"
                              )

    val columnsToCreateFieldNameColFor = mapToFieldNameValues.keys

    val modifiedDf2 = columnsToCreateFieldNameColFor.foldLeft(df2) {
      (df, c) => df.withColumn(s"${c}_FN", concat(Seq(lit(mapToFieldNameValues(c)), col(nameOfEnumeratorColumn)):_*))
    }


    val modifiedDf3 = columnsToCreateFieldNameColFor
                      .map( c => {modifiedDf2.select(col(s"${c}_FN").as("FIELD_NAME"), col(c).as("FIELD_VALUE"))})
                      .reduce(_ union _)

    modifiedDf3.show(false)
  }


}
