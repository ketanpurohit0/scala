import org.scalatest.funsuite.AnyFunSuite

import java.io.FileNotFoundException
import java.net.MalformedURLException
import scala.util.{Failure, Success, Try}

class ch79 extends AnyFunSuite{

  def using[A, B <: {def close(): Unit}] (closeable: B) (f: B => A): A =
    try { f(closeable) } finally { closeable.close() }


  def readFile(fileName: String) : Try[List[String]] = {
    Try {
      val lines = using (io.Source.fromFile(fileName)) { source =>
        (for (line <- source.getLines()) yield line).toList
      }
      lines
    }
  }

  test("test_read_no_exist"){
    val contents  = readFile("foobar.txt")
    contents match {
      case Success(result) => assert(false)
      case Failure(exception: FileNotFoundException) => assert(true)
      case e: Throwable => assert(false, e.getClass)
    }
  }

  test("test_file_exists") {
    val contents = readFile(System.getProperty("user.dir") + "/src/test/scala/ch79.scala")
    contents match {
      case Success(result) => assert(result.length > 0)
      case Failure(exception: FileNotFoundException) => assert(false)
      case e: Throwable => assert(false, e.getClass)
    }
  }

}
