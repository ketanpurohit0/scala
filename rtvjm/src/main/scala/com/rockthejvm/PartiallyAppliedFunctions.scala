package com.rockthejvm

object PartiallyAppliedFunctions extends App {

  def thisisAMethod(x: Int) : Int = x + 1
  this.thisisAMethod(5)

  val thisisaFunction = (x:Int) => x + 1
  thisisaFunction(2)

  val thisisalsoaFunction = new Function[Int, Int] {
    override def apply(v1: Int): Int = v1 + 1
  }

  thisisalsoaFunction(2)

  val f =  thisisAMethod _ // eta expansion
  f(2)

  val f2: Int => Int = thisisAMethod // eta exansion

  List(1,2,3).map(f2)
  List(1,2,3).map(f)
  List(1,2,3).map(thisisaFunction)
  List(1,2,3).map(thisisAMethod)

  // Partially applied function

  def multiArgAdder(x: Int)(y:Int) = x + y
  def add2 = multiArgAdder(2) _
  val addit = multiArgAdder(2) _
  def adder(x: Int, y:Int) = x+y
  val adderF = adder _

  println(multiArgAdder(1)(2))
  println(add2(3))
  println(addit(3))
  println(List(1,2,3).map(multiArgAdder(4)))

  def multiArgAdder3(x:Int)(y:Int)(z:Int) = x+y+z
  val twoArgsRemaining = multiArgAdder3(4) _ // we get curried on y, z
  val oneArgRemaining = twoArgsRemaining(3) _
  val oneArgRemaining2 = multiArgAdder3(4)(3) _
}
