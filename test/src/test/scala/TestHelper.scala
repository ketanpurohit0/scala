package com.kkp.Unt

//import com.kkp.Unt.Helper
import com.kkp.Unt.Helper.unionWithDefault
import com.typesafe.config.ConfigFactory
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.{Column, DataFrame, Encoders, SparkSession}
import org.scalatest.funsuite.AnyFunSuite
import org.apache.spark.sql.functions._

import java.util.Properties
import scala.collection.mutable.ListBuffer

case class Foo(a:String)
case class Data(id: Int, dept_name: String, dept_id: Int)
case class Rule(scenario: String, targetTable: String, ruleOrder: Int, ruleType: String, ruleText: String)
object RuleTypeEnumeration extends Enumeration {
  type RuleTypeEnumeration = Value
  val r0 = Value(0, "R_common_read")
  val first = Value(1, "D")
  val second = Value(2, "DU")
  val third = Value(3, "I")
  val fourth = Value(4, "S")
}

class TestHelper extends  AnyFunSuite {

  val spark = Helper.getSparkSession("local[*]", "test")

  // we treat different reads as a common read type
  // so for that purpose we map actual value to its common value that does exist in the rule type enumerator
  implicit val mapOfReads = Map[String,String]("R_anyread_1" -> "R_common_read", "R_anyread_2"-> "R_common_read", "R_anyread_3"->"R_common_read")


  def loggingWithThreadIdAndCollection(s: Any)(implicit log: ListBuffer[String]): Unit = {
    log += s"[${Thread.currentThread().getId()}],$s"
  }

  def printLog(log: ListBuffer[String]) : Unit = {
    log.foreach(l => println(l))
  }

  def timer[R](block: => R) :(R, Long) = {
    val s = System.nanoTime()
    val r = block
    val e = System.nanoTime()
    (r, (e-s)/1000000000)
  }

  def readLargeDfAndPartition(n: Int): DataFrame = {
    val config = ConfigFactory.load()
    val driver = config.getString("jdbc.driver")
    val url = config.getString("jdbc.url")
    val username = config.getString("jdbc.username")
    val password = config.getString("jdbc.password")

    val prop = new Properties()
    prop.put("user", username)
    prop.put("password", password)

    spark.read.jdbc(url, "TARGET_FOR_SPARK_DF", "ID", 1, 6, n, prop)
  }


  def makeLargeDf(spark: SparkSession, n: Int) : DataFrame = {
    val data : List[(Int, String, Int)] = List[(Int, String, Int)]((1, "Finance", 10), (2, "Marketing", 20), (3, "Sales", 30), (4, "IT", 40), (5, "CTS", 41), (6, "CTS", 42))
    var deptColumns = List("ID", "dept_name", "dept_id")
    import spark.implicits._
    val rdd = spark.sparkContext.parallelize(replicateList(data,n))
    val df = rdd.toDF(deptColumns:_*)
    df
  }

  def sortRuleWithString(left: String, right: String): Boolean = {
    RuleTypeEnumeration.withName(left).id <= RuleTypeEnumeration.withName(right).id
  }

  def sortRuleWith(left: Rule, right: Rule)(implicit map: Map[String,String]): Boolean = {
    RuleTypeEnumeration.withName(map.getOrElse(left.ruleType, left.ruleType)).id <= RuleTypeEnumeration.withName(map.getOrElse(right.ruleType, right.ruleType)).id
  }

