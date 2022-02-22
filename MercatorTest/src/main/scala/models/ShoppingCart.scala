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
}
