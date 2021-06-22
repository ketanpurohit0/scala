package main.scala.com.rockthejvm

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}
import scala.concurrent.duration.Duration



object MonadsForBeginners extends App {
  case class SafeValue[+T](private val intervalValue: T) {
    def get: T = synchronized {
      // some interesting activity
      intervalValue
    }

    def flatMap[S](transformer: T => SafeValue[S]) : SafeValue[S] = synchronized {
      transformer(intervalValue)
    }
  }



  def gimmeSafeValue[T](value: T) : SafeValue[T] = SafeValue(value)

  val safeString: SafeValue[String] = gimmeSafeValue("Scala is awesome")
  val string = safeString.get //E
  val upperString = string.toUpperCase() //T
  val upperSafeString = SafeValue(upperString) //W
  // ETW above

  // ETW redux
  val upperSafeString2 = safeString.flatMap(p => SafeValue(p.toUpperCase()))

  println(upperSafeString, upperSafeString2)

  case class Person(firstName: String, lastName: String) {
    assert(firstName != null && lastName != null)
  }

  // Ex:1 capturing null
  def getPerson1(firstName: String, lastName: String): Option[Person] = {
    Option(firstName).flatMap(fn => Option(lastName).flatMap(ln => Option(Person(fn, ln))))
  }

  def getPersonFor(firstName: String, lastName: String) : Option[Person] = for {
    fn <- Option(firstName)
    ln <- Option(lastName)
  } yield Person(fn,ln)

  val p = getPerson1("Ketan", "purohit")
  val p1 = getPerson1("Ketan", null)
  println(p, p1)

  val q = getPersonFor("Ketan", "purohit")
  val q1 = getPersonFor("Ketan", null)
  println(q, q1)
  for (elem <- List[Option[Person]](q, q1)) {
    elem match {
      case Some(Person(a, b)) => println(s"Person $a $b")
      case None => println("No valid person")
    }
  }

  // Ex:2
  case class User(id: String)
  case class Product(sku: String, price: Double)

  def getUser(url: String) : Future[User] = Future {
   // do something interesting
    User("me")
  }

  def getLastOrder(userId: String) : Future[Product] = Future {
    Product("123-456", 99.99)
  }

  val myUrl = "my.a.b.c"

  // Style:1 -- beginner
  getUser(myUrl).onComplete {
    case Success(User(id)) => val lastOrder = getLastOrder(id)
                              lastOrder.onComplete {
                                case Success(Product(sku, price)) => val cost = price * 1.19
                              }
  }

  val valInclPrice = getUser(myUrl).flatMap(user => getLastOrder(user.id).map(order => order.price * 1.19))

  valInclPrice.onComplete {
    case Success(p) => println("valInclPrice", p)
    case Failure(e) => println("valInclPrice", e.getMessage())
  }

  val valInclPriceFor = for {
    user <- getUser(myUrl)
    product <- getLastOrder(user.id)
  } yield product.price * 1.19

  val d = Duration(1000, java.util.concurrent.TimeUnit.MILLISECONDS)
  val r1 = Await.result(valInclPriceFor, d)
  val r2 = Await.result(valInclPrice, d)
  println(r1, r2)

  // Lesson 3 - double for loops
  val numbers = List(1,2,3)
  val chars = List('a','b','c')

  val combosFlatmap = numbers.flatMap(n => chars.map(c => (n, c)))

  val combosFor = for {
    n <- numbers
    c <- chars
  } yield (n, c)

  println(combosFor)

  // Lesson 4
  // Property 1: Left identity
  // monad(x).flatMap(f) == f(x)
  def twoConsecutive(x: Int) = List(x, x+1)
  println(twoConsecutive(3))
  println(List(3).flatMap(twoConsecutive))
  // above 2 are same .. lesson *left identity

  // Property 2: Right identity
  // monad(x).flatMap(y => monad(y)) == monad(x)
  println(List(1,2,3).flatMap(x => List(x)))
  // above input list is same as output
  // hence useless -

  // prop3 - ETW-ETW (aka associativity)
  // monad(x).flatmap(f).flatmap(g) == monad(x).flatmap(i => f(i).flatmap(g))
  val incrementer = (x:Int) => List(x,x+1)
  val doubler = (x:Int) => List(x,2*x)
  println(numbers.flatMap(incrementer).flatMap(doubler))
  //List(1, 2, 2, 4,  2, 4, 3, 6,  3, 6, 4, 8)
  println(numbers.flatMap(n => incrementer(n).flatMap(doubler)))
  //List(1, 2, 2, 4,  2, 4, 3, 6,  3, 6, 4, 8)

  val result = for {
    n <- numbers
    l1 <- incrementer(n)
    l2 <- doubler(l1)
  } yield l2
  println(result)
  // List(1, 2, 2, 4, 2, 4, 3, 6, 3, 6, 4, 8)
}
