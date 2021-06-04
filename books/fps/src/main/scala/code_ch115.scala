import akka.actor.{Actor, ActorRef, ActorSystem, Props}

case class Hello(msg: String)

class HelloActor extends Actor {
  override def receive: Receive = {
    case Hello(s) => {
      println(s"You: $s")
      println(s"Me: $s back, ${System.nanoTime()}, ${Thread.currentThread().getId()}")
    }
    case _ => println(s"You: Unsupported message")
  }
}


object AkkaHelloWorld extends App {
  val actorSystem = ActorSystem("ActorSystem")

  val helloActor = actorSystem.actorOf(Props[HelloActor], "helloActor")

  helloActor ! Hello("hello")
  helloActor ! Hello("kaise ho")
  helloActor ! "kem cho"
  helloActor.tell(Hello("Habari gani"), Actor.noSender)
  println(helloActor.path)

  actorSystem.terminate()
}

object AkkaHelloWorld2 extends App {
  val actorSystem = ActorSystem("ActorSystem")

  var actors = Seq[ActorRef]()
  for (actor <- 1 to 10) {
    actors :+= actorSystem.actorOf(Props[HelloActor], s"helloActor:$actor")
  }
  actors.foreach(actor => {
    println(actor.path)
    actor.tell(Hello(s"Hi ${actor.path}"), Actor.noSender)
  })


  actorSystem.terminate()
}

