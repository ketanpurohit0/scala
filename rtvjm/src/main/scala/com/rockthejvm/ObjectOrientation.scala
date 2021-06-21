package main.scala.com.rockthejvm

object ObjectOrientation extends App {
  class Animal {
    val age: Int = 0
    def eat() = println("I'm eating")
  }

  val anAnimal = new Animal

  class Dog(val name: String) extends Animal {}

  val aDog = new Dog("Lassie")

  aDog.name

  val aDeclaredAnimal = new Dog("Hachi")
  aDeclaredAnimal.eat()

  abstract class WalkingAnimal {
    val hasLegs = true
    def walk(): Unit
  }

  trait Carnivore {
    def eat(animal: Animal): Unit
  }

  trait  Philosopher {
    def ?!(thought: String): Unit
  }

  class Crocodile extends Animal with Carnivore with Philosopher {
    override def eat(animal: Animal): Unit = println("I am eating you, animal")

    override def eat(): Unit = super.eat()

    override def ?!(thought: String): Unit = println(s"I was thinking ${thought}")
  }



  val aCroc = new Crocodile
  aCroc.eat(aDog)
  aCroc eat aDog
  aCroc ?! "What if we could fly"
  aCroc.?!("What if we could fly")

  val basicMath = 1 + 2
  val anotherBasicMath = 1.+(2)

  val dinosaur = new Carnivore {
    override def eat(animal: Animal): Unit = println("I am a dino eating")
  }

  dinosaur.eat(anAnimal)

  object MySingleton {
    val mySpecialValue = 53278
    def mySpecialMethod() = 5327
    def apply(x: Int) = x + 1
  }

  MySingleton.mySpecialMethod()
  MySingleton.apply(65)
  MySingleton(65)

  object Animal {
    val canLiveIndefinitely = false
  }

  val animalsCanLiveForever = Animal.canLiveIndefinitely

  case class Person(name: String, age: Int)

  val bob = Person("Bob", 54)

  try {
    val x: String = null
    x.length
  } catch {
    case e: Exception => "some faulty error message"
  } finally {
  }

  abstract class MyList[T] {
    def head: T
    def tail: MyList[T]
  }

  val aList: List[Int] = List(1,2,3)
  val first = aList.head
  val rest = aList.tail
  val aStringList : List[String] = List("hello", "Scala")
  val firstString = aStringList.head

  val aReversedList = aList.reverse

}
