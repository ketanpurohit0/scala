package main.scala.com.rockthejvm

object Mutability extends App {
  // - mutable non-mutable
  val nonMutable = 5
  var mutable = 5
  mutable = 6

  // mutate via getters,setters
  class Person(private var n: String, private var a: Int) {
    var nAccessToAge  = 0

    def age : Int = {
      nAccessToAge += 1
      this.a
    }

    def age_=(newAge: Int) : Unit = {
      println(s"Person $n is changing age from $a to $newAge")
      this.a = newAge
    }

    def apply(index : Int) = {
      index match {
        case 0 => this.n
        case 1 => this.a
        case _ => throw new IndexOutOfBoundsException
      }
    }

    def update(index: Int, a: Any) : Unit = {
      index match {
        case 0 => this.n = a.asInstanceOf[String]
        case 1 => age = a.asInstanceOf[Int]
        case _ => throw new IndexOutOfBoundsException
      }
    }
  }

  val alice = new Person("Alice", 26)
  val alicesAge = alice.age
  alice.age = 25

  val bob = new Person("Bob", 23)
  println("name", bob(0))
  println("age", bob(1))
  bob(1) = 24
  println("age", bob(1))
}
