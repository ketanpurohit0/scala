import org.scalatest.funsuite.AnyFunSuite

class ch85 extends AnyFunSuite {

  case class State[S,A](run: S => (S,A)) {

    def flatMap[B](f : A => State[S,B]) : State[S,B] = State {
        (s0: S) => {
          val (s1, a1) = run(s0)
          val s2 = f(a1)
          s2.run(s1)
        }
    }

    def map[B](f: A => B) : State[S,B] = State {
      (s0: S) => {
        val (s1, a1) = run(s0)
        val b1 = f(a1)
        (s1, b1)
      }
    }
  }

  case class GolfState(distance: Int)

  def nextStroke(distance: Int) : State[GolfState, Int] = State {
    (s: GolfState) => {
      val newValue = s.distance + distance
      (GolfState(newValue), newValue)
    }
  }

  test("my.State") {
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
