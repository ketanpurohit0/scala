package main.scala.com.rockthejvm

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}
import scala.concurrent.ExecutionContext.Implicits.global

object Advanced extends App {
  lazy val aLazyValue = 2
  lazy val lazyValueWithSideEffect = {
    println("I am so very lazy!")
    43
  }
  val eagerValue = lazyValueWithSideEffect + 1

  def methodWhichCanReturnNull() = "hello, Scala"

  val anOption = Option(methodWhichCanReturnNull())

  val stringProcessing = anOption match {
    case Some(s) => s"I have obtained a valid string $s"
    case None => "I obtained nothing"
  }

  def methodWithCanThrowException() :String = throw new RuntimeException

  val anTry = Try(methodWithCanThrowException())

  val anotherStringProcessing = anTry match {
    case Success(value) => s"I have a valid value $value"
    case Failure(exception) => s"There was an exception $exception"
  }

  val aFuture = Future({
    println("Loading ...")
    Thread.sleep(1000)
    println("Completed")
    67
  })

  Thread.sleep(2000)

  def aMethodWithImplicArgs(implicit arg: Int) = arg + 1
  implicit val myImplicitInt = 46
  println(aMethodWithImplicArgs)

  implicit class MyRichInteger(n: Int) {
    def isEven() = n % 2 == 0
  }

  println(23.isEven())
}
