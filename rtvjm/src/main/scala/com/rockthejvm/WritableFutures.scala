package com.rockthejvm

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}

object WritableFutures extends App{

  val f = Future[Int] {
    42
  }

  def gimmeMyPreciousValues(yourArgs: Int): Future[String] = ???

  object MyService {
    def produceAPreciousValue(theArg: Int) : String = s"Meaning of life $theArg"

    def submitTask[A](actualArg: A)(function: A => Unit) : Boolean = {
      true
    }
  }

  //use promise
  val myPromise = Promise[String]()
  val myFuture = myPromise.future
  val further = myFuture.map(_.toUpperCase)

  def asyncCall(promise: Promise[String]) : Unit = {
    promise.success("Your value")
  }

  asyncCall(myPromise)

  def gimmeMyPreciousValuesImpl(yourArgs: Int): Future[String] = {
    val thePromise = Promise[String]()
    MyService.submitTask(yourArgs){x: Int =>
      val preciousValue = MyService.produceAPreciousValue(x)
      thePromise.success(preciousValue)
    }
    thePromise.future
  }

  val f2 = gimmeMyPreciousValuesImpl(5)
  println()

}
