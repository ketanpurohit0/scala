import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{DataFrame, DataFrameReader, SparkSession}

object SparkHelper {
  def getSparkSession(sparkMaster: String, jarsFolder: String): SparkSession = {
    val conf = new SparkConf().setMaster(sparkMaster).set("spark.app.name", "TEST")
    val sc = new SparkContext(conf)
    sc.setLogLevel("ERROR")
    val spark = SparkSession.builder().getOrCreate()
    spark.conf.set("spark.sql.crossJoin.enabled", true)
    conf.set("spark.jars", jarsFolder)
    return spark
  }

  def getDfReader(sparkSession: SparkSession,sql: String, driver: String, user: String, pass: String, url: String): DataFrameReader = {

    var reader = sparkSession.read.format("jdbc")
      .option("driver", driver)
      .option("user", user)
      .option("password", pass)
      .option("url", url)

    return reader

  }

  def getTableDf(sparkSession: SparkSession,sql: String, driver: String, user: String, pass: String, url: String) : DataFrame = {
    var df = getDfReader(sparkSession, sql, driver, user, pass, url)
      .option("dbtable", sql).load()
    return df
  }

  def getQueryDf(sparkSession: SparkSession,sql: String, driver: String, user: String, pass: String, url: String) : DataFrame = {
    var df = getDfReader(sparkSession, sql, driver, user, pass, url)
      .option("dbtable", "(" + sql +") T").load()
    return df
  }
}
