import org.scalatest.funsuite.AnyFunSuite

class ch70 extends AnyFunSuite{
    case class Debuggable[A](value: A, message: String) {
      def map[B](f: A => B) : Debuggable[B] = {
        Debuggable[B](f(value), message)
      }
      def flatMap[B](f: A => Debuggable[B]): Debuggable[B] = {
          val nv = f(value)
          Debuggable[B](nv.value, this.message + nv.message)
        }
    }

    def f(a:Int) = Debuggable(a*2, s"f($a) => _*2;")
    def g(a:Int) = Debuggable(a*3, s"f($a) => _*3;")
    def h(a:Int) = Debuggable(a*4, s"f($a) => _*4;")

    test("forInt") {
      val r = for {
        i <- f(1)
        j <- g(i)
        k <- h(j)
      } yield k

      assert(r.value == 24 && r.message == "f(1) => _*2;f(2) => _*3;f(6) => _*4;")
    }


}
