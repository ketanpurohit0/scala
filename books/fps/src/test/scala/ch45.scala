import org.scalatest.funsuite.AnyFunSuite

class ch45 extends AnyFunSuite{
  // Creating a sequence class to be used in a for comprehension

  case class Sequence[T](initialElems: T*) {
    private val elems = scala.collection.mutable.ArrayBuffer[T]()
    elems ++= initialElems

    def foreach(block: T => Unit): Unit = {
      // make for work
      elems.foreach(block)
    }

    def map[B](f: T => B): Sequence[B] = {
      // makes yield work
      val tbMap = elems.map(f)
      Sequence(tbMap:_*)
    }

    def withFilter(p: T => Boolean) : Sequence[T] = {
      // make filter work
      Sequence(elems.filter(p): _*)
    }
  }


  test("Seq1") {
    val s_string = Sequence("A","B")
    val s_int = Sequence(1,2)
  }

  test("loop") {
    val s = Sequence(1,2)
    for (i <- s) {println(i)}
  }

  test("yield") {
    val s = Sequence(1,2)
    def y = for (i <- s) yield i
    for (i <- y) {println(i)}
  }

  test("filter") {
    val s = Sequence(1,2)
    def y = for (i <- s) yield i
    for (i <- y if i > 8) {println(i)}
  }
}
