import models.{Apple, Orange, ShoppingCart, ShoppingItem}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.must.Matchers.{be, equal}
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper

class Tests extends AnyFunSuite {

  test("shoppingCart") {

    val tests = Seq((3, 1), (1, 1), (2, 2), (0, 0), (0, 1), (1, 0), (7, 2))

    tests.foreach {
      case (nApples, nOranges) => {
        val shoppingItems = List.fill(nApples)(Apple()) ++ List.fill(nOranges)(Orange())
        val shoppingCart = new ShoppingCart()

        shoppingItems.foreach(i => shoppingCart.addItem(i))

        val result = shoppingCart.tillUp()
        val expected = (nApples * Apple.costPerUnitInPence + nOranges * Orange.costPerUnitInPence) / 100.0
        assert(result == expected)

      }
    }

  }

  test("simpleOffers") {
    val shoppingItems = List.fill(6)(Apple()) ++ List.fill(4)(Orange())
    val x = shoppingItems.groupBy(si => si.readableName).map(y => y._1 -> y._2.count(_ => true))
    println(x)
  }
}
