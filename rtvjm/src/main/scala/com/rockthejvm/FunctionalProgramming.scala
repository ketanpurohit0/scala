package main.scala.com.rockthejvm

object FunctionalProgramming extends App {
  class Person(name: String) {
    def apply(age:Int): Unit = println(s"I have aged ${age} years")
  }

  val bob = new Person("Bob")
  bob.apply(43)
  bob(43)

  val simpleIncrementer = new Function1[Int, Int] {
    override def apply(arg: Int): Int = arg + 1
  }

  simpleIncrementer.apply(23)
  simpleIncrementer(23)

  val stringConcatenator = new Function2[String, String, String] {
    override def apply(arg1: String, arg2: String): String = arg1 + arg2
  }
  stringConcatenator("I love", " Scala")

  val doubler: Int => Int = (x: Int) => 2 * x
  doubler(4)

  val aMappedList = List(1,2,3).map(x => x + 1)
  val aFlatMappedList = List(1,2,3).flatMap(x => List(x, 2*x))
  val aFilteredList = List(1,2,3,4,5).filter(_ <= 3)

  val allPairs = List(1,2,3).flatMap(number => List("A","B","C").map(letter => s"${number}-${letter}"))

  val alternativePairs = for {
    number <- List(1,2,3)
    letter <- List("A","B","C")
  } yield s"${number}-{$letter}"

  val aList = List(1,2,3,4,5)
  val firstElement = aList.head
  val rest = aList.tail
  val aPrependedList = 0 :: aList
  val anExtendedList =  0 +: aList :+ 6

  val aSequence: Seq[Int] = Seq(1,2,3)
  val accessElement = aSequence(1)

  val aVector = Vector(1,2,3,4,5)
  val accessElementVector = aVector(1)

  val aSet = Set(1,2,3,4,1,2,3)
  val setHas5 = aSet.contains(5)
  val anAddedSet = aSet + 5
  val aRemoveSet = aSet - 3

  val aRange = 1 to 1000
  val twoByTwo = aRange.map(2*_).toList

  val aTuple = ("Bon Jovi", "Rock", 1982)

  val aMap = Map[String, Int]("Daniel"-> 31312, "Jane" -> 4532)

}
