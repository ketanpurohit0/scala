import org.apache.spark.sql.functions.{col, lit, schema_of_json}
import org.apache.spark.sql.types.{StructField, StructType}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{Column, DataFrame, SparkSession}

object Helper {
  def getSparkSession(sparkMaster: String, appName: String): SparkSession = {
    val conf = new SparkConf().setMaster(sparkMaster).set("spark.app.name", appName)
    val sc = new SparkContext(conf)
    val spark = SparkSession.builder().getOrCreate()
    spark.sparkContext.setLogLevel("ERROR")
    spark.conf.set("spark.sql.crossJoin.enabled", true)
    spark.conf.set("spark.sql.caseSensitive", false)
    spark
  }


  def loadCSV(spark: SparkSession, filePath: String): DataFrame = {
    spark.read
      .option("header", "true")
      .option("multiline","true")
      .option("escape", "\"")
      .csv(filePath)
  }

  def getJsonSchemaForType(spark: SparkSession, df: DataFrame, jsonColName: String, eventElementType: String): String = {
    val jsonSample = df
                    .filter(col(jsonColName).contains(eventElementType))
                    .select(jsonColName)
                    .collect()(0).getString(0)
    val schemaJson = spark.range(1)
      .select(schema_of_json(lit(jsonSample)))
      .collect()(0).getString(0)
    schemaJson
  }

  def getJsonSchema(spark: SparkSession, df: DataFrame, jsonColName: String): StructType = {
    import spark.implicits._
    spark.read.json(df.select(col(jsonColName).as[String])).schema
  }

  def flattenSchema(schema: StructType, parent: Option[String]) : Array[String] = {
    // heavily inspired by https://sparkbyexamples.com/spark/spark-flatten-nested-struct-column/
    val columnTag = parent match {
      case Some(parentValue) => s"${parentValue}."
      case None => ""
    }

    val cols = schema.fields.flatMap(f => {
        f.dataType match {
          case s : StructType => flattenSchema(s, Some(s"${columnTag}${f.name}"))//s"Struct<${f.name}>"
          case _ => Array(s"${columnTag}${f.name}")
        }
    })


    cols
  }




}