  def makeRuleDf(spark: SparkSession, n: Int) : DataFrame = {
    // rule_types is "S" = set, "I" = insert, "D" = "delete", "DU" = "duplicate"
    val ruleColumns = List("Scenario", "RuleOrder", "RuleType", "RuleText")
    val rules : List[(String, Int, String, String)] = List(
      ("Adj#1",1,"S", "S.SomeRuleDefn1"),
      ("Adj#2",2,"S","S.SomeRuleInAdj#2"),
      ("Adj#2",3,"I","insert.SomeRuleInAdj#2"),
      ("Adj#2",4,"S", "S.SomeOtherRuleInAdj#2"),
      ("Adj#2",5,"D", "D.SomeOtherRuleInAdj#2"),
      ("Adj#2",6,"DU", "S.SomeOtherRuleInAdj#2")
    )
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

  def cacheToParquet(df: DataFrame, path: String): DataFrame = {
    df.write.mode("overwrite").parquet(path)
    df.count()
    val df2 = spark.read.parquet(path)
    val r = df2.inputFiles.forall(f => f.contains(path))
    assert(r == true)
    df2
  }

  ignore("AlwaysPass") {
    assert(1==1)
  }

  ignore("SparkHelperTest") {
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

  ignore("postGreJdbc") {
    val url = Helper.getPostGreUrl("postgres","postgres", "foobar_secret")
    val conn = Helper.getJdbc("postgres", "postgres","foobar_secret")
    val rs = Helper.select(conn, "select * from foo")
    rs.last()
    assert(rs.getRow() > 0)
  }

  ignore("postGreJdbcFromConfig") {
    val conn = Helper.getJdbcFromConfig()
    val rs = Helper.select(conn, "select * from foo")
    rs.last()
    assert(rs.getRow() > 0)
  }

  ignore("config") {
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

  test("largeDfWriteToParquet") {
    val df = makeLargeDf(spark, 3)
    df.write.mode("overwrite").parquet("./foobar_largeDfWriteToParquet")
    val df2 = spark.read.parquet("./foobar_largeDfWriteToParquet")
    val r = df2.inputFiles.forall(f => f.contains("foobar_largeDfWriteToParquet"))
    assert(r == true)
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

  test("readLargeDfFromDBandWriteToDB") {
    val df = readLargeDfAndPartition(6)

    val config = ConfigFactory.load()
    val driver = config.getString("jdbc.driver")
    val url = config.getString("jdbc.url")
    val username = config.getString("jdbc.username")
    val password = config.getString("jdbc.password")

    (10 to 40 by 10).foreach(v => {
      val (_, e) = timer {
        df.write.mode("append")
          .format("jdbc")
          .option("url", url)
          .option("dbtable", "OTHER_TARGET_FOR_SPARK_DF")
          .option("user", username)
          .option("password", password)
          .option("batchsize", v * 100000)
          .save()
      }
      println(v, e)
    })
  }

  test("readLargeDf") {
    val config = ConfigFactory.load()
    val driver = config.getString("jdbc.driver")
    val url = config.getString("jdbc.url")
    val username = config.getString("jdbc.username")
    val password = config.getString("jdbc.password")

    val df = spark.read
      .format("jdbc")
      .option("url", url)
      .option("dbtable", "TARGET_FOR_SPARK_DF")
      .option("user", username)
      .option("password", password)
      .option("numPartitions", 12) // has no affect
      .load()

    val (a, b) = timer(
      println("Rows: ", df.count(), df.rdd.getNumPartitions)
    )
    println("Time:", b)

  }

  test("readLargeDfAndPartition") {
    val df  = readLargeDfAndPartition(3)
    val (a,b) = timer(
      println("Rows: ", df.count(), df.rdd.getNumPartitions)
    )
    println("Time:", b)
  }

  test("largeDfAdjustment") {
    val n = math.pow(2, 8).toInt
    spark.sparkContext.setCheckpointDir("checkpointing_folder")
    val df = makeLargeDf(spark, n)
    assert(df.count() == 6 * n)

    val rule_scale = math.pow(2, 5).toInt
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

    val rule_scale = math.pow(2, 5).toInt
    val rules = replicateList(modificationList(), rule_scale)
    import org.apache.spark.sql.functions._

    val (rows_affected, t) = timer {
      val flag_col = "isModified"
      val partition_cols = List[Column](col("dept_name"), col("dept_id"))
      val check_point_interval = 5

      var check_point_stale: Boolean = true

      val df_result = rules.zipWithIndex.foldLeft[DataFrame](df.withColumn(flag_col, lit(false)).repartition(8, partition_cols: _*).cache())((df_base, r) => {
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

      rows_affected
    }

    println(s"Time: $t sec. Rows affected: $rows_affected")
  }

  test("replicateList") {
    val list = replicateList(List(1,2,3), 3)
    assert(list == List(1,2,3,1,2,3,1,2,3))
  }

  ignore("parallelLogging") {

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


  test("convertDfToCaseClassAndConvertBacktoRow") {
    val df = makeLargeDf(spark, 1)
    //println("#count",df.count())
    import spark.implicits._

    val schema = Encoders.product[Data].schema

    val (_,e) = timer(
    df.as[Data].collect().foreach(x => {
      val values = x.productIterator.toSeq.toArray
      val row = new GenericRowWithSchema(values, schema)
      //println(x, row, row.getAs[Int]("id"), row.getAs[String]("dept_name"), row.getInt(0))
    }))
    println(s"with_convert_back: $e")

    val (_,e1) = timer(
      df.as[Data].collect().foreach(x => {
        //println(x, row, row.getAs[Int]("id"), row.getAs[String]("dept_name"), row.getInt(0))
      }))
    println(s"wo_convert_back: $e1")


  }

  test("dfToCaseClassForRulesFunctionalProcessing") {
    val df = makeRuleDf(spark, 1)
    import spark.implicits._

    val scenario_groups = df.as[Rule].collect().groupBy(r => r.scenario).par
    val results1 = scenario_groups.map(s => {
      implicit val logCollector = ListBuffer[String]()
      val scenario = s._1
      val rule_groups = s._2.sortWith( (r1,r2) => sortRuleWith(r1, r2)).groupBy(r => RuleTypeEnumeration.withName(r.ruleType).id)
      rule_groups.keys.toList.sorted.foreach(k => {
        loggingWithThreadIdAndCollection(s"--> Processing ${RuleTypeEnumeration(k)}")
        rule_groups(k).sortBy(_.ruleOrder).map(r => {
          loggingWithThreadIdAndCollection(s"${scenario}  ${r.ruleType} ${r.ruleOrder}")
        })
      })
      val booleanResult = System.nanoTime() % 2 == 0
      (scenario, booleanResult, logCollector)
    })

    // note par.map returns a par iterable,so convert to seq
    results1.seq.foreach(result => {
      println(s"scenario: ${result._1}, status: ${result._2}")
      printLog(result._3)
    }
    )


    val results2 = df.as[Rule].collect().sortBy(r => (r.scenario, r.ruleOrder)).groupBy(r => r.scenario).par.map( group =>
      {
        implicit val logCollector = ListBuffer[String]()
        val scenario = group._1
        val rules = group._2
        loggingWithThreadIdAndCollection(s"scenario: $scenario")
        rules.map(r => loggingWithThreadIdAndCollection(s"\t ${r.ruleOrder} -> ${r.ruleText}"))
        val booleanResult = System.nanoTime() % 2 == 0
        (scenario, booleanResult, logCollector)
      }
    )
    // note par.map returns a par iterable,so convert to seq
    results2.seq.foreach(result => {
      println(s"scenario: ${result._1}, status: ${result._2}")
      printLog(result._3)
    }
    )
  }

  test("collectPerf") {
    val arrayOf100s = Array.fill[Int](100)(100)
    arrayOf100s.foreach ( s => {
      import spark.implicits._
      val df = makeLargeDf(spark, s)
      assert (df.as[Data].collect().length == df.count())
    })
  }

  test("BespokeRuleOrdering") {

    // R_anyread_1 and R_anyread_2 are different types of reads we
    // want to treat the same for ordering purposes
    val rules = List[Rule](
      Rule("ABC", "t1", 1, "R_anyread_1", "t"),
      Rule("ABC", "t1", 3, "S", "t"),
      Rule("ABC", "t1", 5, "S", "t"),
      Rule("ABC", "t1", 7, "I", "t"),
      Rule("ABC", "t1", 9, "S", "t"),
      Rule("ABC", "t1", 11, "D", "t"),
      Rule("ABC", "t1", 13, "R_anyread_2", "t"),
      Rule("ABC", "t1", 15, "DU", "t"),
      Rule("ABC", "t1", 17, "DU", "t"),

      Rule("ABC", "t2", 2, "R_anyread_1", "t"),
      Rule("ABC", "t2", 4, "S", "t"),
      Rule("ABC", "t2", 6, "S", "t"),
      Rule("ABC", "t2", 8, "I", "t"),
      Rule("ABC", "t2", 10, "S", "t"),
      Rule("ABC", "t2", 12, "D", "t"),
      Rule("ABC", "t2", 14, "R_anyread_2", "t"),
      Rule("ABC", "t2", 16, "DU", "t"),
      Rule("ABC", "t2", 18, "DU", "t")

    )

    // group rules by ruleType and order them in ruleOrder within ruleType
//    val rules_group = rules.sortWith((r1, r2) => sortRuleWith(r1, r2)).groupBy(r => RuleTypeEnumeration.withName(mapOfReads.getOrElse(r.ruleType, r.ruleType)).id)
//    rules_group.keys.toList.sorted.foreach(k =>
//    {
//      println(s"**$k -> $RuleTypeEnumeration(k)")
//      rules_group(k).sortBy(r=>r.ruleOrder).foreach(f => println("\t", f))
//    })
//
//    println("=================")
//
//    // group rules by table and then as above
//    val rules_group_by_t = rules.groupBy(r => r.targetTable)
//    rules_group_by_t.foreach(g =>{
//      println(s"${g._1}")
//      val rules_group = g._2.sortWith((r1, r2) => sortRuleWith(r1, r2)).groupBy(r => RuleTypeEnumeration.withName(mapOfReads.getOrElse(r.ruleType, r.ruleType)).id)
//      rules_group.keys.toList.sorted.foreach(k =>
//      {
//        println(s"\t**$k -> $RuleTypeEnumeration(k)")
//        rules_group(k).sortBy(r=>r.ruleOrder).foreach(f => println("\t\t", f))
//      })
//    })

    println("================")

    // group rules by id, then by target table and then sort by ruleorder
    val rules_group_by_id = rules.sortWith((r1, r2) => sortRuleWith(r1, r2)).groupBy( r => RuleTypeEnumeration.withName(mapOfReads.getOrElse(r.ruleType, r.ruleType)).id )
    rules_group_by_id.keys.toArray.sortBy(k => k).foreach(g => {
      println(s"${g}")
      val rules_group =rules_group_by_id(g).groupBy(g => g.targetTable)
      rules_group.keys.foreach(k =>{
        println(s"\t**$k -> ${rules_group(k)}")
        rules_group(k).sortBy(r=>r.ruleOrder).foreach(f => println("\t\t", f))
      })
    })
  }

  test("parquetWriteTest") {
    val df = makeRuleDf(spark, 1)
    val parquetPath = "file:///parquet_files/test"
    cacheToParquet(df, parquetPath )
  }

  test("hadoopFs") {
    import org.apache.hadoop.fs._
    val File = FileSystem.get(spark.sparkContext.hadoopConfiguration)
    val parquetPath = "file:///parquet_files/test"
    assert (File.exists(new Path(parquetPath)))
    File.delete(new Path(parquetPath), true)
    assert (! File.exists(new Path(parquetPath)))
  }

  test("matchTuples") {
    val testTuples = Seq[(Boolean, String)]((true, "A"),(false,"B"),(true, "SET"), (false, "SET"))

    testTuples.foreach(tt => {
      tt match {
        case (_, "A") => println("A"); assert("A" == tt._2)
        case (_, "B") => println("B"); assert("B" == tt._2)
        case (true, "SET") => println("fast-SET"); assert(true == tt._1)
        case (false, "SET") => println("slow-SET"); assert(false == tt._1)
      }
    })
  }

  test("unionWithDefaults") {
    import spark.implicits._
    val df1 = Seq((1,2,3),(2,3,4),(3,4,5)).toDF("N1", "N2", "N3")
    val df2 = Seq(("A","B"),("B","C"),("C","D")).toDF("A1","A2")
    val df3 = Seq(3.14, 2.713, 9.81).toDF("F1")

    val data = Seq(df1, df2, df3).reduce(Helper.unionWithDefault(_,_))
    data.show(false)
  }


}
