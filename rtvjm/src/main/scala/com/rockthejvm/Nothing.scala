package com.rockthejvm

object Nothing extends App {
  class MyPrecious  // extends AnyRef

  def gimmeNumber(): Int =  throw new NoSuchMethodError
  def gimmeString() : String = throw new NoSuchElementException
  def gimmePrecious(): MyPrecious  = throw new NoSuchElementException

  // Nothing != Null,
  // Nothing can replace any type
  // Nothing is a sub-type of all types

  def gimmePrecious2(): MyPrecious = null

  def aFunctionAboutNothing(a: Nothing) : Int = 45
//  aFunctionAboutNothing(throw new NullPointerException)

  def aFunctionReturningNothing(): Nothing = throw new NullPointerException

  // usefull in generic, esp covariant generics

  abstract class MyList[+T]
  class NonEmptyList[+T] (head: T, tail: MyList[T])
  object EmptyList extends MyList[Nothing]

  val listOfStrings : MyList[String] = EmptyList
  val listOfIntergers : MyList[Int] = EmptyList
  val listOfPrecious : MyList[MyPrecious] = EmptyList

  //
  def someUnimplementedMethod(): String = ???

}
