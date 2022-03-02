trait Monoid[T] {
  val identity: T
  def op(l: T, r: T): T
}

package object Monoids {
  val integerAdd = new Monoid[Int] {
    override val identity: Int = 0

    override def op(l: Int, r: Int): Int = l + r
  }

  val integerMult = new Monoid[Int] {
    override val identity: Int = 1

    override def op(l: Int, r: Int): Int = l * r
  }

  val stringConcat = new Monoid[String] {
    override val identity: String = ""

    override def op(l: String, r: String): String = l + r
  }

  def composedMonoid[T, Y](tmonoid: Monoid[T], ymonoid: Monoid[Y]): Monoid[(T, Y)] = {
    new Monoid[(T, Y)] {
      override val identity: (T, Y) = (tmonoid.identity, ymonoid.identity)

//      override def op(l: (T, Y), r: (T, Y)): (T, Y) = {
//        (tmonoid.op(l._1, r._1), ymonoid.op(l._2, r._2))
//      }

      override def op(l: (T, Y), r: (T, Y)): (T, Y) = {
        (l, r) match {
          case ((lt, ly), (rt, ry)) => (tmonoid.op(lt, rt), ymonoid.op(ly, ry))
        }
      }
    }
  }

  def mappedMonoid[K, V](monoid: Monoid[V]): Monoid[Map[K, V]] = {
    new Monoid[Map[K, V]] {
      override val identity: Map[K, V] = Map()

      override def op(l: Map[K, V], r: Map[K, V]): Map[K, V] = {
        (l.keySet ++ r.keySet).foldLeft(identity) {
          case (mapped, k) => {
            mapped.updated(k, monoid.op(l.getOrElse(k, monoid.identity), r.getOrElse(k, monoid.identity)))
          }
        }
      }
    }
  }
}

object MonoidOps {
  def fold[T](list: List[T], monoid: Monoid[T]) = list.foldLeft(monoid.identity) { monoid.op }

  def fold1[T](list: List[T], monoid: Monoid[T]) = foldMap2(list, monoid) { identity }
  def fold2[T](list: List[T], monoid: Monoid[T]) = foldMap(list, monoid) { identity }

  def foldMap[T, Y](list: List[T], monoid: Monoid[Y])(f: T => Y): Y = {
    list.map(f).foldLeft(monoid.identity) { monoid.op }
  }

  def foldMap2[T, Y](list: List[T], monoid: Monoid[Y])(f: T => Y): Y = {
    list.foldLeft(monoid.identity) { case (y, t) =>
      monoid.op(y, f(t))
    }
  }

  def splitFold[T, Y](list: List[T], monoid: Monoid[Y])(f: T => Y): Y = {
    list.length match {
      case 0 => monoid.identity
      case 1 => f(list(0))
      case len => {
        val (leftSplit, rightSplit) = list.splitAt(len / 2)
        monoid.op(splitFold(leftSplit, monoid)(f), splitFold(rightSplit, monoid)(f))
      }
    }
  }
}

import Monoids._
object MonoidFolding extends App {
  val strings = List("a", "b", "c")
  val numbers = List(1, 2, 3, 4)
  val stringZero = ""
  //
  val rs = strings.foldLeft(stringZero) {
    case (currentValue, nextValue) => {
      println(currentValue, nextValue)
      currentValue + nextValue
    }
  }
  println(rs)

  //
  val numberZero = 0
  val rn = numbers.foldLeft(numberZero) {
    case (currentValue, nextValue) => {
      println(currentValue, nextValue)
      currentValue + nextValue
    }
  }
  println(rn)

  val rs2 = strings.foldLeft(stringConcat.identity) { stringConcat.op }
  println(rs2)

  //
  val rs3 = MonoidOps.fold[String](strings, stringConcat)
  val rs4 = MonoidOps.fold[Int](numbers, integerAdd)
  val rs5 = MonoidOps.fold[Int](numbers, integerMult)
  val rs6 = MonoidOps.fold1[String](strings, stringConcat)
  val rs7 = MonoidOps.fold2[Int](numbers, integerAdd)
  println(rs3, rs4, rs5, rs6, rs7)

  val rs8 = MonoidOps.foldMap[String, Int](strings, integerAdd) { s => s.length }
  val rs9 = MonoidOps.foldMap2[String, Int](strings, integerAdd) { s => s.length }
  println(rs8, rs9)

  // splitFold
  val rs10 = MonoidOps.splitFold(List.fill(300)("A"), integerAdd) { s => s.length }
  println(rs10)

  // compose monoid
  val composedMn = composedMonoid(integerAdd, integerMult)
  val rcmn1 = MonoidOps.splitFold(numbers, composedMn) { n => (n, n) }
  println(rcmn1)

  val ccMn = composedMonoid(integerAdd, composedMn)
  val rcmn2 = MonoidOps.splitFold(numbers, ccMn) { n => (1, (n, n)) }
  println(rcmn2)

}

object MonoidFeatureCountApp extends App {
  val features = List("Apple", "Apple", "Bananas", "Pear", "Orange", "Orange")
  val mapMonoid = mappedMonoid[String, Int](integerAdd)
  val rs = MonoidOps.splitFold(features, mapMonoid) { i => Map(i -> 1) }
  println(rs)
}
