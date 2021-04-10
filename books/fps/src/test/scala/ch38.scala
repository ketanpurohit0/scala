import org.scalatest.exceptions.TestFailedException
import org.scalatest.funsuite.AnyFunSuite

import scala.annotation.tailrec

class ch38 extends AnyFunSuite{

  // Tail recursion

  def timer[R](codeBlock : => R) : (Long, R) = {
    val startTime = System.nanoTime()
    val r = codeBlock
    (System.nanoTime() - startTime, r)
  }

  private def sum_non_tail_recursive(l: List[Int]) : Long = {
      l match {
        case Nil => 0
        case x :: xs => x+sum_non_tail_recursive(xs)
      }
    }

  @tailrec
  private def sum_tail_recursive(l:List[Int], currentCumulativeSum : Long): Long = {
    l match {
      case Nil => currentCumulativeSum
      case x :: xs => sum_tail_recursive(xs, x + currentCumulativeSum)
    }
  }
  @tailrec
  private def product_tail_recursive(l:List[Int], currentCumulative : Double): Double = {
    l match {
      case Nil => currentCumulative
      case x :: xs => product_tail_recursive(xs, x * currentCumulative)
    }
  }
  @tailrec
  private def concate_tail_recursive(l:List[String], currentCumulative : String) : String  = {
    l match {
      case Nil => currentCumulative
      case x :: xs => concate_tail_recursive(xs, if (currentCumulative != "") currentCumulative + "," + x else x)
    }
  }

  def test_sum(l : List[Int]) : (Long, Long) = {
    timer(l.sum.toLong)
  }

  def test_prod(l : List[Int]) : (Long, Long) = {
    timer(l.product)
  }

  test("test_sum_non_tail_recursive") {
      val n = 100
      val r =  1 to n toList
      val (t,s) = timer(sum_non_tail_recursive(r))
      val expectedSum = n*(n+1)/2
      assert(s == expectedSum)
      println(s"elapsed ns: $t")
      // Use built in
      val (t1, s1) = test_sum(r)
      assert(s1 == expectedSum)
      println(s"elapsed ns: $t1")

  }


  test("test_sum_non_tail_recursive_StackOverflow"){
    assertThrows[StackOverflowError] {
      val n = 10000
      val r = 1 to n toList
      val (t, s) = timer(sum_non_tail_recursive(r))
      val expectedSum = n * (n + 1) / 2
      assert(s == expectedSum)
      println(s"elapsed ns: $t")
      // use built in
      val (t1, s1) = test_sum(r)
      assert(s1 == expectedSum)
      println(s"elapsed ns: $t1")

    }
  }

  test("test_sum_tail_recursive") {
    val n = 100
    val r =  1 to n toList
    val (t,s) = timer(sum_tail_recursive(r,0))
    val expectedSum = n*(n+1)/2
    assert(s == expectedSum)
    println(s"elapsed ns: $t")
    // use built in
    val (t1, s1) = test_sum(r)
    assert(s1 == expectedSum)
    println(s"elapsed ns: $t1")
  }


  test("test_sum_tail_recursive_StackOverflow"){
      val n = 1000000
      val r = 1 to n toList
      val (t, s) = timer(sum_tail_recursive(r, 0))
      val expectedSum:Long = n.toLong * (n.toLong + 1) / 2
      assert(s == expectedSum)
      println(s"elapsed ns: $t")
      // use built in
      assertThrows[TestFailedException] {
        // why.. because built in results in overflow of Max integer value
        val (t1, s1) = test_sum(r)
        assert(s1 == expectedSum)
        println(s"elapsed ns: $t1")
      }
  }

  test("prod") {
    val n = 50
    val l = 1 to n toList
    val (t,s) = timer(product_tail_recursive(l,1))
    //assert(s == l.product)
    println(s"elapsed ns: $t")
  }

  test("concate") {
    val n = 10
    val l = for (i <- n to 1 by -1 )  yield i.toString
    val (t,s) = timer(concate_tail_recursive(l.toList, ""))
    assert (s == l.mkString(","))
    println(s"elapsed ns: $t")
  }
}
