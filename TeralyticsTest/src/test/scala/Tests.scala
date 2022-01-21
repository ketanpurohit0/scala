import org.scalatest.funsuite.AnyFunSuite

class Tests extends AnyFunSuite{

    test("thiswillpass") {
      assert(true, "some cond")
    }

  test("thiswillfail") {
    assert(false, "some cond")
  }
}
