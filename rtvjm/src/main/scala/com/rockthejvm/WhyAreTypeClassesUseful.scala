package main.scala.com.rockthejvm

object WhyAreTypeClassesUseful  {

//  def processMyList[T](list: List[T]): T = {
//    // ints - sum
//    // str - concatenate
//    // others - errors
//    // how - see processMyListUsingSummable?
      // type-class is a pattern
      // trait combined with one-more instances of that trait
      // provide specific implementations aka template specialisation
      // this leads to ad-hoc polymorphism
      // ad-doc = needs presence of an implicit
      // polymorphism = due to use of generic
//  }

  trait Summable[T] {
    def sumElements(list: List[T]): T
  }

  implicit object IntSummable extends Summable[Int] {
    override def sumElements(list: List[Int]): Int = list.sum
  }

  implicit object StringSummable extends Summable[String] {
    override def sumElements(list: List[String]): String = list.mkString(" ")
  }

  def processMyListUsingSummable[T](list: List[T])(implicit summable: Summable[T]): T = {
    // ints - sum
    // str - concatenate
    // others - errors
    // how ?
    return summable.sumElements(list)
  }


  def main(args: Array[String]): Unit = {
    println(processMyListUsingSummable(List(1,2,3)))
    println(processMyListUsingSummable(List("Scala", "is", "awesome")))
  }

}
