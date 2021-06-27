package com.rockthejvm

import scala.concurrent.Future
import scala.util.Try

object CallByName extends App {
  def byValueFunction(x: Int) = 43

  byValueFunction(2 + 3)

  def byNameFunction(x: => Int) = x + 5

  // re-evalutian
  def byValuePrint(x:Long) = {
    println(x)
    println(x)
  }

  def byNamePrint(x: => Long) = {
    println(x)
    println(x)
  }

  byValuePrint(System.nanoTime())
  byNamePrint(System.nanoTime())

  // call by need
  abstract class MyList[+T] {
    def head: T
    def tail: MyList[T]
  }

  class NonEmptyList[+T](h: => T, t: => MyList[T]) extends MyList[T] {
    override lazy val head = h
    override lazy val tail = t
  }

  // hold the door, basically takes the expression
  val anAttempt : Try[Int] =Try(throw new NullPointerException)
  val anAttempt2 = Try {
    throw new NullPointerException
  }

  //
  import scala.concurrent.ExecutionContext.Implicits.global
  val aFuture = Future {
    42
  }
}
