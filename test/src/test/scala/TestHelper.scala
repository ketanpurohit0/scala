package com.kkp.Unt

import com.typesafe.config.ConfigFactory
import org.apache.spark.sql.{Column, DataFrame, SparkSession}
import org.scalatest.funsuite.AnyFunSuite
import org.apache.spark.sql.functions._

import scala.collection.mutable.ListBuffer

case class Foo(a:String)
case class Data(id: Int, dept_name: String, dept_id: Int)
case class Rule(scenario: String, ruleOrder: Int, ruleText: String)

class TestHelper extends  AnyFunSuite {

  val spark = Helper.getSparkSession("local[*]", "test")

  def loggingWithThreadIdAndCollection(s: Any)(implicit log: ListBuffer[String]): Unit = {
    log += s"[${Thread.currentThread().getId()}],$s"
  }

  def printLog(log: ListBuffer[String]) : Unit = {
    log.map(l => println(l))
  }


  def makeLargeDf(spark: SparkSession, n: Int) : DataFrame = {
    val data : List[(Int, String, Int)] = List[(Int, String, Int)]((1, "Finance", 10), (2, "Marketing", 20), (3, "Sales", 30), (4, "IT", 40), (5, "CTS", 41), (6, "CTS", 42))
    var deptColumns = List("ID", "dept_name", "dept_id")
    import spark.implicits._
    val rdd = spark.sparkContext.parallelize(replicateList(data,n))
    val df = rdd.toDF(deptColumns:_*)
    df
  }

  def makeRuleDf(spark: SparkSession, n: Int) : DataFrame = {
    val ruleColumns = List("Scenario", "RuleOrder", "RuleText")
    val rules : List[(String, Int, String)] = List(("Adj#1",1, "SomeRuleDefn1"), ("Adj#2",1,"SomeRuleInAdj#2"), ("Adj#2",2,"SomeOtherRuleInAdj#2"))
    import spark.implicits._
    val rdd = spark.sparkContext.parallelize(replicateList(rules, n))
    val df = rdd.toDF(ruleColumns:_*)
    df
  }

  def modificationList(): List[(String, String, String)] = {
    return List[(String, String, String)](
    ("dept_name", "Marketing2.0", "dept_name = 'Marketing'"),
    ("dept_name", "Marketing2.0", "dept_name = 'XMarketing'"),
    ("dept_name", "Marketing2.0", "dept_name = 'XMarketing'"),
    ("dept_name", "xMarketing2.0", "dept_name = 'XMarketing'"),
    ("dept_name", "Marketing2.0", "dept_name = 'Marketing'")
    )
  }

  def replicateList[A](list: List[A], n: Int): List[A] = {
    val b = 1 to n
    b.map(x => list).flatMap(y => y).toList
  }

  test("BigTest") {
    assert(1==1)
  }

  test("SparkHelperTest") {
    val spark = Helper.getSparkSession("local", "test")
    import spark.implicits._
    val seqFoo = Seq[Foo](Foo("Foo"),Foo("Bar") )
    //val df = seqFoo.toDF("col")
    //df.show()
    val df1 = seqFoo.toDF()
    val df2 = seqFoo.toDS()
    val df3 = df1.alias("df1").join(df2.alias("df2"), expr("df1.a = df2.a"))
    assert(df1.count == seqFoo.length)
    assert(df2.count()== seqFoo.length)
    assert(df3.count() == seqFoo.length)
  }

  test("postGreJdbc") {
    val url = Helper.getPostGreUrl("postgres","postgres", "foobar_secret")
    val conn = Helper.getJdbc("postgres", "postgres","foobar_secret")
    val rs = Helper.select(conn, "select * from foo")
    rs.last()
    assert(rs.getRow() > 0)
  }

    test("postGreJdbcFromConfig") {
    val conn = Helper.getJdbcFromConfig()
    val rs = Helper.select(conn, "select * from foo")
    rs.last()
    assert(rs.getRow() > 0)
  }

