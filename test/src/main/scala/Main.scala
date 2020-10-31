package com.kkp.Unt

object Main {
def main(args: Array[String]): Unit = {
  Helper.printMe("Hello World")
  val spark = Helper.getSparkSession("local", "test")
  import spark.implicits._
  spark.close()
}

}
