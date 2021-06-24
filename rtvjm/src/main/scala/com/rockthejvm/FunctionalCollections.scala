package com.rockthejvm

object FunctionalCollections extends App {

  val aSet = Set(1,2,3,4,5)

  aSet(2) // true
  aSet(4352) //false

  val anewSet = aSet + 6
  val aSmallerSet = aSet - 3

  trait RSet[A] extends (A => Boolean) {
    override def apply(x: A): Boolean = contains(x)
    def contains(x: A) :Boolean
    def +(x: A): RSet[A]
    def -(x:A): RSet[A]
  }

  case class REmpty[A]() extends RSet[A] {
    override def contains(x: A): Boolean = false

    override def +(x: A): RSet[A] = new PBSet[A](_ == x)

    override def -(x: A): RSet[A] = this
  }

  case class PBSet[A](property: A => Boolean) extends RSet[A] {
    def contains(x: A): Boolean = property(x)
    def +(x: A): RSet[A] = if (property(x)) new PBSet[A](e => property(e) || e == x) else this
    def -(x: A): RSet[A] = if (contains(x)) new PBSet[A](e => property(e) && e != x) else this
  }

  object RSet {
    def apply[A](values: A*) = values.foldLeft[RSet[A]](new REmpty())(_ + _)
  }

  val setupElements = RSet(1,2,3,4,5)
  println(setupElements.contains(43))

}
