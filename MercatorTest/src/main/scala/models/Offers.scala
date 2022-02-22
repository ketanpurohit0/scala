package models

object Offers {
  // Map["name of product" -> (for_every_group_of_M, pay_N)
  val offers = Map(
    Apple.readableName -> (2, 1),
    Orange.readableName -> (3, 2),
    Bananas.readableName -> (2, 1)
  )

  val ensembleOffers = Map(
    (Bananas.readableName, Apple.readableName) -> 1
  )

  def hasEnsembleOffer(productOne: String, productTwo: String): Boolean = {
    ensembleOffers.contains((productOne, productTwo)) || ensembleOffers.contains((productTwo, productOne))
  }

}
