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

    def flatMap[B](f: T => Sequence[B]) : Sequence[B] = {

      def flattenMe(s: Sequence[Sequence[B]]) : Sequence[B] = {
        val ab = scala.collection.mutable.ArrayBuffer[B]()
        for (i <- s) {
           for (e <-i) {
             ab += e
           }
        }
        Sequence(ab:_*)
      }

      // make flatmap work
      val m = this.map(f)
      flattenMe(m)

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

  test("Persons") {
    case class Person(name:String)

    val myFriends = Sequence(Person("Abi"), Person("Vinny"),Person("Faz"))
    val adamsFriends = Sequence(Person("Abi"),Person("Faz"),Person("Mo"), Person("Vinny"))

    val mutualFriends = for {
      mine <- myFriends
      adams <- adamsFriends
      if mine.name == adams.name
    } yield mine

    mutualFriends.foreach(println)
  }
}
