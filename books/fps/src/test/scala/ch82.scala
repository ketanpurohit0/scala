import cats.data.State
import org.scalatest.funsuite.AnyFunSuite

class ch82 extends AnyFunSuite{

  case class GolfStateConcept(distance: Int)

  def nextStroke(distance: Int, prevState: GolfStateConcept) = GolfStateConcept(prevState.distance + distance)

  case class NaiveState(value:Int) {
    def map(f: Int => Int) = NaiveState(f(value))
    def flatMap(f: Int => NaiveState) = f(value)
  }

  case class GolfState(distance: Int)

  def nextStroke(distance: Int) : State[GolfState, Int] = State {
    (previousState: GolfState) => {
        val newValue = previousState.distance + distance
        (GolfState(newValue), newValue)
      }
  }

  test("3strockes") {
    val state0 = GolfStateConcept(0)
    val state1 = nextStroke(20, state0)
    val state2 = nextStroke(15, state1)
    val state3 = nextStroke(0, state2)

    assert(state0.distance == 0 && state1.distance == 20 && state2.distance == 35 && state3 == state2)
  }

  test("NaiveState") {

    val r = for {
      a <- NaiveState(0)
      b <- NaiveState(a + 15)
      c <- NaiveState(b + 20)
      d <- NaiveState(c + 0)
    } yield d

    assert(r.value == 35)
  }

  test("cats.State") {
    val initialState = GolfState(0)
    val state = for {
      _ <- nextStroke(20)
      _ <- nextStroke(15)
      r <- nextStroke(0)
    } yield r

    val result = state.run(initialState)
    val (r1,r2)  = result.value
    assert(r1.distance == 35)
    assert(r2 == 35)
  }

}
