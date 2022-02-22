import models.{Apple, Bananas, Offers, Orange, Prices, ShoppingCart, ShoppingItem}
import org.scalatest.funsuite.AnyFunSuite

class Tests extends AnyFunSuite {

  test("shoppingCartWithSimpleOffers") {

    // (nApples, nOranges, nBananas, offerWillApply)
    val tests =
      Seq(
        (3, 1, 1, true),
        (1, 1, 1, false),
        (2, 2, 1, true),
        (0, 0, 1, false),
        (0, 1, 1, false),
        (1, 0, 1, false),
        (7, 2, 1, true)
      )

    tests.foreach {
      case (nApples, nOranges, nBananas, offerWillApply) => {
        val shoppingItems =
          List.fill(nApples)(Apple()) ++ List.fill(nOranges)(Orange()) ++ List.fill(nBananas)(Bananas())
        val shoppingCart = new ShoppingCart()

        shoppingItems.foreach(i => shoppingCart.addItem(i))

        val result = shoppingCart.tillUpOffers()
        val expected =
          (nApples * Prices.prices(Apple.readableName) + nOranges * Prices.prices(
            Orange.readableName
          ) + nBananas * Prices.prices(Bananas.readableName)) / 100.0
        if (offerWillApply) {
          assert(result < expected)
        } else {
          assert(result == expected)
        }

      }
    }

  }

  test("shoppingCartWithEnsembleOffers") {
    // (nApples, nOranges, nBananas, ensembleWillApply)
    val tests =
      Seq(
        (3, 1, 1, true),
        (1, 1, 1, true),
        (2, 2, 1, true),
        (0, 0, 1, false),
        (0, 1, 1, false),
        (1, 0, 1, true),
        (7, 2, 1, true)
      )

    tests.foreach {
      case (nApples, nOranges, nBananas, offerWillApply) => {
        val shoppingItems =
          List.fill(nApples)(Apple()) ++ List.fill(nOranges)(Orange()) ++ List.fill(nBananas)(Bananas())
        val shoppingCart = new ShoppingCart()

        shoppingItems.foreach(i => shoppingCart.addItem(i))

        val result = shoppingCart.tillUpOffersAndEnsembleOffers()
        val expected =
          (nApples * Prices.prices(Apple.readableName) + nOranges * Prices.prices(
            Orange.readableName
          ) + nBananas * Prices.prices(Bananas.readableName)) / 100.0
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

  ignore("ensembleOffer") {

    // product1, product2, expected To have ensemble offer
    val tests = Seq(
      (Bananas.readableName, Apple.readableName, true),
      (Apple.readableName, Orange.readableName, false),
      (Apple.readableName, Bananas.readableName, true)
    )

    tests.foreach({
      case (product1, product2, expectation) => {
        val result = Offers.hasEnsembleOffer(product1, product2)
        assert(result == expectation)

      }
    })
  }

  ignore("ensembleOffer2") {
    val tests = Seq(
      List(Bananas, Apple, Orange).map(_.readableName),
      List(Apple, Orange).map(_.readableName),
      List(Apple, Bananas).map(_.readableName)
    )

    tests.foreach(items => {
      println("*")
      items.combinations(2).foreach({ c => println(c, Offers.hasEnsembleOffer(c(0), c(1))) })
      val combos = items.combinations(2).foreach { c =>
        println(c)
        println(Offers.hasEnsembleOffer(c(0), c(1)))
        println(Prices.cheaperOfTwoProducts(c(0), c(1)))

      }
    })
  }
}
