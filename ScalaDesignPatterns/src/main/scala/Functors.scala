trait Functor2[F[_]] {
  def map[T, Y](l: F[T])(f: T => Y): F[Y]
}

trait Functor[T] {
  def map[Y](f: T => Y): Functor[Y]
}

//trait Monad[T] extends Functor[T] {
//  def unit[Y](value: Y): Monad[Y]
//  def flatMap[Y](f: T => Monad[Y]): Monad[Y]
//
//  override def map[Y](f: T => Y): Monad[Y] = flatMap(i => unit(f(i)))
//}

package object functors {
  val listFunctor = new Functor2[List] {
    override def map[T, Y](l: List[T])(f: T => Y): List[Y] = l.map(f)
  }
}

//sealed trait Option[A] extends Monad[A]
//case class Some[A](a: A) extends Option[A] {
//  override def unit[Y](value: Y): Monad[Y] = Some(value)
//
//  override def flatMap[Y](f: A => Monad[Y]): Monad[Y] = f(a)
//}
//
//case class None[A]() extends Option[A] {
//  override def unit[Y](value: Y): Monad[Y] = None()
//
//  override def flatMap[Y](f: A => Monad[Y]): Monad[Y] = None()
//}

import functors._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, ExecutionContext, Future}
object FunctorsEx1 extends App {
  val numbers = List(1, 2, 3)
  val labels = Map(1 -> "one", 2 -> "two", 3 -> "three")

  val dbls = listFunctor.map(numbers) { i => 2 * i }
  val lbls = listFunctor.map(numbers) { i => labels(i) }

  println(dbls)
  println(lbls)

  val n1 = List(1, 2, 3, 4)
  val n2 = List(5, 6, 7, 8)
  val r1 = n1.flatMap(n => n2.map(_ * n))
  val r2 = n1.map(n => n2.map(_ * n))
  val r3 = r2.flatten
  val r4 = for {
    n <- n1
    m <- n2
  } yield (m * n)
  println(r1)
  println(r2)
  println(r3)
  println(r4)

}

object OptionsEx1 extends App {
  val a = Some("S")
  val b = None
  val c = Some(5)
  val f = Future(5)

  val r = for {
    i_a <- a
    // i_b <- b
    i_c <- c
  } yield (i_a)

  println(r)

  val r2 = for {
    f_1 <- f
  } yield f_1

}
