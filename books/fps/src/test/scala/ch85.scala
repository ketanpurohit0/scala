import org.scalatest.funsuite.AnyFunSuite

class ch85 extends AnyFunSuite {

  case class MyState[S,A](run: S => (S,A)) {
    
    def flatMap[B](f : A => MyState[S,B]) : MyState[S,B] = MyState {
        (s0: S) => {
          val (s1, a1) = run(s0)
          val s2 = f(a1)
          s2.run(s1)
        }
    }
    
    def map[B](f: A => B) : MyState[S,B] = MyState {
      (s0: S) => {
        val (s1, a1) = run(s0)
        val b1 = f(a1)
        (s1, b1)
      }
    }
  }
  
  case class GolfState(distance: Int)

  def nextStroke(distance: Int) : MyState[GolfState, Int] = MyState {
    (s: GolfState) => {
      val newValue = s.distance + distance
      (GolfState(newValue), newValue)
    }
  }

  test("my.MyState") {
    val initialState = GolfState(0)
    val state = for {
      _ <- nextStroke(20)
      _ <- nextStroke(15)
      r <- nextStroke(0)
    } yield r

    val result = state.run(initialState)
    val (r1,r2)  = result
    assert(r1.distance == 35)
    assert(r2 == 35)
  }
}
