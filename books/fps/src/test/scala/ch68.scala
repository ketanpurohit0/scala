import org.scalatest.funsuite.AnyFunSuite

class ch68 extends AnyFunSuite{



  case class Debuggable(value: Int, message: String) {
    def map(f: Int => Int): Debuggable = {
       Debuggable(f(this.value), this.message)
    }
    def flatMap(f: Int => Debuggable): Debuggable = {
      val d = f(this.value)
      Debuggable(d.value, this.message + "\n" + d.message )
    }
  }

 def f(i: Int): Debuggable =  {
   Debuggable(i*2,s"f${i*2}")
 }
  def g(i: Int): Debuggable =  {
    Debuggable(i*3,s"g${i*3}")
  }
  def h(i: Int): Debuggable =  {
    Debuggable(i*4,s"h${i*4}")
  }

  test("ch68") {
    val r = for {
      i <- f(2)
      j <- g(2)
      k <- h(2)
    } yield i+ j+ k

    assert(r.value == 18)
  }

  test("ch68_2") {
    val r = for {
      i <- f(2)  // 2*2
      j <- g(i)  // 2*2*3
      k <- h(j)  // 2*2*3*4
    } yield k

    assert(r.value == 48)

    val fr = f(2).flatMap(r1 => g(r1).flatMap(r2=>h(r2).map(r3=>r3)))
    assert(r.value == fr.value)
  }

}
