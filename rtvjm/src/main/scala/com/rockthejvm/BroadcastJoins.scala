package com.rockthejvm
import org.apache.spark.sql.functions.broadcast
import org.apache.spark.sql.{Row, SparkSession}
import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType}

case class Data(id: Int, memo: String)

object BroadcastJoins {

  def replicateList[T](list: List[T], n: Int) : List[T] = {
    (1 to n).map(_ => list).flatMap(x => x).toList
  }

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

  import spark.implicits._
  // 'huge' dataset

  val large_table = spark.range(1, 100000000)
  val rows = spark.sparkContext.parallelize(
    replicateList(
    List(
      Row(1, "gold"),
      Row(2, "silver"),
      Row(3, "bronze")
    ), 2
    )
  )
  val schema = StructType(
    Array(
      StructField("id", IntegerType),
      StructField("memo", StringType)
    )
  )

  val df_small_table = spark.createDataFrame(rows, schema)

  val joined = large_table.join(broadcast(df_small_table), "id")

  def main(args: Array[String]): Unit = {
    val (count : Long, time: Long) =  timer {
      joined.count()
    }

    println(s"count: $count, time(sec): $time")
    val (_ , time_taken: Long) = timer {
      large_table.foreach(l => {})
    }
    println(s"foreach:$time_taken")

    def noop(id: Int) : Unit = {}

    val (_, time_to_cvt1: Long) = timer {
      val df_small_dataset = df_small_table.as[Data]
      df_small_dataset.foreach(d => { (1 to 10).foreach(_ => noop(d.id))})
    }
    println(s"convert:$time_to_cvt1")

    val (_, time_to_cvt2: Long) = timer {
      df_small_table.foreach(d =>  { (1 to 10).foreach(_ => noop(d.getInt(0)))})
    }
    println(s"convert:$time_to_cvt2")

  }
}
