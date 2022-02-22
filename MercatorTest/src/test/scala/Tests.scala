import models.{Apple, Orange, Prices, ShoppingCart, ShoppingItem}
import org.scalatest.funsuite.AnyFunSuite

class Tests extends AnyFunSuite {

  test("shoppingCart") {

    // (nApples, nOranges, offerWillApply)
    val tests =
      Seq((3, 1, true), (1, 1, false), (2, 2, true), (0, 0, false), (0, 1, false), (1, 0, false), (7, 2, true))

    tests.foreach {
      case (nApples, nOranges, offerWillApply) => {
        val shoppingItems = List.fill(nApples)(Apple()) ++ List.fill(nOranges)(Orange())
        val shoppingCart = new ShoppingCart()

        shoppingItems.foreach(i => shoppingCart.addItem(i))

        val result = shoppingCart.tillUpOffers()
        val expected =
          (nApples * Prices.prices(Apple.readableName) + nOranges * Prices.prices(Orange.readableName)) / 100.0
        if (offerWillApply) {
          assert(result < expected)
        } else {
          assert(result == expected)
        }

      }
    }

  }

  ignore("simpleOffers") {

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

  ignore("offerLogic") {
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
