import models.{Apple, Orange, ShoppingCart, ShoppingItem}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.must.Matchers.{be, equal}
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper

class Tests extends AnyFunSuite {

  test("shoppingCart") {

    // (nApples, nOranges)
    val tests = Seq((3, 1), (1, 1), (2, 2), (0, 0), (0, 1), (1, 0), (7, 2))

    tests.foreach {
      case (nApples, nOranges) => {
        val shoppingItems = List.fill(nApples)(Apple()) ++ List.fill(nOranges)(Orange())
        val shoppingCart = new ShoppingCart()

        shoppingItems.foreach(i => shoppingCart.addItem(i))

        val result = shoppingCart.tillUp()
        val offerResult = shoppingCart.tillUpOffers()
        val expected = (nApples * Apple.costPerUnitInPence + nOranges * Orange.costPerUnitInPence) / 100.0
        assert(result == expected)
        print(result, offerResult)

      }
    }

  }

  test("simpleOffers") {

    val tests = Seq((3, 1), (1, 1), (2, 2), (0, 0), (0, 1), (1, 0), (7, 2))

    tests.foreach {
      case (nApples, nOranges) => {
        val shoppingItems = List.fill(nApples)(Apple()) ++ List.fill(nOranges)(Orange())
        val groupedByName = shoppingItems
          .groupBy(si => si.readableName)
          .map({ case (readableName, collection) => readableName -> collection.count(_ => true) })

        groupedByName.foreach({ case (s, i) => { println(i, i / 2, i % 2) } })
      }
    }
  }

  test("offerLogic") {
    // M, N, quantity (offer is N for M, aka for every M pay N)
    // a bogof (1 for 1) is represented as 2,1,.
    val tests = Seq((2, 1, 1), (2, 1, 5), (3, 2, 5), (1, 1, 5))
    tests.foreach({
      case (forM, payN, quantityBought) => {
        val effectiveQty = effectiveQuantity(quantityBought, forM, payN)
        println(quantityBought, forM, payN, "->", effectiveQty)
      }
    })

    def effectiveQuantity(quantity: Int, forM: Int, payN: Int) = {

      val remainderQty = quantity % forM
      val groupsOfM = quantity / forM
      groupsOfM * payN + remainderQty

    }
  }
}
