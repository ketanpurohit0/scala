import org.scalatest.funsuite.AnyFunSuite
import shapeless.lens
import shapeless.test._

class ch110 extends AnyFunSuite{

  case class User(
                   id: Int,
                   name: Name,
                   billingInfo: BillingInfo,
                   phone: String,
                   email: String
                 )

  case class Name(
                   firstName: String,
                   lastName: String
                 )

  case class Address(
                      street1: String,
                      street2: String,
                      city: String,
                      state: String,
                      zip: String
                    )

  case class CreditCard(
                         name: Name,
                         number: String,
                         month: Int,
                         year: Int,
                         cvv: String
                       )

  case class BillingInfo(
                          creditCards: Seq[CreditCard]
                        )

  val user = User(
    id = 1,
    name = Name(
      firstName = "Al",
      lastName = "Alexander"
    ),
    billingInfo = BillingInfo(
      creditCards = Seq(
        CreditCard(
          name = Name("John", "Doe"),
          number = "1111111111111111",
          month = 3,
          year = 2020,
          cvv = ""
        )
      )
    ),
    phone = "907-555-1212",
    email = "al@al.com"
  )


  test("modifyWithShapeless") {
    val nameLens = lens[User].name
    val firstNameLens = lens[User].name.firstName
    val phoneLens = lens[User].phone
    val emailLens = lens[User].email
    val fpeLens = firstNameLens ~ phoneLens ~ emailLens

    val u = nameLens.get(user)
    val f = firstNameLens.get(user)
    assert(f == "Al")
    assert(u.lastName == "Alexander")
    assert(u.firstName == "Al")

    // modify
    val user2 = firstNameLens.set(user)("Ketan")
    assert(user2.name.firstName == "Ketan")
    val user3 = nameLens.set(user)(Name(firstName = user2.name.firstName, lastName = "Purohit"))
    assert(user3.name.firstName == "Ketan")
    assert(user3.name.lastName == "Purohit")
    typed[User](user3)

    val fpe = fpeLens.get(user3)
    typed[(String, String, String)](fpe)
    assert(fpe == ("Ketan", "907-555-1212", "al@al.com"))
    val user4 = fpeLens.set(user3)("Ketan K", "07943-111-222", "purohit@gmail.com")
    typed[User](user4)
    assert(user4.name.firstName == "Ketan K")
    assert(fpeLens.get(user4)._2 == "07943-111-222")
    assert(fpeLens.get(user4)._3 == "purohit@gmail.com")

  }

}
