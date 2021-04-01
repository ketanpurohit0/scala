import org.scalatest.funsuite.AnyFunSuite

// Functions can have multiple parameter groups

class ch26 extends AnyFunSuite {

  def whilst(predicate: => Boolean)(codeBlock: => Unit) : Unit = {
    while(predicate) {
      codeBlock
    }
  }

  def ifBothTrue(predicate1: => Boolean) (predicate2: => Boolean)(codeBlock: => Unit) : Unit = {
    if (predicate1 && predicate2) {
      codeBlock
    }
  }

  def printIntIf(printThis: Int)(implicit b: Boolean): Unit = {
    if (b) {println(printThis)}
  }

  test("whilst") {
    var i:Int = 0
    whilst(i < 6) ({println(i); i+=1})
    whilst (i < 6) {println(i); i+=1}

  }

  test("printThis") {
    implicit val b : Boolean = false
    printIntIf(5)
  }
}
