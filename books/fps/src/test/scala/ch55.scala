import org.scalatest.funsuite.AnyFunSuite

class ch55 extends  AnyFunSuite{

  test("withOptions") {
    val o = Seq[Option[Int]](None, Some(1),Some(2), None, Some(3))
    assert(o.flatten == List(1,2,3))
  }

  test("withString") {
    val s = Seq[String]("Ketan", "Purohit")
    assert(s.map(_.toUpperCase()) == List("KETAN","PUROHIT"))
    assert(s.flatMap(_.toUpperCase()) == s.map(_.toUpperCase()).flatten)
    assert(s.map(_.toUpperCase()).flatten == List('K','E','T','A','N','P','U','R','O','H','I','T'))
  }

  test("withListOfLists"){
    def v(i:Int) = List(i,i+1,i+2)
    val l = Seq[Int](1,2,3)
    assert(l.map(v).flatten == List(1,2,3,2,3,4,3,4,5))
    assert(l.flatMap(v) == List(1,2,3,2,3,4,3,4,5))
  }

  test("L"){
    //import scala.language.postfixOps
    val l = Seq(1,2,3).map{x => List(-x,x)}.flatten
    val m = Seq(1,2,3).flatMap(x => List(-x,x))
    assert(l == List(-1,1,-2,2,-3,3))
    assert(l == m)
  }
}
