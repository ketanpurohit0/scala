package com.kkp.Unt
import org.apache.spark.{SparkConf,SparkContext}
import org.apache.spark.sql.SparkSession
object Helper {

  def printMe(m :String) : Unit = {
    println(m)
  }

  def getSparkSession(sparkMaster : String, appName : String) : SparkSession = {
    val conf = new SparkConf().setMaster(sparkMaster).set("spark.app.name", appName)
    val sc = new SparkContext(conf)
    val spark = SparkSession.builder().getOrCreate()
    spark.sparkContext.setLogLevel("ERROR")
    spark.conf.set("spark.sql.crossJoin.enabled", true)
    spark.conf.set("spark.sql.caseSensitive", false)
    return spark
  }

}
