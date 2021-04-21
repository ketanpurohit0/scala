import org.scalatest.funsuite.AnyFunSuite

class ch65 extends AnyFunSuite{

  class Wrapper[A](value:A) {

    val myValue:A = value

    def map[B](f: A => B) : Wrapper[B] = {
      new Wrapper[B](f(value))
    }

    def flatMap[B](f : A => Wrapper[B]) : Wrapper[B] = {
      f(value)
    }
  }

  test("generic_wrapper") {
    val x = new Wrapper[Int](5)
    val y = new Wrapper[Int](6)
    val z = new Wrapper[Int](7)

    val r = for {
      i <- x
      j <- y
      k <- z
    } yield i + j + k

    assert(r.myValue == 18)
  }

  test("generic_wrapper_str") {
    val x = new Wrapper[String]("5")
    val y = new Wrapper[String]("6")
    val z = new Wrapper[String]("7")

    val r = for {
      i <- x
      j <- y
      k <- z
    } yield i + j + k

    assert(r.myValue == "567")
  }

}
