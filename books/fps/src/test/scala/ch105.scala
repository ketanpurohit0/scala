import org.scalatest.funsuite.AnyFunSuite

class ch105 extends AnyFunSuite{
  sealed trait CrustSize
    case object SmallCrustSize extends CrustSize
    case object MediumCrustSize extends CrustSize
    case object LargeCrustSize extends CrustSize

  sealed trait CrustType
    case object RegularCrustType extends CrustType
    case object ThinCrustType extends CrustType
    case object ThickCrustType extends CrustType

  sealed trait Topping
    case object Cheese extends Topping
    case object Pepperoni extends Topping
    case object Sausage extends Topping
    case object Mushrooms extends Topping
    case object Onions extends Topping

  case class Address(street1 : String, street2: Option[String], city: String, state: String, zipCode: String)
  case class Customer(name: String, phone: String, address: Address)
  case class Pizza (crustSize: CrustSize, crustType: CrustType, toppings:Seq[Topping])
  case class Order (pizzas: Seq[Pizza], customer: Customer)

  trait PizzaServiceInterface {
    def addTopping(p: Pizza, t: Topping): Pizza
    def removeTopping(p: Pizza, t: Topping): Pizza
    def removeAllToppings(p: Pizza): Pizza

    def updateCrustSize(p: Pizza, c: CrustSize): Pizza
    def updateCrustType(p: Pizza, c: CrustType): Pizza

    def calculatedPizzaPrize(p: Pizza, toppingPrizes: Map[Topping, Float], typePrizes: Map[CrustSize, Float], crustPrizes: Map[CrustType, Float]) : Float

  }

  trait PizzaService extends PizzaServiceInterface{
    def addTopping(p: Pizza, t: Topping): Pizza = {
      val newToppings = p.toppings :+ t
      p.copy(toppings = newToppings)
    }
    def removeTopping(p: Pizza, t: Topping): Pizza = {
      val newToppings = p.toppings.filter(i => i != t)
      p.copy(toppings = newToppings)
    }
    def removeAllToppings(p: Pizza): Pizza = {
      p.copy(toppings = Seq[Topping]())
    }

    def updateCrustSize(p: Pizza, cs: CrustSize): Pizza = {
      p.copy(crustSize = cs)
    }
    def updateCrustType(p: Pizza, ct: CrustType): Pizza = {
      p.copy(crustType = ct)
    }

    def calculatedPizzaPrize(p: Pizza, toppingPrizes: Map[Topping, Float], crustSizePrizes: Map[CrustSize, Float], crustTypePrizes: Map[CrustType, Float]) : Float = {
      val toppingCost = p.toppings.foldLeft[Float](0){(i: Float, p: Topping) => i + toppingPrizes(p)}
      val base = 10
      toppingCost + base + p.toppings.size
    }

  }

  trait PizzaDaoInterface {
    def getToppingPrices(): Map[Topping, Float]
    def getCrustSizePrices(): Map[CrustSize, Float]
    def getCrustTypePrices(): Map[CrustType, Float]
  }

  object MockPizzaDao extends PizzaDaoInterface {
    def getToppingPrices(): Map[Topping, Float] = {
      Map(Cheese -> 1, Pepperoni -> 1, Sausage -> 1, Mushrooms -> 1)
    }
    def getCrustSizePrices(): Map[CrustSize, Float] = {
      Map( SmallCrustSize -> 0, MediumCrustSize -> 1, LargeCrustSize -> 2)
    }
    def getCrustTypePrices(): Map[CrustType, Float] = {
      Map(RegularCrustType -> 0, ThinCrustType -> 1, ThickCrustType -> 2)
    }
  }

  trait  OrderServiceInterface {
    protected def database: PizzaDaoInterface
    def calculateOrderPrice(o: Order) : Float
  }

  trait AbstractOrderService extends OrderServiceInterface {
    object PizzaService extends PizzaService
    import PizzaService.calculatedPizzaPrize

    private lazy val toppingPricesMap = database.getToppingPrices()
    private lazy val crustSizePricesMap = database.getCrustSizePrices()
    private lazy val crustTypePricesMap = database.getCrustTypePrices()

    def calculateOrderPrice(o: Order): Float = {
      calculateOrderPriceInternal(o, toppingPricesMap, crustSizePricesMap, crustTypePricesMap)
    }

    private  def calculateOrderPriceInternal(order: ch105.this.Order, toppingPricesMap: Map[ch105.this.Topping, Float], crustSizePricesMap: Map[ch105.this.CrustSize, Float], crustTypePricesMap: Map[ch105.this.CrustType, Float]) : Float = {
      val pizzaPrices = for {
        pizza <- order.pizzas
      } yield calculatedPizzaPrize(pizza, toppingPricesMap, crustSizePricesMap, crustTypePricesMap )
      pizzaPrices.sum
    }

  }

  object MockDbOrderService extends AbstractOrderService {
    override protected def database: PizzaDaoInterface = MockPizzaDao
  }

  class PizzaServiceProvider extends PizzaService

  test("t") {
    val p = Pizza(MediumCrustSize, RegularCrustType, Seq(Cheese))

    val psp = new PizzaServiceProvider
    val p1 = psp.addTopping(p, Pepperoni)
    val p2 = psp.addTopping(p1, Pepperoni)
    val p3 = psp.updateCrustSize(p2, MediumCrustSize)
    val p4 = psp.updateCrustType(p3, RegularCrustType)
    val price = psp.calculatedPizzaPrize(p4, Map(Pepperoni -> 4, Mushrooms -> 3, Cheese -> 1), Map(MediumCrustSize -> 1), Map(RegularCrustType -> 1, ThinCrustType -> 2))
    assert(price == 22)
  }

  test("b") {
    val o = MainDriver
    o.main(Array[String]())
  }

  object MainDriver  {

    def main(args: Array[String]): Unit = {


      object PizzaService extends PizzaService
      import PizzaService._

      val address = Address(street1 = "1 Main Street", street2 = None, city = "London", state = "UK", zipCode = "NW9")
      val customer = Customer(name = "Ketan", phone = "0794", address = address)
      val order = Order(Seq[Pizza](), customer)
      val p1 = Pizza(crustSize = MediumCrustSize, crustType = RegularCrustType, Seq(Cheese))
      val o1 = order.copy(pizzas = order.pizzas :+ p1)
      val p2 = p1.copy()
      val p2a = addTopping(p2, Pepperoni)
      val p2b = addTopping(p2a, Mushrooms)
      val p2c = updateCrustType(p2b, ThickCrustType)
      val p2Last = updateCrustSize(p2c, LargeCrustSize)
      val o2 = o1.copy(pizzas = o1.pizzas :+ p2Last)

      import MockDbOrderService.calculateOrderPrice
      val price = calculateOrderPrice(o2)
      print(price)
    }
  }
}
