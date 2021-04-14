import org.scalatest.funsuite.AnyFunSuite

class ch54 extends AnyFunSuite{

  trait Person {
    val name: String
    val age: Int
    override val toString: String = s"name: $name, age:$age"
  }

  def timer[B](code: => B): (Long,B) = {
    val t0 = System.nanoTime()
    val r = code
    (System.nanoTime() - t0, r)
  }

  case class StringToInt(run:String => Int)


  test("traitPerson") {
    val p = new Person {
       val name = "Ketan"
       val age = 21
    }
    assert(p.age == 21)
  }

  test("timer") {
    {
      val (t, r) = timer(Thread.sleep(1000))
      assert(t >= 1000 * 1000000000)
    }
    {
      val (t, r) = timer {Thread.sleep(1000)}
      assert(t >= 1000 * 1000000000)
    }
  }

  test("stringToInt") {
    val stringToInt = StringToInt {s : String => s.length}
    assert(stringToInt.run("Ketan") == 5)
  }

}
