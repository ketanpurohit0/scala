import org.apache.spark.sql.{Column, DataFrame}
import org.apache.spark.sql.expressions.Window
import org.scalatest.funsuite.AnyFunSuite
import org.apache.spark.sql.functions.{col, concat, explode, from_json, get_json_object, lag, lead, lit, row_number, schema_of_json, sum, to_json, when}
import org.apache.spark.sql.types.{DataType, StructType}
class Tests extends AnyFunSuite{

  val spark = Helper.getSparkSession("local[*]", "test")
  import spark.implicits._
  val resourceCsvPath = "file:/MyWork/GIT/scala/TestMe/resource/keystrokes-for-tech-test.csv"
  val resourceCsvLineCount = 2212


  test("TestSparkSession") {
    //val spark = Helper.getSparkSession("local[*]", "test")
    spark.conf.getAll.foreach( c => println(c._1, ":=", c._2))
    assert(spark.conf.getAll.isEmpty == false)
  }

  test("ImportTestCSV") {
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

  test("GetJsonSchemaForType") {
    val df = Helper.loadCSV(spark, resourceCsvPath)
    val eventElemTypes = Array[String]("MatchStatusUpdate", "PointStarted", "PointFault", "PointScored", "PhysioCalled","PointLet", "CodeViolation", "TimeAnnouncement")
    val jsonSchemaMap = eventElemTypes.map(t => t -> Helper.getJsonSchemaForType(spark, df, "match_element", t))
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

  test("FlattenSchema")  {
    val df = Helper.flattenSchema(spark, resourceCsvPath)
    df.printSchema()

  }

  test("Requirement_Clean_&_flatten_the_data") {

    val resultDf = Helper.cleanAndFlatten(spark, resourceCsvPath)
    //resultDf.show()
  }

  test("Enrichment_AddSecondServeFlag") {
    val resultDf = Helper.enrichmentAddSecondFlag(spark, resourceCsvPath)
    resultDf.show()
  }

  test("Transformation") {
    val m = Map[String,String]()
    val df = Helper.loadCSV(spark, resourceCsvPath)
    val json_schema = Helper.getJsonSchema(spark, df, "match_element")
    val df2 = df.withColumn("match_element", from_json(col("match_element"), json_schema, m))
    df2
      .select("match_id", "message_id","match_element.score.previousSetsScore", "match_element.score.overallSetScore")
      .filter(col("previousSetsScore").isNotNull)
      .withColumn("A_S", col("previousSetsScore.gamesA"))
      .show(100, false)
//      .selectExpr("aggregate(previousSetsScore, 0, (x.gamesA, y.gamesB) -> x + y) as details_sum")
//      .printSchema()
//      .withColumn("x", col("match_element.score.previousSetsScore"))
//      .show(100, false)
  }

  test("R&D") {
    // add a cumulative number of Aces in the match (regardless of who served the ace)
    val m = Map[String,String]()
    val df = Helper.loadCSV(spark, resourceCsvPath)
    val json_schema = Helper.getJsonSchema(spark, df, "match_element")
    val df2 = df.withColumn("match_element", from_json(col("match_element"), json_schema, m))
                .withColumn("message_id", col("message_id").cast("int"))

    val w = Window.partitionBy("match_id")
                  .orderBy(col("message_id"))
                  .rowsBetween(Window.unboundedPreceding, Window.currentRow)

    val df3 = df2
              .withColumn("IsAce", when(col("match_element.details.pointType") === "Ace",lit(1)).otherwise(lit(0)))
              .withColumn("TotalMatchAces", sum($"IsAce").over(w))
              .drop("IsAce")
//   df3.printSchema()
      df3.select("match_id", "message_id","TotalMatchAces").show(1000)
  }

  test("ConvertStringColToJson") {
    val m = Map[String,String]()
    val df = Helper.loadCSV(spark, resourceCsvPath)
    val eventElemTypes = Array[String]("MatchStatusUpdate", "PointStarted", "PointFault", "PhysioCalled","PointLet", "CodeViolation","TimeAnnouncement")
    val jsonSchemasPerType = eventElemTypes.map(t => t -> Helper.getJsonSchemaForType(spark, df, "match_element", t))


    val dfs = jsonSchemasPerType.map( schemaForType => schemaForType._1 -> df.filter(col("match_element").contains(schemaForType._1)).withColumn(s"match_element", from_json(col("match_element"), schemaForType._2, m)))

    dfs.foreach(d => println(d._2.columns.mkString("Array(", ", ", ")")))

    dfs.foreach(d => d._2.select(col(s"match_element.*")).show(5))

    val commonCols = dfs.map(d => d._2.select(col(s"match_element.*")).columns).reduce(_ intersect _)
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

  test("pivotAndFill") {
    val data = Seq(
      ("EventA",2, "EventA","Event1"),
      ("EventA",3, "EventB","Event3"),
      ("EventB",4, "EventC","Event2"),
      ("EventC",5, "EventD","Event4")
    )

    val df = data.toDF("State","ID","PriorState","OutComeState")
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

    val pivotedToOutcome =  df.groupBy("ID").pivot("OutComeState").agg(lit(1))
    pivotedToOutcome.show()



  }

  test("oneHotEncode") {
      val data = Seq(
        ("EventA",2),
        ("EventA",3),
        ("EventB",4),
        ("EventC",5)
      )

      val df = data.toDF("Event","ID")
      df.show()

    }


}
