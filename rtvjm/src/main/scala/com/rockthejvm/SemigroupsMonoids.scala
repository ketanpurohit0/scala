package main.scala.com.rockthejvm

object SemigroupsMonoids extends App {

  // type + a combination

  trait Semigroup[T] {
    def combine(a: T, b: T) : T
  }

  object Semigroup {
    def apply[T](implicit instance: Semigroup[T]): Semigroup[T] = instance
  }

  object SemigroupInstance {
    implicit val intSemiGroup: Semigroup[Int] = new Semigroup[Int] {
      override def combine(a: Int, b: Int) = a + b
    }

    implicit val stringSemiGroup: Semigroup[String] = new Semigroup[String] {
      override def combine(a: String, b: String): String = a + b
    }
  }

  import SemigroupInstance._

  val naturalIntSemigroup = Semigroup[Int]
  val naturalStringSemigroup = Semigroup[String]
  val meaningOfLife = naturalIntSemigroup.combine(2, 40)
  val favLanguage = naturalStringSemigroup.combine("Sca", "la")
  println(meaningOfLife, favLanguage)

  def reduceInts(list: List[Int]): Int = list.reduce(_ + _)
  def reduceStrings(list: List[String]): String = list.reduce(_ + _)
  def reduceThings[T](list: List[T])(implicit semigroup: Semigroup[T]): T = list.reduce(semigroup.combine)

  val reducedThingsOfInt = reduceThings(List(2, 40))
  val reducedThingsOfString = reduceThings(List("Sca", "la"))
  println(reducedThingsOfInt, reducedThingsOfString)

//  object SemigroupSyntaxScala3 {
//    extension [T](a: T)
//    def |+|(b: T)(implicit semigroup: Semigroup[T]): T = semigroup.combine(a, b)
//  }

  object SemigroupSyntaxScala2 {
    implicit class Extension[T](a: T) {
      def |+|(b: T)(implicit semigroup: Semigroup[T]): T = semigroup.combine(a, b)
    }
  }

  import SemigroupSyntaxScala2._
  val extensionJoinInts = 2 |+| 40
  val extensionJointring = "Sca" |+| "la"
  println(extensionJoinInts, extensionJointring)

  def reduceCompact[T: Semigroup](list: List[T]): T = list.reduce(_ |+| _)
  val reduceCompactInts = reduceCompact(List(2, 40))
  val reduceCompactStrings = reduceCompact(List("Sca", "la"))
  println(reduceCompactInts, reduceCompactStrings)

  trait Monoid[T] extends Semigroup[T] {
    def empty: T
  }

  object MonoidInstances {
    implicit val IntMonoid : Monoid[Int] = new Monoid[Int] {
      override def combine(a: Int, b: Int) = a + b
      override def empty: Int = 0
    }

    implicit val StringMonoid : Monoid[String] = new Monoid[String] {
      override def combine(a: String, b: String) = a + b
      override def empty: String = ""
    }
  }

  import MonoidInstances._

  val reducedThingsOfIntWithMonoid = reduceThings(List(2, 40))
  val reducedThingsOfStringWithMonoid = reduceThings(List("Sca", "la"))
  println(reducedThingsOfIntWithMonoid, reducedThingsOfStringWithMonoid)

}
