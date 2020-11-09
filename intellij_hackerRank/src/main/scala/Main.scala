package com.kkp.Unt

object Main {
def main(args: Array[String]): Unit = {
  def f(n: Int) = {for (i <- 0 until n) println("Hello World")}
  var n = scala.io.StdIn.readInt
  f(n)
}

}
