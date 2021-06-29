package com.rockthejvm

object Underscores {

  // to ignore values
  val _ = 5

  val onlyFives = (1 to 10).map(_ => 5)

  // self types

  trait Singer
  trait Actor { //anyone implementing actor must implement singer
    _:  Singer =>
  }

  def lenOfaList(list: List[_]): Int = list.length

  // matching wildcard

  onlyFives match {
    case _ => "I am fine"
  }

  // import
  import scala.collection._

  // default initialisers
  var myStr: String = _  //

  // lambda sugar
  List(1,2,3,4).map(x => x + 5)
  List(1,2,3,4).map(_ * 5)
  val sumUp: (Int, Int) => Int = _ + _

  // ETA expansion
  def methodm(x: Int) = x+ 1
  val functm = methodm _

  // Higher kinded types
  class MyHigherKindedType[M[_]]
  //val myjewel = new MyHigherKindedType[List]()

  // Varargs
  def makeSentense(words: String*) = ???
  makeSentense(List("1","2"):_*)

}
