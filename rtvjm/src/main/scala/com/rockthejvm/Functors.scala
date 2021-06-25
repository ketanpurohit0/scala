package com.rockthejvm

import scala.util.{Success, Try}

object Functors extends App {
  val anIncrementedList = List(1,2,3).map(i => i + 1)

  // options
  val anOption : Option[Int] = Some(2)

  // try
  val aTry : Try[Int] = Success(42)

  //
  val aTransformedOption = anOption.map(x => x * 10)
  val aTransformedTry = aTry.map(x => x * 10)

  def do10xList(list: List[Int]): List[Int] = list.map(_ * 10)
  def do10xOption(option: Option[Int]): Option[Int] = option.map(_ * 10)
  def do10xTry(atry: Try[Int]): Try[Int] = atry.map(_ * 10)

  //
  trait Functor[C[_]] {
    def map[A, B](container: C[A])(f: A => B): C[B]
  }

  implicit val listFunctor = new Functor[List] {
    override def map[A, B](container: List[A])(f: A => B): List[B] = container.map(f)
  }
  implicit val OptionFunctor = new Functor[Option] {
    override def map[A, B](container: Option[A])(f: A => B): Option[B] = container.map(f)
  }
  implicit val TryFunctor = new Functor[Try] {
    override def map[A, B](container: Try[A])(f: A => B): Try[B] = container.map(f)
  }

  def do10x[C[_]](container: C[Int])(implicit f: Functor[C]): C[Int] = f.map(container)(_ * 10)

  println(do10x(List(1,2,3)))
  println(do10x(Option[Int](2)))
  println(do10x(Try[Int](8)))

  trait Tree[+T]
  case class Leaf[+T](value: T) extends Tree[T]
  case class Branch[+T](value: T, left: Tree[T], right: Tree[T]) extends Tree[T]

  object Tree {
    def leaf[T](value: T): Tree[T] = Leaf(value)
    def branch[T](value: T, left: Tree[T], right: Tree[T]) : Tree[T] = Branch(value, left, right)
  }

  implicit val treeFunctor = new Functor[Tree] {
    override def map[A, B](container: Tree[A])(f: A => B): Tree[B] = {
      container match {
        case Branch(value, left, right) => Branch(f(value), map(left)(f), map(right)(f))
        case Leaf(value) =>Leaf(f(value))
      }
    }
  }

  val tree =
    Tree.branch(1, Tree.branch(2, Tree.leaf(3), Tree.leaf(4)), Tree.leaf(5))
  println(tree)
  println(do10x(tree))
}
