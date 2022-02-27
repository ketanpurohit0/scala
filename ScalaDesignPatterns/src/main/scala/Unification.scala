class FunctionLiterals {
  val sum = (a: Int, b: Int) => a + b
}

// Pattern matching
case class Point(x: Double, y: Double)
sealed abstract trait Shape
case class Circle(centre: Point, radius: Double) extends Shape
case class Rectangle(topLeft: Point, height: Double, width: Double) extends Shape
object Shape {
  def area(shape: Shape): Double = {
    shape match {
      case Circle(centre, radius)            => Math.PI * Math.pow(radius, 2.0)
      case Rectangle(topLeft, height, width) => height * width
    }
  }
}

// module and objects
trait Tick {
  trait Ticker {
    def count(): Int
    def tick(): Unit
  }
  def ticker: Ticker
}

trait TickUser extends Tick {
  class TickUserImpl extends Ticker {
    var curr = 0
    override def count(): Int = curr
    override def tick(): Unit = curr += 1
  }
  object ticker extends TickUserImpl
}

trait Alarm2 {
  trait Alarmer {
    def trigger(): Unit
  }
  def alarm: Alarmer
}

trait AlarmUser extends Alarm2 with Tick {
  trait AlarmUserImpl extends Alarmer {
    override def trigger(): Unit = {
      if (ticker.count() % 10 == 0) {
        println(s"Triggered @${ticker.count()}")
      }
    }
  }
  object alarm extends AlarmUserImpl
}

object FunctionLiteralApp extends App {
  val fl = new FunctionLiterals
  assert(fl.sum(3, 4) == op(fl.sum, 3, 4))

  def op(fn: (Int, Int) => Int, left: Int, right: Int): Int = {
    fn(left, right)
  }
}

object ShapeApp extends App {
  val shapes = List(Circle(Point(0, 0), 2), Rectangle(Point(0, 0), 2, 2))
  shapes.foreach(s => println(Shape.area(s)))
}

object ModuleApp extends AlarmUser with TickUser {
  def main(args: Array[String]): Unit = {
    (1 to 100).foreach {
      case i => {
        ticker.tick()
        alarm.trigger()
      }
    }
  }
}
