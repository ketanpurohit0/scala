package models

trait ShoppingItem {
  def costPerUnitInPence: Int
  def readableName: String
}

case class Apple() extends ShoppingItem {
  override def costPerUnitInPence: Int = Apple.costPerUnitInPence
  override def readableName: String = Apple.readableName
}

object Apple extends ShoppingItem {
  override def costPerUnitInPence: Int = 60
  override def readableName = "Apple"

  def apply(): Apple = new Apple()
}

case class Orange() extends ShoppingItem {
  override def costPerUnitInPence: Int = Orange.costPerUnitInPence
  override def readableName: String = Orange.readableName
}

object Orange extends ShoppingItem {
  override def costPerUnitInPence: Int = 25
  override def readableName = "Orange"

  def apply(): Orange = new Orange()
}
