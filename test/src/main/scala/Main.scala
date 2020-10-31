package com.kkp.Unt

object Main {
def main(args: Array[String]): Unit = {
  var x = new Helper()
  x.printMe("Hello World")
  val spark = x.getSparkSession("local", "test")
  import spark.implicits._
  spark.close()
}

}
