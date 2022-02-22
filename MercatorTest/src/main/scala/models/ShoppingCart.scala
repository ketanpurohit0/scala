package models

import scala.collection.mutable

class ShoppingCart {
  private var shoppingItems = List[ShoppingItem]()

  def addItem(shoppingItem: ShoppingItem): Unit = {
    shoppingItems = shoppingItems :+ shoppingItem
  }

  def tillUp(): Double = {
    val totalSumInPence = shoppingItems.map(f => Prices.prices(f.readableName)).sum
    totalSumInPence / 100.0
  }

  def tillUpOffers(): Double = {
    val groupByNameAndCount = shoppingItems
      .groupBy(si => si.readableName)
      .map({ case (readableName, collection) => readableName -> collection.count(_ => true) })

    val namedEffectiveQuantities = groupByNameAndCount.map({ case (readableName, quantity) =>
      readableName -> effectiveQuantity(quantity, Offers.offers.getOrElse(readableName, (1, 1)))
    })

    val totalSumInPence = namedEffectiveQuantities
      .map({ case (readableName, quantity) => quantity * Prices.prices(readableName) })
      .sum

    totalSumInPence / 100.0

  }

  def tillUpOffersAndEnsembleOffers(): Double = {
    val groupByNameAndCount = shoppingItems
      .groupBy(si => si.readableName)
      .map({ case (readableName, collection) => readableName -> collection.count(_ => true) })

    val namedEffectiveQuantities = groupByNameAndCount.map({ case (readableName, quantity) =>
      readableName -> effectiveQuantity(quantity, Offers.offers.getOrElse(readableName, (1, 1)))
    })

    val mutableMap = mutable.Map[String, Int](namedEffectiveQuantities.toSeq: _*)

    // deal with ensemble offers, pick combinations of 2 items each
    // NOT clear if just one item is free (we can adjust logic)
    // NOT clear if only one free discount can be applied
    mutableMap.keys.toList
      .combinations(2)
      .foreach(combination => {
        if (Offers.hasEnsembleOffer(combination(0), combination(1))) {
          // This combination appears to have a ensemble offer, so which one is cheaper
          val cheaperProduct = Prices.cheaperOfTwoProducts(combination(0), combination(1))
          println(s"zero down $cheaperProduct")
          mutableMap(cheaperProduct) = 0
        }
      })

    println(mutableMap)

    val totalSumInPence = mutableMap
      .map({ case (readableName, quantity) => quantity * Prices.prices(readableName) })
      .sum

    totalSumInPence / 100.0

  }

  private def effectiveQuantity(quantity: Int, offer: (Int, Int)): Int = {

    offer match {
      case (forM, payN) => {
        val remainderQty = quantity % forM
        val groupsOfM = quantity / forM
        groupsOfM * payN + remainderQty
      }
    }

  }
}
