package Foo
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import scala.util.Either

object HelloWorld extends  App {
  val result = println("Hello World")
}

object IOMonadCats extends  App {

  def f(p: Either[Throwable, Unit]): Unit = {}
  val helloSlow = IO{println("Hello Async1"); Thread.sleep(1000); println("Hello Async2")}
  helloSlow.unsafeRunAsync(f)
  val helloFast = IO{ println("Hello World")}
  helloFast.unsafeRunSync()
  Thread.sleep(1000)

}

object Demo extends App {
  val program : IO[Unit] = for {
    _ <- IO{println("Welcome to scala, whats is your name?")}
    name <- IO{scala.io.StdIn.readLine()}
    nameUC = name.toUpperCase
    _ <- IO{println(s"Welcome ${nameUC}")}
  } yield Unit

  program.unsafeRunSync()
}
