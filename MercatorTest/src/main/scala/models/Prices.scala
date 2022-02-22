package models

object Prices {
  // Map["name of product" -> (for_every_group_of_M, pay_N)
  val prices = Map(
    Apple.readableName -> 60,
    Orange.readableName -> 25
  )

}
