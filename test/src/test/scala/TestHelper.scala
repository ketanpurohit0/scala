package com.kkp.Unt

import org.scalatest.funsuite.AnyFunSuite
import org.apache.spark.sql.functions._

case class Foo(a:String)

class TestHelper extends  AnyFunSuite {
  test("BigTest") {
    assert(1==1)
  }
  
  test("SparkHelperTest") {
    val spark = Helper.getSparkSession("local", "test")
    import spark.implicits._
    val seqFoo = Seq[Foo](Foo("Foo"),Foo("Bar") )
    //val df = seqFoo.toDF("col")
    //df.show()
    val df1 = seqFoo.toDF()
    val df2 = seqFoo.toDS()
    val df3 = df1.alias("df1").join(df2.alias("df2"), expr("df1.a = df2.a"))
    assert(df1.count == seqFoo.length)
    assert(df2.count()== seqFoo.length)
    assert(df3.count() == seqFoo.length)
  }
}
