import org.scalatest.funsuite.AnyFunSuite

// Partially applied functions and Currying

class ch27 extends AnyFunSuite{
  def wrap(prefix: String)(htm:String)(suffix: String) : String = {
    prefix+htm+suffix
  }

  def wrap2(prefix: String, htm:String, suffix:String) : String = {
    prefix + htm + suffix
  }

  def wrapped_div = wrap("div")(_:String)("div")
  def wrapped_foo = wrap("foo")(_:String)("foo")

  def wrapped2_div = wrap2("div",_,"div")

  test("wrapped_div") {
    assert(wrapped_div("X") == "div" + "X" + "div")
  }

  test("wrapped_foo") {
    assert(wrapped_foo("Y") == "foo" + "Y" + "foo")
  }

  test("eta") {
    val eta = wrap2 _
    assert(eta("A","B","C") == "A" +"B" +"C")

    val eta_curry = eta.curried
    assert(eta_curry("A")("B")("C") == "A"+ "B" +"C")

    val partial_apply_a = eta_curry("A")
    val partial_apply_b= partial_apply_a("B")
    val partial_apply_c = partial_apply_b("C")
    assert(partial_apply_c == "A" + "B" + "C")

  }

  test("wrapped2_div") {
    assert(wrapped2_div("X") == "div" + "X" + "div")
  }
}
