package com.rockthejvm
import org.apache.spark.sql.functions.broadcast
import org.apache.spark.sql.{Row, SparkSession}
import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType}
object BroadcastJoins {

  def timer[R](block: => R): (R, Long) = {
    val s = System.nanoTime()
    val r = block
    val e = System.nanoTime()
    (r, (e-s)/1000000)
  }

  val spark = SparkSession.builder()
    .appName("Broadcast Joins")
    .master("local")
    .getOrCreate()

  // 'huge' dataset

  val large_table = spark.range(1, 100000000)
  val rows = spark.sparkContext.parallelize(
    List(
      Row(1, "gold"),
      Row(2, "silver"),
      Row(3, "bronze")
    )
  )
  val schema = StructType(
    Array(
      StructField("id", IntegerType),
      StructField("memo", StringType)
    )
  )

  val df_small_table = spark.createDataFrame(rows, schema)

  val joined = large_table.join((df_small_table), "id")

  def main(args: Array[String]): Unit = {
    val (count : Long, time: Long) =  timer {
      joined.count()
    }

    println(s"count: $count, time(sec): $time")
  }
}
