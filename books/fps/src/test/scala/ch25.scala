import org.scalatest.funsuite.AnyFunSuite

import java.io.{ByteArrayOutputStream, PrintStream}

class ch25 extends AnyFunSuite {
  // By name parameters

  def takes_by_name[R](block: => R) = {
    block
  }

  def timer_and_result[R](block: => R) :(Long,R) = {
    val startTime = System.nanoTime()
    val result = block
    val elapsedTime = System.nanoTime() - startTime
    (elapsedTime, result)
  }

  test("code_block_no_result") {
    val r = takes_by_name({println("Hello")})
    assert(r.equals(()))
  }

  test("code_block_int_result") {
    val r = takes_by_name({val i = 0; i})
    assert(r == 0)
  }

  test("code_block_that_prints") {
    val printThis = "foo"
    val myOut = new ByteArrayOutputStream();
    val prs = new PrintStream(myOut)
    Console.withOut(prs) {
    takes_by_name({println(printThis)})
    assert(myOut.toString.startsWith( printThis))
    }
  }

  test("timer_no_result") {
    val (t,r) = timer_and_result({for (i <- 1 to 1000) {}})
    assert(t > 0)
    assert(r == ())
  }

  test("timer_with_result") {
    val (t,r) = timer_and_result({for (i <- 1 to 1000) {}; 5.toInt})
    assert(t > 0)
    assert(r == 5)
  }
}
