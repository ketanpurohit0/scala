package com.kkp.Unt

import org.apache.spark.sql.{Column, DataFrame, SparkSession}
import org.scalatest.funsuite.AnyFunSuite
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.{BooleanType, IntegerType, LongType, StringType, StructField, StructType}
import sun.awt.image.ImagingLib

import scala.collection.mutable.ListBuffer

case class Foo(a:String)

class TestHelper extends  AnyFunSuite {

  val spark = Helper.getSparkSession("local[*]", "test")

  def makeLargeDf(spark: SparkSession, n: Int) : DataFrame = {
    val data : List[(Int, String, Int)] = List[(Int, String, Int)]((1, "Finance", 10), (2, "Marketing", 20), (3, "Sales", 30), (4, "IT", 40), (5, "CTS", 41), (6, "CTS", 42))
    var deptColumns = List("ID", "dept_name", "dept_id")
    import spark.implicits._
    val rdd = spark.sparkContext.parallelize(replicateList(data,n))
    val df = rdd.toDF(deptColumns:_*)
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

  test("largeDf") {
    val n = math.pow(2, 20).toInt
    spark.sparkContext.setCheckpointDir("checkpointing_folder")
    val df = makeLargeDf(spark, n)
    assert(df.count() == 6 * n)

    val rule_scale = math.pow(2, 6).toInt
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

  test("replicateList") {
    val list = replicateList(List(1,2,3), 3)
    assert(list == List(1,2,3,1,2,3,1,2,3))
  }

  test("parallelLogging") {

    def log[R](block: => R): R = {
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

    def loggingWithThreadIdAndCollection(s: Any)(implicit log: ListBuffer[String]): Unit = {
        log += s"[${Thread.currentThread().getId()}],$s"
    }

    case class Foo(a: Int, b: String, c: Double)


    class Activity(activityName: String) {
      implicit val logCollector = ListBuffer[String]()
      val randomNumberGenerator = new scala.util.Random(activityName.hashCode())
      val foo = Foo(0, "First", 0.0)
      def action(): ListBuffer[String] = {
        loggingWithThreadIdAndCollection(activityName, "First Hi")
        loggingWithThreadIdAndCollection(activityName, foo)
        Thread.sleep(randomNumberGenerator.nextInt(4) * 1000)
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
    val list = List("##1","##2","##3", "##4")
    val list2 = replicateList(list, 1).par
    // logging is intermingled
    list2.foreach(s => loggingWithPrintln(s))
    list2.foreach(s => loggingWithThreadId(s, "Hello", foo))
    list2.foreach(s => {
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
    })
  }
}
