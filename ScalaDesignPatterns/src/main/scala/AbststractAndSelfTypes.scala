trait Adder {
  def sum[T](a: T, b: T)(implicit numeric: Numeric[T]): T = {
    numeric.plus(a, b)
  }
}

class Container[T](data: T) {
  def compare(other: T) = data.equals(other)
}

// abstract type
trait ContainerAT {
  type T
  val data: T
  def compare(other: T) = data.equals(other)
}

class StringContainer(val data: String) extends ContainerAT {
  override type T = String
}

// Abstract types
abstract class PrintData
abstract class PrintMaterial
abstract class PrintMedia
trait Printer {
  type Data <: PrintData
  type Material <: PrintMaterial
  type Media <: PrintMedia
  def print(data: Data, material: Material, media: Media) = println(s"Printing $data with $material on $media")
}
case class Text() extends PrintData
case class Model() extends PrintData
case class Toner() extends PrintMaterial
case class Plastic() extends PrintMaterial
case class Paper() extends PrintMedia
case class Air() extends PrintMedia

class LaserPrinter extends Printer {
  type Data = Text
  type Material = Toner
  type Media = Paper
}

class ThreeDPrinter extends Printer {
  type Data = Model
  type Material = Plastic
  type Media = Air
}

trait GenericPrinter[Data <: PrintData, Material <: PrintMaterial, Media <: PrintMedia] {
  def print(data: Data, material: Material, media: Media) = println(s"Printing $data with $material on $media")
}

class GenericLaserPrinter[Data <: Text, Material <: Toner, Media <: Paper] extends GenericPrinter[Data, Material, Media]
class Generic3DPrinter[Data <: Model, Material <: Plastic, Media <: Air] extends GenericPrinter[Data, Material, Media]

object GenericsApp extends Adder with App {
  println(sum(1, 4))
  println(sum(3.0, 3))
  val container = new Container(11)
  val container2 = new Container("freedom")
  val container3 = new StringContainer("freedom")
  container3.compare("freedom")
}
object AbstractTypeApp extends App {
  val laserPrinter = new LaserPrinter
  val threeDPrinter = new ThreeDPrinter
  laserPrinter.print(Text(), Toner(), Paper())
  threeDPrinter.print(Model(), Plastic(), Air())
}

object GenericTypeApp extends App {
  val laserPrinter = new GenericLaserPrinter[Text, Toner, Paper]
  val threeDPrinter = new Generic3DPrinter[Model, Plastic, Air]
  laserPrinter.print(Text(), Toner(), Paper())
  threeDPrinter.print(Model(), Plastic(), Air())

  // mistake
  class GenericPrinterImpl[Data <: PrintData, Material <: PrintMaterial, Media <: PrintMedia]
      extends GenericPrinter[Data, Material, Media]

  val wrongPrinter = new GenericPrinterImpl[Model, Toner, Air]
  wrongPrinter.print(Model(), Toner(), Air())
}

// Polymorphism

// Subtype polymorphism
abstract class Item {
  def pack: String
}

class Fruit extends Item {
  override def pack: String = "Fruit/Bag"
}

class Drink extends Item {
  override def pack: String = "Drink/Bottle"
}

object SubTypePolyApp extends App {
  val shoppingBasket = List(new Fruit(), new Drink())
  shoppingBasket.foreach(item => { println(item.pack) })
}

// ad-hoc polymorphism
trait Adder2[T] {
  def sum(a: T, b: T): T
}
object Adder2 {
  def sum[T: Adder2](a: T, b: T): T = implicitly[Adder2[T]].sum(a, b)

  implicit val intadder: Adder2[Int] = new Adder2[Int] {
    override def sum(a: Int, b: Int): Int = a + b
  }

  implicit val stradder: Adder2[String] = new Adder2[String] {
    override def sum(a: String, b: String): String = a + b
  }

//  implicit val dbladder: Adder2[Double] = new Adder2[Double] {
//    override def sum(a: Double, b: Double): Double = a + b
//  }

  implicit def nmcadder[T: Numeric]: Adder2[T] = new Adder2[T] {
    override def sum(a: T, b: T): T = implicitly[Numeric[T]].plus(a, b)
  }
}

object AddHocPolymorphism extends App {
  import Adder2._
  println(s"Sum is ${sum(1, 2)}")
  println(sum("ab", "cd"))
  println(sum(1.0, 2.0))
}

//self-types
