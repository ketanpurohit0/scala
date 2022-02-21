import org.apache.spark.sql.{Column, DataFrame}
import org.apache.spark.sql.expressions.Window
import org.scalatest.funsuite.AnyFunSuite
import org.apache.spark.sql.functions.{
  array,
  arrays_zip,
  avg,
  col,
  concat,
  count,
  explode,
  expr,
  from_json,
  get_json_object,
  lag,
  lead,
  length,
  lit,
  row_number,
  schema_of_json,
  size,
  sum,
  to_json,
  when,
  upper
}
import org.apache.spark.sql.types.DataTypes.createDecimalType
import org.apache.spark.sql.types.{DataType, StructType}
class Tests extends AnyFunSuite {

  val spark = Helper.getSparkSession("local[*]", "test")
  import spark.implicits._
  val resourceCsvPath =
    "file:/MyWork/GIT/scala/TestMe/resource/keystrokes-for-tech-test.csv"
  val resourceCsvLineCount = 2212

  ignore("TestSparkSession") {
    //val spark = Helper.getSparkSession("local[*]", "test")
    spark.conf.getAll.foreach(c => println(c._1, ":=", c._2))
    assert(spark.conf.getAll.isEmpty == false)
  }

  ignore("ImportTestCSV") {
    val df = Helper.loadCSV(spark, resourceCsvPath)
    assert(df.count() == resourceCsvLineCount)

//    df.printSchema()
//    root
//    |-- _c0: string (nullable = true)
//    |-- match_id: string (nullable = true)
//    |-- message_id: string (nullable = true)
//    |-- match_element: string (nullable = true)

//      df.show(2)
//    +---+--------+----------+--------------------+
//    |_c0|match_id|message_id|       match_element|
//    +---+--------+----------+--------------------+
//    |  0|   29304|         1|{'seqNum': 1, 'ma...|
//    |  1|   29304|         2|{'seqNum': 2, 'ma...|
//    +---+--------+----------+--------------------+

  }

  ignore("GetJsonSchemaForType") {
    val df = Helper.loadCSV(spark, resourceCsvPath)
    val eventElemTypes = Array[String](
      "MatchStatusUpdate",
      "PointStarted",
      "PointFault",
      "PointScored",
      "PhysioCalled",
      "PointLet",
      "CodeViolation",
      "TimeAnnouncement"
    )
    val jsonSchemaMap = eventElemTypes.map(t =>
      t -> Helper.getJsonSchemaForType(spark, df, "match_element", t)
    )
//    jsonSchemaMap.map(println)
//    (MatchStatusUpdate,struct<eventElementType:string,matchStatus:struct<courtNum:bigint,firstServer:string,matchState:struct<locationTimestamp:string,state:string>,numSets:bigint,scoringType:string,teamAPlayer1:string,teamAPlayersDetails:struct<player1Country:string,player1Id:string>,teamBPlayer1:string,teamBPlayersDetails:struct<player1Country:string,player1Id:string>,tieBreakType:string,tossChooser:string,tossWinner:string,umpire:string,umpireCode:string,umpireCountry:string>,matchTime:string,seqNum:bigint,timestamp:string>)
//    (PointStarted,struct<eventElementType:string,matchTime:string,nextServer:struct<team:string>,seqNum:bigint,server:struct<team:string>,timestamp:string>)
//    (PointFault,struct<eventElementType:string,faultType:string,matchTime:string,nextServer:struct<team:string>,seqNum:bigint,server:struct<team:string>,timestamp:string>)
//    (PointScored,struct<details:struct<pointType:string,scoredBy:string>,eventElementType:string,matchTime:string,nextServer:struct<team:string>,score:struct<currentGameScore:struct<gameType:string,pointsA:string,pointsB:string>,currentSetScore:struct<gamesA:bigint,gamesB:bigint>,previousSetsScore:array<null>>,seqNum:bigint,server:struct<team:string>,timestamp:string>)
//    (PhysioCalled,struct<eventElementType:string,matchTime:string,seqNum:bigint,team:string,timestamp:string>)
//    (PointLet,struct<eventElementType:string,matchTime:string,nextServer:struct<team:string>,seqNum:bigint,server:struct<team:string>,timestamp:string>)
//    (CodeViolation,struct<eventElementType:string,matchTime:string,playerId:bigint,reason:string,seqNum:bigint,team:string,timestamp:string>)
//    (TimeAnnouncement,struct<eventElementType:string,matchTime:string,seqNum:bigint,time:string,timestamp:string>)

  }

