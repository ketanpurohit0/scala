import org.scalatest.funsuite.AnyFunSuite

class ch41 extends AnyFunSuite{
  case class Person(id: String, name:Name, creditCards:Seq[String])

  case class Name(firstName: String, ni: String, lastName: String)

  def create() : Person = {
    val p = {
      Person("id123", Name("Ketan", "NH65", "Purohit"), Seq[String]())
    }
    p
  }

  def creditCards(cards : Seq[String], from:String, to:String): Seq[String]  = cards.foldLeft(Seq[String]()) { (r,s) =>
    s match {
      case s if s == from => r :+ to
      case _ => r :+ s
    }
  }

  test("create") {
    val p = create()
    val p2 = p.copy(id = p.id, name = p.name.copy("K"))
    val p3 = p.copy(name = p.name.copy(firstName = "K"))
    assert(p2 != p)
    assert(p2 == p3)
    val p4 = p.copy(creditCards = Seq[String]("Aa","Bb"))
    assert(p4.creditCards.length == 2)
  }

  test("creditCards") {
    val cards = Seq("AA","BB")
    val cards2 = creditCards(cards, "AA","CC")
    assert(cards2.length == 2)
    assert(cards2.filter(s => s == "AA").length == 0)
    assert(cards2.filter(s => s == "CC").length == 1)
  }
}
