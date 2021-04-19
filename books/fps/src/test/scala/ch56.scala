import org.scalatest.funsuite.AnyFunSuite

class ch56 extends AnyFunSuite {

  def toInt(s:String):Option[Int] = {
    try {
      Some(s.trim.toInt)
    }
    catch {
      case e: Exception => None
    }
  }

   test("Option") {
    val x: Option[Int] = Some(3)
    val y: Option[Int] = Some(1)
     val z:Option[Int] = None
    val r = x.map(a => y map {b => a+b})
     val r2 = x.flatMap(a => y map {b => a+b})
    assert(r == Some(Some(4)))
     assert(r2 == Some(4))

     val s = for {
       i1 <- x
       i2 <- y
     }  yield i1 + i2
     assert(s== Some(4))

  }

  test("MapOption") {
    val x: Option[Int] = Some(3)
    val z = x.map(_  * 2)
    println(z)

  }

  test("seq") {
    val s = Seq[Option[Int]](Some(1), Some(3))
    val r = for {
      item <- s
    } yield item

    assert(true)
  }

}
