import org.scalatest.funsuite.AnyFunSuite
import org.apache.spark.sql.functions.{col, explode, get_json_object, lit, schema_of_json, to_json}
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

  test("CleanAndFlatten") {
    val df = Helper.loadCSV(spark, resourceCsvPath)
    val jsonSample = df.select("match_element").collect()
    val sampleJson = jsonSample(0).getString(0)
    val schemaJson = spark.range(1)
      .select(schema_of_json(lit(sampleJson)))
      .collect()(0)(0)

    df.select(get_json_object(col("match_element"), "*")).show(5)

    df.select(from)
  }


}