  test("mysql_explore_question") {
    import spark.implicits._
    // Loading class `com.mysql.jdbc.Driver'. This is deprecated. The new driver class is `com.mysql.cj.jdbc.Driver'.
    val loadDf = spark.read
      .format("jdbc")
      .option("url", "jdbc:mysql://localhost:3306/DevTest")
      .option("driver", "com.mysql.cj.jdbc.Driver")
      .option("user", "root")
      .option("password", "Sdy+q8bLwsNB")
      .option("dbtable", "question")
      .load()
      .filter(
        $"questionType" === "TB"
      )

    loadDf.show()
//    loadDf.printSchema()
    val m = Map[String, String]()
    val setYJsonSchema = Helper.getJsonSchema(spark, loadDf, "setY")
    val setXJsonSchema = Helper.getJsonSchema(spark, loadDf, "setX")

    // convert from 'string' to json struct using schema obtained earlier
    val df = loadDf
      .filter($"questionId" === "54657374-696E-6720-5131-343130000000")
      .withColumn("setY", from_json($"setY", setYJsonSchema, m))
      .withColumn("setX", from_json($"setX", setXJsonSchema, m))

//    df.show()
    df.printSchema()

    val columnsOfInterest = Seq(
      "setX.options.id",
      "setX.options.langs.en_GB.text",
      "setX.options.reportingValue"
    )
    df.select(columnsOfInterest.head, columnsOfInterest.tail: _*).show()

    val columnsOfInterest2 = Seq(
      "options.id",
      "options.langs.en_GB.text",
      "options.reportingValue"
    )

    df.select(explode($"setX.options").as("options"))
      .select(columnsOfInterest2.head, columnsOfInterest2.tail: _*)
      .withColumn("id", upper($"id"))
      .show()

    val df2 =
      df.withColumn(
        "setYid",
        explode($"setY.options.id")
      ) //langs.en_GB"))

//    df2.printSchema()

//    df2
//      .select("questionId", "setYid")
//      .show(100, false)

    df.select($"questionId", explode($"setX.options.id")).show(100, false)
  }

