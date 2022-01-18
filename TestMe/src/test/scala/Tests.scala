import org.scalatest.funsuite.AnyFunSuite
import org.apache.spark.sql.functions.{col, explode, from_json, get_json_object, lit, schema_of_json, to_json}
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

  test("ConvertStringColToJson") {
    val m = Map[String,String]()
    val df = Helper.loadCSV(spark, resourceCsvPath)
    val jsonSample = df.filter(col("match_element").contains("MatchStatusUpdate")).select("match_element").collect()
    val sampleJson = jsonSample(0).getString(0)
    val schemaJson = spark.range(1)
      .select(schema_of_json(lit(sampleJson)))
      .collect()(0).getString(0)

//    print(schemaJson)
//    struct<eventElementType:string,matchStatus:struct<courtNum:bigint,firstServer:string,matchState:struct<locationTimestamp:string,state:string>,numSets:bigint,scoringType:string,teamAPlayer1:string,teamAPlayersDetails:struct<player1Country:string,player1Id:string>,teamBPlayer1:string,teamBPlayersDetails:struct<player1Country:string,player1Id:string>,tieBreakType:string,tossChooser:string,tossWinner:string,umpire:string,umpireCode:string,umpireCountry:string>,matchTime:string,seqNum:bigint,timestamp:string>

    val df2 = df.withColumn("structFromJson", from_json(col("match_element"), schemaJson, m))
//    df2.printSchema()
//    |-- _c0: string (nullable = true)
//    |-- match_id: string (nullable = true)
//    |-- message_id: string (nullable = true)
//    |-- match_element: string (nullable = true)
//    |-- structFromJson: struct (nullable = true)
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
    df2.select("structFromJson.eventElementType", "structFromJson.matchStatus.matchState.state").show()
    df2.select("structFromJson.*").show()
  }


}
