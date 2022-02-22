import org.apache.spark.sql.functions.{col, lit, schema_of_json}
import org.apache.spark.sql.types.{StructField, StructType}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{Column, DataFrame, SparkSession}

object Helper {
  def getSparkSession(sparkMaster: String, appName: String): SparkSession = {
    val conf = new SparkConf()
      .setMaster(sparkMaster)
      .set("spark.app.name", appName)
    // .set("spark.driver.host","localhost")
    val sc = new SparkContext(conf)
    val spark = SparkSession.builder().getOrCreate()
    spark.sparkContext.setLogLevel("ERROR")
    spark.conf.set("spark.sql.crossJoin.enabled", true)
    spark.conf.set("spark.sql.caseSensitive", false)
    spark
  }

}
