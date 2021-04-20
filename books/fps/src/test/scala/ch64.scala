import org.scalatest.funsuite.AnyFunSuite

class ch64 extends  AnyFunSuite{
  class Wrapper[Int](value: Int) {
    val myValue = value
    def map(f : Int =>Int) : Wrapper[Int] = {
      new Wrapper[Int](f(value))
    }
    def flatMap(f : Int => Wrapper[Int]) : Wrapper[Int] = {
      f(value)
    }
  }

  test("wrapper_map") {
    val r = new Wrapper[Int](5).map(_ * 2)
    assert(r.myValue == 10)
  }

  test("single_for_expression") {
    val w = new Wrapper[Int](4)
    val r = for {
      i <- w
    } yield i * 2

    assert(r.myValue == 8)
  }

  test("multi_for_expression") {
    val x = new Wrapper(1)
    val y = new Wrapper[Int](2)
    val z = new Wrapper[Int](3)

    val r = for {
      i <- x
      j <- y
      k <- z
    } yield i + j + k

    assert(r.myValue == 6)
  }

  test("wrapper_flatmap") {
    def f(i: Int) = new Wrapper[Int](i)
    val r = new Wrapper[Int](5).flatMap(f)
    assert(r.myValue == 5)
  }
}
