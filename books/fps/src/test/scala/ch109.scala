import org.scalatest.funsuite.AnyFunSuite
import scala.util.{Success, Failure, Try}

class ch109 extends AnyFunSuite{

  def using[A, B <: {def close(): Unit}] (closeable: B) (f: B => A): A =
    try { f(closeable) } finally { closeable.close() }

  def readTextFileAsString(fileName: String) : Try[String] = {
    Try {
      val lines = using(io.Source.fromFile(fileName)) {
        source => (for (line <- source.getLines) yield line).toList
      }
      lines.mkString("\n")
    }
  }

  test("readFileDoesNotExist") {
    println(System.getProperty("user.dir"))
    val result = readTextFileAsString("xxgradlew.bat")
    result match {
      case Success(value) => assert(false)
      case Failure(exception) => assert(true)
    }
  }

  test("readFile") {
    val result = readTextFileAsString("gradlew.bat")
    val z = for {
      a <- result
    } yield a

    assert(z.isSuccess)
  }
}
