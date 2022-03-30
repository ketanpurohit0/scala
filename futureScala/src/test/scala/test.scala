package org.kp.futscala

import org.scalatest.funsuite.AnyFunSuite
import org.apache.spark
import org.apache.spark.{
  SparkConf,
  SparkContext
}
import org.apache.spark.sql.SparkSession

class test extends AnyFunSuite {

  def getSparkSession(
      sparkMaster: String,
      appName: String
  ): SparkSession = {
    val conf =
      new SparkConf()
        .setMaster(sparkMaster)
        .set("spark.app.name", appName)
    val sc = new SparkContext(conf)
    val spark = SparkSession
      .builder()
      .getOrCreate()
    spark.sparkContext.setLogLevel(
      "ERROR"
    )
    spark.conf.set(
      "spark.sql.crossJoin.enabled",
      true
    )
    spark.conf.set(
      "spark.sql.caseSensitive",
      false
    )
    spark
  }

  val spark =
    getSparkSession("local", "test")

  test("good_test") {
    assert(1 == 1, "Some problem")
  }

  test("bad_test") {
    assert(1 == 2, "Some problem")
  }

  test("spark") {
    val s = Seq(("a", "b", "c"))
    import spark.implicits._
    val df = s.toDF(
      Seq("col1", "col2", "col3"): _*
    )
    assert(df.count() == 1)
    assert(df.columns.head == "col1")

//    df.write.parquet(
//      raw"C:\parquet_files2"
//    )

    df.write.parquet(
      "file:///parquet_files3"
    )
  }

}