  test("mysql explore surveydatopt") {

    import spark.implicits._
    // Loading class `com.mysql.jdbc.Driver'. This is deprecated. The new driver class is `com.mysql.cj.jdbc.Driver'.

    val questionId = "54657374-696E-6720-5131-343130000000"

    val loadOptionsDf = spark.read
      .format("jdbc")
      .option("url", "jdbc:mysql://localhost:3306/DevTest")
      .option("driver", "com.mysql.cj.jdbc.Driver")
      .option("user", "root")
      .option("password", "Sdy+q8bLwsNB")
      .option("dbtable", "surveydataopt")
      .load()
      .filter($"questionId" === s"$questionId")
      .select(Seq("setY", "setX").map(col(_)): _*)

    // bring it together with 'question'

    val loadQuestionDf = spark.read
      .format("jdbc")
      .option("url", "jdbc:mysql://localhost:3306/DevTest")
      .option("driver", "com.mysql.cj.jdbc.Driver")
      .option("user", "root")
      .option("password", "Sdy+q8bLwsNB")
      .option("dbtable", "question")
      .load()
      .filter(
        $"questionType" === "TB"
      )

    val m = Map[String, String]()
    val setYJsonSchema = Helper.getJsonSchema(spark, loadQuestionDf, "setY")
    val setXJsonSchema = Helper.getJsonSchema(spark, loadQuestionDf, "setX")

    // convert from 'string' to json struct using schema obtained earlier
    val questionDf = loadQuestionDf
      .filter($"questionId" === s"$questionId")
      .withColumn("setY", from_json($"setY", setYJsonSchema, m))
      .withColumn("setX", from_json($"setX", setXJsonSchema, m))

    val columnsOfInterest = Seq(
      "options.id",
      "options.langs.en_GB.text",
      "options.reportingValue"
    )

    val questionDf2 = questionDf
      .select(explode($"setX.options").as("options"))
      .select(columnsOfInterest.head, columnsOfInterest.tail: _*)
      .withColumn("id", upper($"id"))
      .withColumnRenamed("id", "setXid")

    questionDf2.show()

    loadOptionsDf.show()
//    loadDf.printSchema()

    val enrichedOptionsDf = loadOptionsDf
      .join(
        questionDf2,
        loadOptionsDf.col("setX") === questionDf2.col("setXid")
      )
//      .withColumn(
//        "setX",
//        concat(Seq(col("text"), lit(" ("), col("reportingValue"), lit(")")): _*)
//      )

    enrichedOptionsDf.show()

    val reportingValueDf = enrichedOptionsDf
      .groupBy("setY") //, "setX")
      .agg(sum("reportingValue").as("sumReportingValue"))

    val pivotedDf = enrichedOptionsDf
      .groupBy("setY")
      .pivot($"setX")
      .agg(count(lit(1)))
      .na
      .fill(0)

    val summaryDf = loadOptionsDf
      .groupBy("setY")
      .count()

    val interimDf = summaryDf
      .join(pivotedDf, "setY")
//      .join(reportingValueDf, "setY")
//      .withColumnRenamed("sumReportingValue", "Average")
//      .withColumn("Average", col("Average") / col("count"))

    val columnsToConvertToPCT =
      interimDf.columns.filter(c => !(c == "setY" || c == "count"))

    val resultDf = columnsToConvertToPCT.foldLeft(interimDf) { (df, c) =>
      df.withColumn(
        c,
        (lit(100.0) * $"$c" / $"count")
      )
    }

//    summaryDf.show()
//    pivotedDf.show()
//    interimDf.show()
    resultDf.show()
    resultDf.printSchema()

  }

  ignore("FlattenSchema") {
    val df = Helper.flattenSchema(spark, resourceCsvPath)
    df.printSchema()

  }

  ignore("Requirement_Clean_&_flatten_the_data") {

    val resultDf = Helper.cleanAndFlatten(spark, resourceCsvPath)
    resultDf.filter("PhysioCalled ==1").orderBy("match_id", "message_id").show()
  }

  ignore("Enrichment_AddSecondServeFlag") {
    val resultDf = Helper.enrichmentAddSecondFlag(spark, resourceCsvPath)
//    resultDf.show()
  }

  ignore("Transformation") {
    val resultDf = Helper.transformation(spark, resourceCsvPath)

    resultDf.printSchema()
    resultDf
      .filter("match_element.eventElementType == 'PointScored'")
      .select(
        "message_id",
        "fixedOverallScore",
        "match_element.score.overallSetScore"
      )
      .show()
  }

  ignore("R&D") {
    // add a cumulative number of Aces in the match (regardless of who served the ace)
    val rndDf = Helper.RND(spark, resourceCsvPath)
//  rndDf.printSchema()
//  rndDf.select("match_id", "message_id","TotalMatchAces").show()
  }

