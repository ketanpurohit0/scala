import org.apache.spark.sql.Column
import org.apache.spark.sql.expressions.Window
import org.scalatest.funsuite.AnyFunSuite
import org.apache.spark.sql.functions.{col, explode, from_json, get_json_object, lag, lead, lit, row_number, schema_of_json, to_json, when}
import org.apache.spark.sql.types.{DataType, StructType}
class Tests extends AnyFunSuite{

  val spark = Helper.getSparkSession("local[*]", "test")
  import spark.implicits._
  val resourceCsvPath = "file:/MyWork/GIT/scala/TestMe/resource/keystrokes-for-tech-test.csv"
  val resourceCsvLineCount = 2212


  test("TestSparkSession") {
    //val spark = Helper.getSparkSession("local[*]", "test")
    // spark.conf.getAll.foreach( c => println(c._1, ":=", c._2))
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
    val m = Map[String,String]()
    val df = Helper.loadCSV(spark, resourceCsvPath)
    val json_schema = Helper.getJsonSchema(spark, df, "match_element")
    val df2 = df.withColumn("match_element", from_json(col("match_element"), json_schema, m))
//    df2.printSchema()
//    root
//    |-- _c0: string (nullable = true)
//    |-- match_id: string (nullable = true)
//    |-- message_id: string (nullable = true)
//    |-- match_element: struct (nullable = true)
//    |    |-- delayStatus: string (nullable = true)
//    |    |-- details: struct (nullable = true)
//    |    |    |-- pointType: string (nullable = true)
//    |    |    |-- scoredBy: string (nullable = true)
//    |    |-- eventElementType: string (nullable = true)
//    |    |-- faultType: string (nullable = true)
//    |    |-- matchStatus: struct (nullable = true)
//    |    |    |-- courtNum: long (nullable = true)
//    |    |    |-- firstServer: string (nullable = true)
//    |    |    |-- matchState: struct (nullable = true)
//    |    |    |    |-- challengeEnded: string (nullable = true)
//    |    |    |    |-- evaluationStarted: string (nullable = true)
//    |    |    |    |-- locationTimestamp: string (nullable = true)
//    |    |    |    |-- playerId: long (nullable = true)
//    |    |    |    |-- state: string (nullable = true)
//    |    |    |    |-- team: string (nullable = true)
//    |    |    |    |-- treatmentEnded: string (nullable = true)
//    |    |    |    |-- treatmentLocation: string (nullable = true)
//    |    |    |    |-- treatmentStarted: string (nullable = true)
//    |    |    |    |-- won: string (nullable = true)
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
//    |    |-- nextServer: struct (nullable = true)
//    |    |    |-- team: string (nullable = true)
//    |    |-- numOverrules: long (nullable = true)
//    |    |-- playerId: long (nullable = true)
//    |    |-- reason: string (nullable = true)
//    |    |-- score: struct (nullable = true)
//    |    |    |-- currentGameScore: struct (nullable = true)
//    |    |    |    |-- gameType: string (nullable = true)
//    |    |    |    |-- pointsA: string (nullable = true)
//    |    |    |    |-- pointsB: string (nullable = true)
//    |    |    |-- currentSetScore: struct (nullable = true)
//    |    |    |    |-- gamesA: long (nullable = true)
//    |    |    |    |-- gamesB: long (nullable = true)
//    |    |    |-- overallSetScore: struct (nullable = true)
//    |    |    |    |-- setsA: long (nullable = true)
//    |    |    |    |-- setsB: long (nullable = true)
//    |    |    |-- previousSetsScore: array (nullable = true)
//    |    |    |    |-- element: struct (containsNull = true)
//    |    |    |    |    |-- gamesA: long (nullable = true)
//    |    |    |    |    |-- gamesB: long (nullable = true)
//    |    |    |    |    |-- tieBreakScore: struct (nullable = true)
//    |    |    |    |    |    |-- pointsA: long (nullable = true)
//    |    |    |    |    |    |-- pointsB: long (nullable = true)
//    |    |-- seqNum: long (nullable = true)
//    |    |-- server: struct (nullable = true)
//    |    |    |-- team: string (nullable = true)
//    |    |-- team: string (nullable = true)
//    |    |-- time: string (nullable = true)
//    |    |-- timestamp: string (nullable = true)
//    |    |-- won: string (nullable = true)

    val topLevelParent = Option.empty[String]
    val cols = Helper.flattenSchema(df2.schema, topLevelParent)
    //println(cols.mkString("||"))

    val columns_to_select = cols.map(c => col(c).as(c.replace(".","_")))
    df2.select(columns_to_select:_*).printSchema()

  }

  test("LeadAndLagOverStruct") {
    val m = Map[String,String]()
    val df = Helper.loadCSV(spark, resourceCsvPath)
    val json_schema = Helper.getJsonSchema(spark, df, "match_element")
    val df2 = df.withColumn("match_element", from_json(col("match_element"), json_schema, m))
    val window = Window.partitionBy("match_id").orderBy("match_element.seqNum")
    val columnsOfInterest = Seq("match_element.seqNum", "match_element.eventElementType","match_element.details.scoredBy")
    def lead_lag_columns(f : (String, Int) => Column, prefix: String) = {
      columnsOfInterest.map(c => f(c, 1).over(window).as(prefix+c))
    }
    val all_cols = Seq(col("match_element.server.team").as("serving_team")) ++ lead_lag_columns(lag, "LAG_") ++ Seq(col("*")) ++ lead_lag_columns(lead, "LEAD_")
    val df3 = df2.select(all_cols:_*).filter("match_element.eventElementType = 'PointStarted'")
      .withColumn("serveid", row_number().over(window))
      .withColumn("server", when(col("serving_team") ==="TeamA",col("match_element.matchStatus.teamAPlayer1"))
      .otherwise(col("match_element.matchStatus.teamBPlayer1")))
      .drop("_c0")

    val dfMatchPlayers = df2
                         .filter("match_element.eventElementType = 'MatchStatusUpdate' and match_element.matchStatus.matchState.state = 'PlayersArriveOnCourt'")
                         .select("match_id","match_element.matchStatus.teamAPlayer1","match_element.matchStatus.teamBPlayer1")

    val joinCondition = (df3("match_id") === dfMatchPlayers("match_id"))
    val df4 = df3.join(dfMatchPlayers,joinCondition,"inner")
                 .select(df3("*"), dfMatchPlayers("teamAPlayer1"),dfMatchPlayers("teamBPlayer1"))
                 .withColumn("server", when(col("serving_team") === "TeamB", col("teamBPlayer1")).otherwise(col("teamAPlayer1")))
                 .drop(Seq("teamBPlayer1","teamAPlayer1"):_*)



    df3.show(5)
    dfMatchPlayers.show(5)
    df4.show(35)
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
      ("EventA",2),
      ("EventA",3),
      ("EventB",4),
      ("EventC",5)
    )

    val df = data.toDF("Event","ID")
    df.show()

    val pivotDf = df.groupBy("ID").pivot(col("Event")).agg(lit(1))
    val pivotDf_Filled = pivotDf.na.fill(0)
    pivotDf_Filled.show()

    val pivotDf2 = df.groupBy("ID").pivot(col("Event"),Seq("EventA","EventB")).agg(lit(1))
    val pivotDf2_Filled = pivotDf2.na.fill(0)
    pivotDf2_Filled.show()

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
