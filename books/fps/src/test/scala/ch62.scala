import org.scalatest.funsuite.AnyFunSuite

class ch62 extends  AnyFunSuite{
  def f(a: Int) : (Int,String) = (a,"f")
  def g(a: Int) : (Int,String) = (a,"g")
  def h(a: Int) : (Int,String) = (a,"h")

  def bind(f: Int => (Int,String), t: (Int,String)): (Int,String) = {
    val (l,r) = f(t._1)
    (l, r + t._2)
  }

  test("bind") {
    val fR = f(100)
    val gR = bind(g, fR)
    val hR = bind(h, gR)
    assert(hR._2 == "hgf")
  }


}