  ignore("ConvertStringColToJson") {
    val m = Map[String, String]()
    val df = Helper.loadCSV(spark, resourceCsvPath)
    val eventElemTypes = Array[String](
      "MatchStatusUpdate",
      "PointStarted",
      "PointFault",
      "PhysioCalled",
      "PointLet",
      "CodeViolation",
      "TimeAnnouncement"
    )
    val jsonSchemasPerType = eventElemTypes.map(t =>
      t -> Helper.getJsonSchemaForType(spark, df, "match_element", t)
    )

    val dfs = jsonSchemasPerType.map(schemaForType =>
      schemaForType._1 -> df
        .filter(col("match_element").contains(schemaForType._1))
        .withColumn(
          s"match_element",
          from_json(col("match_element"), schemaForType._2, m)
        )
    )

    dfs.foreach(d => println(d._2.columns.mkString("Array(", ", ", ")")))

    dfs.foreach(d => d._2.select(col(s"match_element.*")).show(5))

    val commonCols = dfs
      .map(d => d._2.select(col(s"match_element.*")).columns)
      .reduce(_ intersect _)
    println(commonCols.mkString("Array(", ", ", ")"))

//    dfs.foreach(d => {println(d._1); d._2.printSchema()})
//    MatchStatusUpdate
//    root
//    |-- _c0: string (nullable = true)
//    |-- match_id: string (nullable = true)
//    |-- message_id: string (nullable = true)
//    |-- match_element: string (nullable = true)
//    |-- tost_MatchStatusUpdate: struct (nullable = true)
//    |    |-- eventElementType: string (nullable = true)
//    |    |-- matchStatus: struct (nullable = true)
//    |    |    |-- courtNum: long (nullable = true)
//    |    |    |-- firstServer: string (nullable = true)
//    |    |    |-- matchState: struct (nullable = true)
//    |    |    |    |-- locationTimestamp: string (nullable = true)
//    |    |    |    |-- state: string (nullable = true)
//    |    |    |-- numSets: long (nullable = true)
//    |    |    |-- scoringType: string (nullable = true)
//    |    |    |-- teamAPlayer1: string (nullable = true)
//    |    |    |-- teamAPlayersDetails: struct (nullable = true)
//    |    |    |    |-- player1Country: string (nullable = true)
//    |    |    |    |-- player1Id: string (nullable = true)
//    |    |    |-- teamBPlayer1: string (nullable = true)
//    |    |    |-- teamBPlayersDetails: struct (nullable = true)
//    |    |    |    |-- player1Country: string (nullable = true)
//    |    |    |    |-- player1Id: string (nullable = true)
//    |    |    |-- tieBreakType: string (nullable = true)
//    |    |    |-- tossChooser: string (nullable = true)
//    |    |    |-- tossWinner: string (nullable = true)
//    |    |    |-- umpire: string (nullable = true)
//    |    |    |-- umpireCode: string (nullable = true)
//    |    |    |-- umpireCountry: string (nullable = true)
//    |    |-- matchTime: string (nullable = true)
//    |    |-- seqNum: long (nullable = true)
//    |    |-- timestamp: string (nullable = true)
//
//    PointStarted
//    root
//    |-- _c0: string (nullable = true)
//    |-- match_id: string (nullable = true)
//    |-- message_id: string (nullable = true)
//    |-- match_element: string (nullable = true)
//    |-- tost_PointStarted: struct (nullable = true)
//    |    |-- eventElementType: string (nullable = true)
//    |    |-- matchTime: string (nullable = true)
//    |    |-- nextServer: struct (nullable = true)
//    |    |    |-- team: string (nullable = true)
//    |    |-- seqNum: long (nullable = true)
//    |    |-- server: struct (nullable = true)
//    |    |    |-- team: string (nullable = true)
//    |    |-- timestamp: string (nullable = true)
//
//    PointFault
//    root
//    |-- _c0: string (nullable = true)
//    |-- match_id: string (nullable = true)
//    |-- message_id: string (nullable = true)
//    |-- match_element: string (nullable = true)
//    |-- tost_PointFault: struct (nullable = true)
//    |    |-- eventElementType: string (nullable = true)
//    |    |-- faultType: string (nullable = true)
//    |    |-- matchTime: string (nullable = true)
//    |    |-- nextServer: struct (nullable = true)
//    |    |    |-- team: string (nullable = true)
//    |    |-- seqNum: long (nullable = true)
//    |    |-- server: struct (nullable = true)
//    |    |    |-- team: string (nullable = true)
//    |    |-- timestamp: string (nullable = true)
//
//    PhysioCalled
//    root
//    |-- _c0: string (nullable = true)
//    |-- match_id: string (nullable = true)
//    |-- message_id: string (nullable = true)
//    |-- match_element: string (nullable = true)
//    |-- tost_PhysioCalled: struct (nullable = true)
//    |    |-- eventElementType: string (nullable = true)
//    |    |-- matchTime: string (nullable = true)
//    |    |-- seqNum: long (nullable = true)
//    |    |-- team: string (nullable = true)
//    |    |-- timestamp: string (nullable = true)
//
//    PointLet
//    root
//    |-- _c0: string (nullable = true)
//    |-- match_id: string (nullable = true)
//    |-- message_id: string (nullable = true)
//    |-- match_element: string (nullable = true)
//    |-- tost_PointLet: struct (nullable = true)
//    |    |-- eventElementType: string (nullable = true)
//    |    |-- matchTime: string (nullable = true)
//    |    |-- nextServer: struct (nullable = true)
//    |    |    |-- team: string (nullable = true)
//    |    |-- seqNum: long (nullable = true)
//    |    |-- server: struct (nullable = true)
//    |    |    |-- team: string (nullable = true)
//    |    |-- timestamp: string (nullable = true)
//
//    CodeViolation
//    root
//    |-- _c0: string (nullable = true)
//    |-- match_id: string (nullable = true)
//    |-- message_id: string (nullable = true)
//    |-- match_element: string (nullable = true)
//    |-- tost_CodeViolation: struct (nullable = true)
//    |    |-- eventElementType: string (nullable = true)
//    |    |-- matchTime: string (nullable = true)
//    |    |-- playerId: long (nullable = true)
//    |    |-- reason: string (nullable = true)
//    |    |-- seqNum: long (nullable = true)
//    |    |-- team: string (nullable = true)
//    |    |-- timestamp: string (nullable = true)
//
//    TimeAnnouncement
//    root
//    |-- _c0: string (nullable = true)
//    |-- match_id: string (nullable = true)
//    |-- message_id: string (nullable = true)
//    |-- match_element: struct (nullable = true)
//    |    |-- eventElementType: string (nullable = true)
//    |    |-- matchTime: string (nullable = true)
//    |    |-- seqNum: long (nullable = true)
//    |    |-- time: string (nullable = true)
//    |    |-- timestamp: string (nullable = true)
//

  }

  ignore("pivotAndFill") {
    val data = Seq(
      ("EventA", 2, "EventA", "Event1"),
      ("EventA", 3, "EventB", "Event3"),
      ("EventB", 4, "EventC", "Event2"),
      ("EventC", 5, "EventD", "Event4")
    )

    val df = data.toDF("State", "ID", "PriorState", "OutComeState")
    df.show()

//    val pivotDf = df.groupBy("ID").pivot(col("State")).agg(lit(1))
//    val pivotDf_Filled = pivotDf.na.fill(0)
//    pivotDf_Filled.show()
//
//    val pivotDf2 = df.groupBy("ID").pivot(col("State"),Seq("EventA","EventB")).agg(lit(1))
//    val pivotDf2_Filled = pivotDf2.na.fill(0)
//    pivotDf2_Filled.show()

    val pivotedToPre = df.groupBy("ID").pivot("PriorState").agg(lit(1))
    pivotedToPre.show()

    val pivotedToOutcome = df.groupBy("ID").pivot("OutComeState").agg(lit(1))
    pivotedToOutcome.show()

  }

}
