package models

class ShoppingCart {
  private var shoppingItems = List[ShoppingItem]()

  def addItem(shoppingItem: ShoppingItem): Unit = {
    shoppingItems = shoppingItems :+ shoppingItem
  }

  def tillUp(): Double = {
    val totalSumInPence = shoppingItems.map(f => f.costPerUnitInPence).sum
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
