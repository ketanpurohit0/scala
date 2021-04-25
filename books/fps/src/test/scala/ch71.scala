import org.scalatest.funsuite.AnyFunSuite

class ch71 extends AnyFunSuite{

  case class Debuggable[A](value:A, log: List[String]) {

    def map[B](f: A => B) : Debuggable[B] = {
      Debuggable(f(this.value), this.log)
    }

    def flatMap[B](f: A => Debuggable[B]) : Debuggable[B] = {
      val nv = f(this.value)
      Debuggable(nv.value, this.log ::: nv.log)
    }
  }

  def f(a:Int) = Debuggable(a*2, List(s"f($a) => _*2;"))
  def g(a:Int) = Debuggable(a*3, List(s"f($a) => _*3;"))
  def h(a:Int) = Debuggable(a*4, List(s"f($a) => _*4;"))

  test("forInt") {
    val r = for {
      i <- f(1)
      j <- g(i)
      k <- h(j)
    } yield k

    assert(r.value == 24 && r.log.size == 3)
  }


}
