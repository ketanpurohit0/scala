
class IOM[A] private(constructorCodeBlock: => A) {

  def run = constructorCodeBlock

  def flatMapOrig[B](f: A => IOM[B]): IOM[B] = IOM(f(run).run)

  def flatMap[B](customAlgorithm: A => IOM[B]): IOM[B] = {
    val result1: IOM[B] = customAlgorithm(run)
    val result2: B = result1.run
    IOM(result2)
  }

  def map[B](f: A => B): IOM[B] = flatMap(a => IOM(f(a)))

}

object IOM {
  def apply[A](a: => A): IOM[A] = new IOM(a)
}

object io_monad {

  def getLine: IOM[String] = IOM(scala.io.StdIn.readLine())
  def putStrLn(s: String): IOM[Unit] = IOM(println(s))
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

  def forExpression : IOM[Unit] = for {
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

  def forExpression : IOM[String] = for {
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

object IOTestRec extends App {
  def forExpression : IOM[Unit] = for {
    _ <- io_monad.putStrLn("Enter a string?")
    inputStr <- io_monad.getLine
    _ <- io_monad.putStrLn(s"You entered $inputStr")
    _ <- if (inputStr == "quit") IOM(Unit) else forExpression
  } yield Unit

  forExpression.run
}