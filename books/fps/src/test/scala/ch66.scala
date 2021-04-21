import org.scalatest.funsuite.AnyFunSuite

class ch66 extends AnyFunSuite{

  class Wrapper[A] private (value:A) {

    val myValue:A = value

    def map[B](f: A => B) : Wrapper[B] = {
      new Wrapper[B](f(value))
    }

    def flatMap[B](f : A => Wrapper[B]) : Wrapper[B] = {
      f(value)
    }
  }

  object Wrapper {
    def apply[A](value:A) : Wrapper[A] = new Wrapper[A](value)
  }


  test("test_wrapper") {
    val x = Wrapper[String]("x")
    val y = Wrapper[String]("y")
    val r = for {
      i <- x
      j <- y
    } yield i + j

    assert(r.myValue == "xy")
  }

  test("pattern") {
    val x = Seq[Any](Map[String, List[String]]("s" -> List("a","b")), Map[String,List[Int]]("s" -> List(1,2)), Map[Int,Int](3->3), Map[String,String]("a" -> "b"))
    x.foreach(item =>
      item match {
        case m1: Map[String@unchecked, List[String]@unchecked] => {println("Map[String, List[String]]")}
        case m2: Map[String@unchecked, List[Int]@unchecked] => println("Map[String, List[Int]]")
        case m3: Map[Int@unchecked,Int@unchecked] => println("Map[Int,Int]")
        case _ => println("OTHER")
      })
  }

  test("pattern_shapeless") {
    import shapeless._
    val map_String_List_String = TypeCase[Map[String, List[String]]]
    val map_String_List_Int = TypeCase[Map[String, List[Int]]]
    val map_Int_Int = TypeCase[Map[Int,Int]]

    val x = Seq[Any](Map[String, List[String]]("s" -> List("a","b")), Map[String,List[Int]]("s" -> List(1,2)), Map[Int,Int](3->3), Map[String,String]("a" -> "b"))
    val item = Map[String, List[Int]]()
    x.foreach(item =>
    item match {
        case map_String_List_String(item) => {println("Map[String, List[String]]")}
        case map_String_List_Int(item) => println("Map[String, List[Int]]")
        case map_Int_Int(item) => println("Map[Int,Int]")
        case _ => println("OTHER")
      })

  }

}
