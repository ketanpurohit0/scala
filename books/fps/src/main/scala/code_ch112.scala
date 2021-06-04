

class MutablePerson(var name: String, var town: String, var state: String)
{
  override def toString = s"name: $name, town: $town, state: $state"
}

case class ImmutablePerson(name: String, town: String, state: String)
{
  override def toString = s"name: $name, town: $town, state: $state"
}

object BadConcurrency extends App {

  val me = new MutablePerson("Alvin", "Talkeetna", "Alaska")

  val t1 = new Thread {
    override def run: Unit = {
      Thread.sleep(1000)
      me.town = "Boulder"
      Thread.sleep(3000)
      me.state = "Colorado"
    }
  }

  t1.start()

  println(s"1> $me")
  Thread.sleep(2000)
  println(s"2> $me")
  Thread.sleep(2000)
  print(s"3> $me")
}

object GoodConcurrency extends App {
  val me = ImmutablePerson("Alvin", "Talkeetna", "Alaska")

  val t1 = new Thread {
    override def run: Unit = {
      Thread.sleep(1000)
      Thread.sleep(3000)
    }
  }

  t1.start()

  println(s"1> $me")
  Thread.sleep(2000)
  println(s"2> $me")
  Thread.sleep(2000)
  print(s"3> $me")
}

