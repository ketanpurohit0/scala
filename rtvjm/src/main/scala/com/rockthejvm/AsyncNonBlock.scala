package com.rockthejvm
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}

object AsyncNonBlocking {

  // sync+blocking on this thread
  def blockingFunction(x: Int) : Int = {
    Thread.sleep(1000)
    42
  }

  blockingFunction(5) //blocking
  val meaningOfLife= 42

  // async blocking on another thread.
  def asyncNonBlocking(x: Int): Future[Int] = Future {
    Thread.sleep(10000)
    x + 42
  }

  asyncNonBlocking(5)
  val anotherMeaningOfLife = 43

  // async, non-blocking
  def createSimpleActor() = Behaviors.receiveMessage[String] {
    m =>
      println(m)
    Behaviors.same
  }

  val rootActor = ActorSystem(createSimpleActor(),"TestSystem")
  rootActor ! "Message in a bottle"  //enqueue a message, async + non-blocking

  val promiseResolver = ActorSystem( Behaviors.receiveMessage[(String, Promise[Int])] {
    case (message, promise) => promise.success(message.length)
      Behaviors.same
  }, "promiseResolver")

  def doAsyncNonBlockingComputation(s: String): Future[Int] = {
    val aPromise = Promise[Int]()
    promiseResolver ! (s, aPromise)
    aPromise.future
  }

  val asyncNonBlockingResult = doAsyncNonBlockingComputation("Some message")

  def main(args: Array[String]): Unit = {
    asyncNonBlockingResult.onComplete(x => println(x.get))
    println("foo")
  }
}
