import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.must.Matchers.{be, equal}
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper

class Tests2 extends AnyFunSuite {

  test("Tests2") {

    val inputs = Seq(
      Array(1, 2, 2, 3, 3),
      Array(1, 4, 5, 6, 4, 3, 2, 4, 2, 4, 5).sorted
    )

    inputs.foreach(i => { println(p.delete_duplicates(i)); println(i.mkString(",")) })
  }
}

object p {
  def delete_duplicates(A: Array[Int]): Int = {
    if (A.length == 0) {
      return 0
    }
    var write_index = 1
    (1 until (A.length)).foreach { i =>
      if (A(write_index - 1) != A(i)) {
        A(write_index) = A(i)
        write_index += 1
      }

    }

    return write_index

  }
}
