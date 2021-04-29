import org.scalatest.funsuite.AnyFunSuite
class ch75 extends AnyFunSuite{

  test("stdin") {


    for {
        _ <- io_monad.putStrLn("First name?")
        firstName <- io_monad.getLine
        _ <- io_monad.putStrLn("Last name?")
        lastName <- io_monad.getLine
        _ <- io_monad.putStrLn(s"$firstName, $lastName")
      } yield ()
  }


}
