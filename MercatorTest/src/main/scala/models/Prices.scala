package models

object Prices {
  // Map["name of product" -> (for_every_group_of_M, pay_N)
  val prices = Map(
    Apple.readableName -> 60,
    Orange.readableName -> 25,
    Bananas.readableName -> 20
  )

  def cheaperOfTwoProducts(productOne: String, productTwo: String): String = {
    val p1 = prices.getOrElse(productOne, Int.MaxValue)
    val p2 = prices.getOrElse(productTwo, Int.MaxValue)
    if (p1 <= p2) {
      productOne
    } else {
      productTwo
    }
  }

}
