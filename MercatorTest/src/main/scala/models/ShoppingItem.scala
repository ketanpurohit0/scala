package models

trait ShoppingItem {
  def readableName: String
}

case class Apple() extends ShoppingItem {
  override def readableName: String = Apple.readableName
}

object Apple extends ShoppingItem {
  override def readableName = "Apple"

  def apply(): Apple = new Apple()

}

case class Orange() extends ShoppingItem {
  override def readableName: String = Orange.readableName
}

object Orange extends ShoppingItem {
  override def readableName = "Orange"

  def apply(): Orange = new Orange()

}

case class Bananas() extends ShoppingItem {
  override def readableName: String = Bananas.readableName
}

object Bananas extends ShoppingItem {
  override def readableName: String = "Bananas"
}
