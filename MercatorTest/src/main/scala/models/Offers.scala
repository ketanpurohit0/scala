package models

object Offers {
  // Map["name of product" -> (for_every_group_of_M, pay_N)
  val offers = Map(
    Apple.readableName -> (2, 1),
    Orange.readableName -> (3, 2)
  )
}
