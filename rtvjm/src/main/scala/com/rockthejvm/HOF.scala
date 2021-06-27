package com.rockthejvm

object HOFsForOOP extends  App{
  class Applicable {
    def apply(x: Int)  = x+1
  }

  val applicable = new Applicable

  assert (applicable.apply(3) == 4)
  assert (applicable(3) == 4)

  val incrementer = new Function[Int, Int] {
    override def apply(v1: Int): Int = v1 + 1
  }

  incrementer.apply(3) == 4
  incrementer(3) == 4

  val incrementerAlt = (x: Int)  => {x+1}
  incrementerAlt(2) == 3
  incrementerAlt.apply(3) == 3

  def ntimes(f: Int => Int, n: Int) : Int => Int = {
    if (n <= 0) {
      (x: Int) => x
    } else {
      (x: Int) => ntimes(f, n - 1)(f(x))
    }

  }

  val r = ntimes((x:Int) => x+1, 4)

  println(r, r(11))
}
