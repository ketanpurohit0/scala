
class IO[A] private (constructorCodeBlock: => A) {

  def run = constructorCodeBlock

  def flatMapOrig[B](f: A => IO[B]): IO[B] = IO(f(run).run)

  def flatMap[B](customAlgorithm: A => IO[B]): IO[B] = {
    val result1: IO[B] = customAlgorithm(run)
    val result2: B = result1.run
    IO(result2)
  }

  def map[B](f: A => B): IO[B] = flatMap(a => IO(f(a)))

}

object IO {
  def apply[A](a: => A): IO[A] = new IO(a)
}

object io_monad {

  def getLine: IO[String] = IO(scala.io.StdIn.readLine())
  def putStrLn(s: String): IO[Unit] = IO(println(s))
}


object IOTest1 extends App {

  for {
    _         <- io_monad.putStrLn("First name?")
    firstName <- io_monad.getLine
    _         <- io_monad.putStrLn(s"Last name?")
    lastName  <- io_monad.getLine
    _         <- io_monad.putStrLn(s"First: $firstName, Last: $lastName")
  } yield ()

}

object IOTest2 extends App {

  def forExpression : IO[Unit] = for {
    _ <- io_monad.putStrLn("First name?")
    firstName <- io_monad.getLine
    _ <- io_monad.putStrLn("Last name?")
    lastName <- io_monad.getLine
    fNameUC = firstName.toUpperCase
    lNameUC = lastName.toUpperCase
    _ <- io_monad.putStrLn(s"First: $fNameUC, Last: $lNameUC")

  } yield Unit

  val r = forExpression.run
}

object IOTest3 extends App {

  def forExpression : IO[String] = for {
    _ <- io_monad.putStrLn("First name?")
    firstName <- io_monad.getLine
    _ <- io_monad.putStrLn("Last name?")
    lastName <- io_monad.getLine
    fNameUC = firstName.toUpperCase
    lNameUC = lastName.toUpperCase
    _ <- io_monad.putStrLn(s"First: $fNameUC, Last: $lNameUC")

  } yield fNameUC

  val r = forExpression.run
}