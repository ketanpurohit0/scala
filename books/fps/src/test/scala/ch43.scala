import org.scalatest.funsuite.AnyFunSuite

class ch43 extends AnyFunSuite{

  // A quick review of for comprehensions

  private case class Person(firstName: String, lastName : String)

  val strings = Seq[String]("A","B","C")

  private val people = List[Person](
    Person("Ketan", "Purohit"),
    Person("Other", "Purohit")
  )

  def gen(seq: Seq[String]) :Seq[String] = {
    for {
      p <- seq
      n = p
      if (n ==  "A")
    } yield n
  }

  def peopleStartingWith(persons: List[Person], startsWith: String) = {
    for {
      p <- persons
      if p.firstName.startsWith(startsWith)
    } yield p.firstName.toUpperCase()
  }

  test("generator") {
    val f = gen(strings)
    assert(f.length == 1)
  }

  test("peopleStartingWith") {
    assert(peopleStartingWith(people,"K").length == 1)
    assert(peopleStartingWith(people,"K")(0) == "KETAN")
    assert(peopleStartingWith(people,"K").head == "KETAN")
    assert(peopleStartingWith(people,"K").tail == Nil)


  }

}
