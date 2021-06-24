package main.scala.com.rockthejvm

object ValueClasses extends App {
  case class Product(code: String, desc: String)

  trait Backend {
    def findByCode(code: String): Option[Product]
    def findByDesc(desc: String): List[Product]
  }

  val aCode = "1-12345-123456"
  val aDescription = "Foam mattress"

  val aBackend = new Backend {
    override def findByCode(code: String): Option[Product] = ???

    override def findByDesc(desc: String): List[Product] = ???
  }

  //aBackend.findByCode(aCode)
  //aBackend.findByDesc(aCode)

  // solve the problem of preventing "wrong value"

  // 1- use case class
  case class BarCode(code:String)
  object BarCode {
    def apply(code: String) :Either[String, BarCode] = Either.cond(
      code.matches("\\d-\\d{5}\\d{5}"),
      new BarCode(code),
      "Code is invalid"
    )
  }
  case class Description(txt: String)
  trait Backendv2 {
    def findByCode(code: BarCode) : Option[Product]
    def findByDesc(desc: Description): List[Product]
  }

  val aBackend2 = new Backendv2 {
    override def findByCode(code: BarCode): Option[Product] = ???

    override def findByDesc(desc: Description): List[Product] = ???
  }
// Does not complile after BarCode apply is written
//  aBackend2.findByCode(BarCode(aCode))

  // 2 - values class
  // no run-time overhead
  // only 1 val contructor argument
  // cannot be extended
  // only have methods
  // no other vals just defs
  case class BarCodeVC(val code: String) extends AnyVal {
    def countryCode: Char = code.charAt(0)
  }

  def show[T](args: T) : String  = args.toString
  println(show(BarCodeVC("1-12345-12345")))

  val barCodes = Array[BarCodeVC](BarCodeVC("1-12345-12345"))

  //
  BarCodeVC("1-12345-123456") match {
    case BarCodeVC(code) => println(code)
  }
}
