package com.kkp.Unt
import org.apache.spark.{SparkConf,SparkContext}
import org.apache.spark.sql.SparkSession
import java.sql.Connection
import java.sql.{DriverManager,ResultSet}
import java.sql.Driver
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

  def getPostGreUrl(db: String, user :String, secret: String) : String = {
    return s"jdbc:postgresql://localhost/${db}?user=${user}&password=${secret}"
  }

  def getPostGreUrlFromConfig() : String = {
    import com.typesafe.config._
    val config = ConfigFactory.load()
    val driver = config.getString("jdbc.driver")
    val url = config.getString("jdbc.url")
    val username = config.getString("jdbc.username")
    val password = config.getString("jdbc.password")
    val database = config.getString("jdbc.database")

    return s"${url}${database}?user=${username}&password=${password}"
  }

  def drivers() : Unit = {
    val dr = DriverManager.getDrivers()
    while (dr.hasMoreElements()){
      val d= dr.nextElement()
      println(d.toString())
    }
    
  }

  def getJdbc(db: String, user: String, secret : String) : Connection = {
    return DriverManager.getConnection(getPostGreUrl(db, user, secret))
  }

  def getJdbcFromConfig() : Connection = {
    return DriverManager.getConnection(getPostGreUrlFromConfig())
  }

  def select(conn : Connection, sql :String) :ResultSet = {
    val stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)
    return stmt.executeQuery(sql)
  }

}
