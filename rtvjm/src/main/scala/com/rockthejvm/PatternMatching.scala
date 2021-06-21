package main.scala.com.rockthejvm

object PatternMatching extends App {
  val anInteger = 55
  val order = anInteger match {
    case 1 => "first"
    case 2 => "second"
    case _ => anInteger + "th"
  }

  case class Person(name:String, age:Int)

  val bob = new Person("Bob", 43)

  val personGreeting = bob match {
    case Person(n, a) => s"Hi, my name is $n and I'm $a years old"
    case _ => "Something else"
  }

  val aTuple = ("Bon Jovi", "Rock")
  val bandDescriptio = aTuple match {
    case (band, genre) => s"$band belongs to genre $genre"
    case _ => "I don't know what you're talking about"
  }

  val aList = List(1,2,3)
  val listDescription = aList match {
    case List(_,2,_) => "List contain 2 in 2nd position"
    case _ => "unknown list"
  }


}
