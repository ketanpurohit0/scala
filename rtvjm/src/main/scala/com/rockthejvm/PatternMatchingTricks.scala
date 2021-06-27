package com.rockthejvm

object PatternMatchingTricks extends App {

  // basic
  case class Person(name:String,age: Int)
  val bob = Person("bob",34)
  bob match {
    case Person(n,a) => s"My $n and I am $a years old"
  }

  // list extractor
  val numberList = List(1,2,3, 42)
  val mustHaveThree = numberList match {
    case List(_,_,3,a) => s"List has 3 in pos 3 and $a"
  }

  // haskell-lie prepending
  val startsWithOne = numberList match {
    case 1 :: tail => "List starts with 1"
    case Nil => "Empty"
  }

  // varargs pattern
  val dontCareAboutTheRest = numberList match {
    case List(_,2,_*) => "I only care 2 in second"
  }

  // other infix pattern
  val mustEndWithMeaningOfList = numberList match {
    case List(1,2,_) :+ 42 => "Last element is a meaning"
    case List(_,_*) :+ 42 => "Last element is a meaning"
  }

  // type matching
  def giveMeaValue(): Any = 45

  giveMeaValue() match {
    case _ : String => "I have a string"
    case _ : Int => "I have a int"
    case _ => "Something else"
  }

  // name binding
  def requestMoreInfo(p: Person)  : String = s"Person ${p.name}"

  val bobsInfo = bob match {
    case p @ Person(n, a) => s"Person $p.name"
  }

  // conditional guards
  val aNumber = 42
  val ordinal2 = aNumber match {
    case 1 => "first"
    case 2 => "second"
    case 3 => "third"
    case n if n % 10 == 1 => n + "st"
    case n if n % 10 == 2 => n + "nd"
    case n if n % 10 == 3 => n + "rd"
    case _ => aNumber + "th"
  }

  // alternative pattens
  val myOptimalList  = numberList match {
    //case List(1,_*) => "I like this list"
    //case List(_,_,3,_*) => "I like this list"
    case List(1, _*)| List(_,_,3,_*) => "I like this list"
    case _ => "I don't like this list"
  }

  println(myOptimalList)
}