  test("config") {
    import com.typesafe.config._
    val config = ConfigFactory.load()
    val driver = config.getString("jdbc.driver")
    val url = config.getString("jdbc.url")
    val username = config.getString("jdbc.username")
    val password = config.getString("jdbc.password")

    println(s"driver =   $driver")
    println(s"url =      $url")
    println(s"username = $username")
    println(s"password = $password")
  }

  test("largeDfWriteToDB") {
    val n = math.pow(2, 20).toInt
    val df = makeLargeDf(spark, n)
    val config = ConfigFactory.load()
    val driver = config.getString("jdbc.driver")
    val url = config.getString("jdbc.url")
    val username = config.getString("jdbc.username")
    val password = config.getString("jdbc.password")

    df.select("dept_id", "ID", "dept_name").write.mode("append")
      .format("jdbc")
      .option("url", url)
      .option("dbtable", "TARGET_FOR_SPARK_DF")
      .option("user", username)
      .option("password", password)
      .save()
  }

  test("largeDfAdjustment") {
    val n = math.pow(2, 8).toInt
    spark.sparkContext.setCheckpointDir("checkpointing_folder")
    val df = makeLargeDf(spark, n)
    assert(df.count() == 6 * n)

    val rule_scale = math.pow(2, 8).toInt
    val rules = replicateList(modificationList(), rule_scale)
    import org.apache.spark.sql.functions._

    val flag_col = "isModified"
    val partition_cols = List[Column](col("dept_name"), col("dept_id"))
    val check_point_interval = 5

    var df_base = df.withColumn(flag_col, lit(false)).repartition(8, partition_cols:_*).cache()
    println(df_base.rdd.getNumPartitions)

    var check_point_stale: Boolean = true

    for ((mod, index) <- rules.zipWithIndex) {
      // println(index, mod)
      df_base = df_base.withColumn(flag_col, when(expr(mod._3), lit(true)).otherwise(col(flag_col)))
      .withColumn(mod._1, when(expr(mod._3), lit(mod._2)).otherwise(col(mod._1)))
      check_point_stale = true
      if (index % check_point_interval == 0) {
        // checkpointing creates a set of files in the checkpoint folder , see spark.sparkContext.setCheckpointDir
        // these need to be removed
        df_base = df_base.checkpoint(true)
        check_point_stale = false
      }
    }

    if (check_point_stale) {
      df_base = df_base.checkpoint(true)
    }

    df_base = df_base.filter(s"$flag_col = true").drop(flag_col).cache()
    val rows_affected = df_base.count()
    println(s"Rows affected: $rows_affected")

  }

  test("largeDfAdjustmentFunctional") {
    val n = math.pow(2, 8).toInt
    spark.sparkContext.setCheckpointDir("checkpointing_folder")
    val df = makeLargeDf(spark, n)
    assert(df.count() == 6 * n)

    val rule_scale = math.pow(2, 8).toInt
    val rules = replicateList(modificationList(), rule_scale)
    import org.apache.spark.sql.functions._

    val flag_col = "isModified"
    val partition_cols = List[Column](col("dept_name"), col("dept_id"))
    val check_point_interval = 5

    var check_point_stale: Boolean = true

    val df_result = rules.zipWithIndex.foldLeft[DataFrame](df.withColumn(flag_col, lit(false)).repartition(8, partition_cols:_*).cache())((df_base, r) => {
      val index = r._2
      val mod = r._1
      if (index % check_point_interval == 0) {
        check_point_stale = false
        df_base.withColumn(flag_col, when(expr(mod._3), lit(true)).otherwise(col(flag_col)))
          .withColumn(mod._1, when(expr(mod._3), lit(mod._2)).otherwise(col(mod._1))).checkpoint(true)
      }
      else {
        check_point_stale = true
        df_base.withColumn(flag_col, when(expr(mod._3), lit(true)).otherwise(col(flag_col)))
          .withColumn(mod._1, when(expr(mod._3), lit(mod._2)).otherwise(col(mod._1)))
      }
    })


    val df_final =
      if (check_point_stale)
        df_result.filter(s"$flag_col = true").drop(flag_col).checkpoint(true)
      else
        df_result.filter(s"$flag_col = true").drop(flag_col)

    val rows_affected = df_final.count()
    println(s"Rows affected: $rows_affected")
  }

