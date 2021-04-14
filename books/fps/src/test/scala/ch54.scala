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

  case class StringToInt(run:String => Int, name:String)

  def f2paramgroups (s:String)(f:String => Int) = f(s)

  case class s2i(s:String)(f:String=>Int) {
    def funcEval = f(s)
  }


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
    val stringToInt = StringToInt ({s : String => s.length}, "Ketan")
    assert(stringToInt.run("Ketan") == 5)
    assert(stringToInt.run(stringToInt.name) == 5)

    def namedFunc(s: String) = s.length

    val stringToInt2 = StringToInt(namedFunc, "Ketan")
    assert(stringToInt2.run(stringToInt2.name) == 5)

  }

  test("f2paramgroups") {
    val r = f2paramgroups("Ketan") (s => s.length)
    assert(r == 5)
  }

  test("s2i") {
    val si = s2i("Ketan")({s: String => s.length})
    assert(si.funcEval == 5)
  }

}
