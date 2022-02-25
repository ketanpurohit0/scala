import scala.io.Source

trait Alarm { // interface
  def trigger(): String
}

trait Notifier {
  val notificationMessage: String
  def printNotification(): Unit = {
    println(notificationMessage)
  }

  def clear()
}

// presence of notificationMessage required
class NotifierImpl(val notificationMessage: String) extends Notifier {
  override def clear(): Unit = println(notificationMessage)
}

trait Beeper {
  def beep(times: Int): Unit = {
    1 to times foreach (i => println(i))
  }
}

// traits extending classes
abstract class Connector {
  def connect()
  def close()
}

trait ConnectorWithHelper extends Connector {
  def findDriver(): Unit = {
    println("Find driver called.")
  }
}

class PgSqlConnector extends ConnectorWithHelper {
  override def connect(): Unit = println("Connected.")

  override def close(): Unit = println("Closed...")
}

// traits extending each other
trait Ping {
  def ping(): Unit = { println("ping") }
}
trait Pong {
  def pong(): Unit = { println("pong") }
}

trait PingPong extends Ping with Pong {
  def pingpong(): Unit = {
    ping()
    pong()
  }
}

trait AlarmNotifier {
  this: Notifier with Ping with Pong =>
  def trigger(): String
}
// Same signature and return type
trait FormalGreeting {
  def hello(): String
  def info(): String = "I have Formal Info"
}

trait InformalGreeting {
  def hello(): String
  def info(): String = "I have Informal Info"
}

class Greeter extends FormalGreeting with InformalGreeting {
  override def hello(): String = "Morning sir/madam."

  override def info(): String = super.info()
  def InformalGreeting_info(): String = super[InformalGreeting].info()
  def FormalGreeting_info(): String = super[FormalGreeting].info()
}

// composing traits (on the fly)
class Watch(brand: String, initialTime: Long) extends {
  def getTime(): Long = System.currentTimeMillis() - initialTime
}

object Main extends App {
  val n = new NotifierImpl("f")
}

object BeeperRunnner extends App {
  val b = new Beeper {}
  b.beep(3)
}

object PingPongRunner extends App with Ping with Pong {
  ping()
  pong()
}

object CompositionRunner extends App {
  val watch = new Watch("Timex", 0)

  val composedWatch = new Watch("PingerWatch", initialTime = 0) with Ping
  val notifierWatch = new Watch("Notifier", initialTime = 0) with Notifier {
    override val notificationMessage: String = "Notification Triggered"

    override def clear(): Unit = println(notificationMessage)
  }

  watch.getTime()
  composedWatch.ping() // as well as getTime
  notifierWatch.printNotification() // as well as getTime
}

//object CompositionWithComplexTraits extends App {
//
//  // fails to compile -> Watch is not a subclass of the superclass Connector of the mixin trait ConnectorWithHelper
//  val expensiveWatch = new Watch("expensive", 0L) with ConnectorWithHelper {
//    override def connect(): Unit = println("Connected with another connector.")
//
//    override def close(): Unit = println("Closed with another connector.")
//  }
//
//  expensiveWatch.findDriver()
//  expensiveWatch.connect()
//  expensiveWatch.close()
//}

object CompositionWithSelfTypeTraits extends App {
  val watch = new Watch("notifierWatch", initialTime = 0L) with AlarmNotifier with Notifier with Ping with Pong {
    override def trigger(): String = "Alarm triggered."

    override val notificationMessage: String = "The notification."

    override def clear(): Unit = println("Alarm cleared.")
  }

  watch.getTime()
  watch.printNotification()
  watch.trigger()
  watch.clear()
}

object GreeterApp extends App {
  val greeter = new Greeter()

  Seq(greeter.info, greeter.FormalGreeting_info, greeter.InformalGreeting_info).foreach(m => println(m))
  Seq(greeter.info(), greeter.FormalGreeting_info(), greeter.InformalGreeting_info()).foreach(m => println(m))

}
