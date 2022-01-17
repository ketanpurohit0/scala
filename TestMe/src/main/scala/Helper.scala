import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{DataFrame, SparkSession}

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
      .csv(filePath)
  }
}
