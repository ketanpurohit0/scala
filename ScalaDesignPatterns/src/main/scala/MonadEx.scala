import java.io.{File, PrintWriter}
import scala.io.Source

sealed trait State {
  def next: State
}

abstract class FileIO {
  private class FileIOState(id: Int) extends State {
    override def next: State = new FileIOState(id + 1)
  }

  def runIO(readPath: String, writePath: String): IOAction[_]

  def run(args: Array[String]): Unit = {
    val action = runIO(args(0), args(1))
    action(new FileIOState(0))
  }
}

sealed abstract class IOAction[T] extends ((State) => (State, T)) {

  def unit[Y](value: Y): IOAction[Y] = IOAction.unit(value)

  def flatMap[Y](f: (T) => IOAction[Y]): IOAction[Y] = {
    val self = this
    new IOAction[Y] {
      override def apply(v1: State): (State, Y) = {
        val (state1, t) = self(v1)
        val action2 = f(t)
        action2(state1)
      }
    }
  }

  def map[Y](f: T => Y): IOAction[Y] = {
    flatMap(i => unit(f(i)))
  }
}

object IOAction {
  def apply[T](result: => T): IOAction[T] = new SimpleAction[T](result)

  def unit[T](value: T): IOAction[T] = new EmptyAction[T](value)

  private class EmptyAction[T](value: T) extends IOAction[T] {
    override def apply(v1: State): (State, T) = (v1, value)
  }
  private class SimpleAction[T](result: => T) extends IOAction[T] {
    override def apply(v1: State): (State, T) = (v1.next, result)
  }
}

package object io {
  def readFile(path: String) = IOAction(Source.fromFile(path).getLines())
  def writeFile(path: String, lines: Iterator[String]) = IOAction(
    {
      val file = new File(path)
      printToFile(file) { p => lines.foreach(p.println) }
    }
  )
  private def printToFile(file: File)(writeOp: PrintWriter => Unit) = {
    val writer = new PrintWriter(file)
    try {
      writeOp(writer)
    } finally {
      writer.close()
    }
  }

}
import io._
object MonadExApp extends FileIO with App {
  run(args)
  override def runIO(readPath: String, writePath: String): IOAction[_] = {
    for {
      rio <- readFile(readPath)
      _ <- writeFile(writePath, rio.map(_.toUpperCase))
    } yield ()
  }
}
