class FunctionLiterals {
  val sum = (a: Int, b: Int) => a + b
}

object FunctionLiteralApp extends App {
  val fl = new FunctionLiterals
  assert(fl.sum(3, 4) == op(fl.sum, 3, 4))

  def op(fn: (Int, Int) => Int, left: Int, right: Int): Int = {
    fn(left, right)
  }
}
