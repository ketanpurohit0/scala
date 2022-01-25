import org.scalacheck.{Prop, Properties}
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.{convertToAnyShouldWrapper, equal}
import org.scalatest.prop.Configuration.minSuccessful

object Tests extends Properties("Examples") {
  implicit val arbitraryInts: Gen[Int] = Arbitrary.arbitrary[Int]
  implicit val nonZeroInts: Gen[Int] = Arbitrary.arbitrary[Int] suchThat (_ != 0)
  implicit val positiveInts: Gen[Int] = Arbitrary.arbitrary[Int] suchThat (_ > 0)

  property("n") = Prop.forAll({ i: Int => i == i })

  property("list tail") = Prop.forAll { (n: Int, l: List[Int]) =>
    (n :: l).tail == l
  }

  property("n+m") = Prop.forAll { (n: Int, m: Int) =>
    n + m == m + n
  }

  case class Person(
      firstName: String,
      lastName: String,
      age: Int
  ) {
    def isTeenager = age >= 13 && age <= 19
  }

  val genPerson = {
    import org.scalacheck.Gen.{choose, oneOf}
    for {
      firstName <- oneOf("Alan", "Ada", "Alonzo")
      lastName <- oneOf("Lovelace", "Turing", "Church")
      age <- choose(1, 100)
    } yield Person(firstName, lastName, age)
  }
  implicit val carbPerson: Arbitrary[Person] = Arbitrary(genPerson)

  property("ex1") = Prop.forAll { (p: Person) =>
    p.isTeenager == (p.age >= 13 && p.age <= 19)
  }

}
