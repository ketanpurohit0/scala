import org.scalatest.funsuite.AnyFunSuite

class ch23 extends AnyFunSuite {

  // Functions that take functions as input parameters

  private def func_returns_2() : Int = {2}
  private def is_even_def(i : Int) = i % 2 == 0
  private val is_even_val = (i : Int) => i % 2 == 0

  private class Ch23ClassMethods {
    def func_returns_2() : Int = {2}
    def is_even_def(i : Int) = i % 2 == 0
    val is_even_val = (i : Int) => i % 2 == 0
  }

  private object Ch23ObjectMethods {
    def func_returns_2() : Int = {2}
    def is_even_def(i : Int) = i % 2 == 0
    val is_even_val = (i : Int) => i % 2 == 0
  }

  def example_f(f:() => Int) : Unit = {
    f()
  }

  def example2_f(f:() =>Int) : Int = {
    f()
  }

  def example_3f(f: Int => Boolean, i: Int): Boolean = {
    f(i)
  }


  test("example_f") {

    assert(example_f(func_returns_2) == ())
    assert(example_f(new Ch23ClassMethods().func_returns_2) == ())
    assert(example_f(Ch23ObjectMethods.func_returns_2) == ())

  }

  test("example_2f") {
    assert(example2_f(func_returns_2) == 2)
    assert(example2_f(new Ch23ClassMethods().func_returns_2) == 2)
    assert(example2_f(Ch23ObjectMethods.func_returns_2) == 2)


  }

  test("example_3f") {
    assert(!example_3f(is_even_def, 3))
    assert(example_3f(is_even_def, 2))
    assert(!example_3f(is_even_val, 3))
    assert(example_3f(is_even_val, 2))
    // use class method
    assert(!example_3f(new Ch23ClassMethods().is_even_def, 3))
    assert(example_3f(new Ch23ClassMethods().is_even_def, 2))
    assert(!example_3f(new Ch23ClassMethods().is_even_val, 3))
    assert(example_3f(new Ch23ClassMethods().is_even_val, 2))
    // use object methods
    assert(!example_3f(Ch23ObjectMethods.is_even_def, 3))
    assert(example_3f(Ch23ObjectMethods.is_even_def, 2))
    assert(!example_3f(Ch23ObjectMethods.is_even_val, 3))
    assert(example_3f(Ch23ObjectMethods.is_even_val, 2))

  }

}
