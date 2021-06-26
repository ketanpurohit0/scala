package com.rockthejvm

import org.apache.spark.sql.SparkSession

object RepartitionCoalesce {

  def timer[R](block: => R): (R, Long) = {
    val s = System.nanoTime()
    val r = block
    val e = System.nanoTime()
    (r, (e-s)/1000000)
  }

  val spark = SparkSession.builder()
    .appName("Repartition Coalesce")
    .master("local[*]")
    .getOrCreate()

  val sc = spark.sparkContext

  val numbers = sc.parallelize(1 to 10000000) // 4 on my laptop

  val repartitioned = numbers.coalesce(2)

  def main(args: Array[String]): Unit = {
    val (r, t) = timer {
      repartitioned.count()
    }

    println(repartitioned.partitions.length, r, t)
  }
}
