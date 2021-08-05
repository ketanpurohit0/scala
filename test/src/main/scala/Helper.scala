package com.kkp.Unt
import org.apache.spark.sql.functions.{col, lit}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{DataFrame, SparkSession}

import java.sql.Connection
import java.sql.{DriverManager, ResultSet}
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

  def unionWithDefault(left: DataFrame, right: DataFrame) : DataFrame = {
    val left_cols = left.columns
    val right_cols = right.columns

    val common_cols = left_cols.intersect(right_cols).map(col(_))
    val left_df_orig_cols = left_cols.diff(right_cols).map(left(_))
    val left_df_new_cols = right_cols.diff(left_cols).map(c => (lit(null).cast(right.schema(c).dataType).as(c)))
    val left_df_with_newcols = left.select((common_cols ++ left_df_orig_cols ++ left_df_new_cols):_*)

    val right_df_orig_cols = right_cols.diff(left_cols).map(right(_))
    val right_df_new_cols = left_cols.diff(right_cols).map(c => (lit(null).cast(left.schema(c).dataType).as(c)))
    val right_df_with_newcols = right.select((common_cols ++ right_df_orig_cols ++ right_df_new_cols):_*)

    left_df_with_newcols.unionByName(right_df_with_newcols)

  }

  def mapper(inputDf: DataFrame, mapDf: DataFrame, inputDfKeyCols: Seq[String], mapDfKeyCols: Seq[String], mapDfValueCols: String*) : DataFrame  = {
    // make sure the number of keys as of equal length
    assert(inputDfKeyCols.size == mapDfKeyCols.size)
    // make sure every specified key actually exists in relevant df
    assert((inputDfKeyCols.diff(inputDf.columns).size == 0) && (mapDfKeyCols.diff(mapDf.columns).size == 0))
    // make sure schema matches between keys for joining
    inputDfKeyCols.zip(mapDfKeyCols).foreach( k => assert(inputDf.schema(k._1).dataType == mapDf.schema(k._2).dataType))
    // make sure all target fields exist
    assert(mapDfValueCols.diff(mapDf.columns).size == 0)
    
  }

}