  test("replicateList") {
    val list = replicateList(List(1,2,3), 3)
    assert(list == List(1,2,3,1,2,3,1,2,3))
  }

  test("parallelLogging") {

    def timer[R](block: => R): R = {
      val t0 = System.nanoTime()
      val result = block    // call-by-name
      val t1 = System.nanoTime()
      println("Elapsed time: " + (t1 - t0) + "ns")
      result
    }

    def loggingWithPrintln(s:String) : Unit = {
      println(s)
    }

    def loggingWithThreadId(s: Any) : Unit = {
      println(s"[${Thread.currentThread().getId()}],$s" )
    }


    case class Foo(a: Int, b: String, c: Double)


    class Activity(activityName: String) {
      implicit val logCollector = ListBuffer[String]()
      val randomNumberGenerator = new scala.util.Random()
      val foo = Foo(0, "First", 0.0)
      def action(): ListBuffer[String] = {
          loggingWithThreadIdAndCollection(activityName, "First Hi")
          loggingWithThreadIdAndCollection(activityName, foo)
          val sleepInterval = randomNumberGenerator.nextInt(16) * 1000
          loggingWithThreadIdAndCollection(activityName, "Sleep Interval:", sleepInterval)
          Thread.sleep(sleepInterval)
          otherAction()
          loggingWithThreadIdAndCollection(activityName, "Lastly Foo")
          val lastFoo = Foo(2, "Last", 2.0)
          loggingWithThreadIdAndCollection(activityName, lastFoo)
          logCollector
      }

      def otherAction(): Unit = {
        val foo = Foo(1, "Middle", 1.0)
        loggingWithThreadIdAndCollection(activityName, foo)
        loggingWithThreadIdAndCollection(activityName, "Middle otherAction")
      }
    }

    val foo = Foo(58, "FooBar", 55.5)
    val list = List("##1","##2","##3", "##4", "##5", "##6", "##7")
    val list2 = replicateList(list, 1).par
    // logging is intermingled
    //list2.foreach(s => loggingWithPrintln(s))
    //list2.foreach(s => loggingWithThreadId(s, "Hello", foo))
    list2.foreach(s => timer({
      // start a worker
      val activity = new Activity(s)
      // do some activity
      val logCollector = activity.action()
      // output log in synchronized format
      this.synchronized {
        println("START LOG FOR ---", s)
        logCollector.map(x => println(x))
        println("END LOG FOR -----", s)
      }
    }))
  }

  test("dfToCaseClassFunctionalProcessing") {
    val df = makeLargeDf(spark, 3)
    import spark.implicits._
    df.as[Data].collect().foreach(x => {
      println(x)
    })

    val r = df.as[Data].collect().groupBy(_.dept_id).map( group =>
    {
      val id = group._1
      val data = group._2.sortBy(s => s.dept_id)
      println(id, data.length)
      data.foreach(d => println(d.id, d.dept_id))
      val messages = List("A", "B", "C")
      messages
    })

    r.foreach(m => m.map(println))

  }

  test("dfToCaseClassForRulesFunctionalProcessing") {
    val df = makeRuleDf(spark, 1)
    import spark.implicits._

    val results = df.as[Rule].collect().sortBy(r => (r.scenario, r.ruleOrder)).groupBy(r => r.scenario).par.map( group =>
      {
        println(s"${Thread.currentThread().getId}-->")
        implicit val logCollector = ListBuffer[String]()
        val scenario = group._1
        val rules = group._2
        loggingWithThreadIdAndCollection(s"scenario: $scenario")
        rules.map(r => loggingWithThreadIdAndCollection(s"\t ${r.ruleOrder} -> ${r.ruleText}"))
        val booleanResult = System.nanoTime() % 2 == 0
        println(s"<--${Thread.currentThread().getId}")
        (scenario, booleanResult, logCollector)
      }
    )

    results.foreach(result => {
      println(s"scenario: ${result._1}, status: ${result._2}")
      printLog(result._3)
    }
    )
  }


}
