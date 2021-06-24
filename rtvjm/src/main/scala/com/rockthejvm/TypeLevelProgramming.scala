package main.scala.com.rockthejvm
import scala.reflect.runtime.universe._
object TypeLevelProgramming extends App {
  def show[T](value: T)(implicit  tag: TypeTag[T]) = tag.toString().replace("main.scala.com.rockthejvm.TypeLevelProgramming.", "")

  println(show(List(1,2,3)))

  trait Nat
  class _0 extends Nat
  class Succ[N <: Nat] extends Nat

  type _1 = Succ[_0]
  type _2 = Succ[_1]
  type _3 = Succ[_2]
  type _4 = Succ[_3]
  type _5 = Succ[_4]

  // how to determine _2 < _4?
  trait <[A <: Nat, B <: Nat]
  object < {
    implicit def ltBasic[B <: Nat]: <[_0, Succ[B]] = new <[_0, Succ[B]] {}
    implicit def inductive[A <: Nat, B <: Nat](implicit lt: <[A,B]) : <[Succ[A], Succ[B]] = new <[Succ[A], Succ[B]] {}
    def apply[A <: Nat, B <: Nat](implicit  lt: <[A,B]) = lt
  }
  val comparison: <[_0, _1] = <[_0, _1]
  val c : <[_1, _3] = <[_1, _3]
  val c1 : _1 < _3 = <[_1, _3]
  println(show(comparison))

  // results in compiler error
  //val c2: _3 < _1 = <[_3, _1]
}
