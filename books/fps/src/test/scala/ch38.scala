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

  def test_sum(l : List[Int]) : (Long, Long) = {
    timer(l.sum.toLong)
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
}
