package  X

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success}

class IO[A] private(constructorCodeBlock: => A) {

    def run = constructorCodeBlock

    def flatMapOrig[B](f: A => IO[B]): IO[B] = IO(f(run).run)

    def flatMap[B](customFmapAlgorithm: A => IO[B]): IO[B] = {
      val res1: IO[B] = customFmapAlgorithm(run)
      val res2: B = res1.run
      IO(res2)
    }

    def map[B](f: A => B): IO[B] = flatMap(a => IO(f(a)))

  }

  object IO {
    def apply[A](a: => A): IO[A] = new IO(a)
  }

  trait Monad[M[_]] {

    // FP’ers prefer to call this `point`
    def lift[A](a: => A): M[A]

    def flatMap[A, B](ma: M[A])(f: A => M[B]): M[B]

    def map[A, B](ma: M[A])(f: A => B): M[B] = flatMap(ma)(a => lift[B](f(a)))
  }

  case class StateT[M[_], S, A](run: S => M[(S, A)]) {
    def flatMap[B](g: A => StateT[M, S, B])(implicit M: Monad[M]): StateT[M, S, B] = StateT { (s0: S) =>
      M.flatMap(run(s0)) {
        case (s1, a) => g(a).run(s1)
      }
    }

    def map[B](f: A => B)(implicit M: Monad[M]): StateT[M, S, B] = flatMap(a => StateT.point(f(a)))
  }

  object StateT {
    def point[M[_], S, A](v: A)(implicit M: Monad[M]): StateT[M, S, A] = StateT(run = s => M.lift((s, v)))
  }

  object LoopWithoutQuitNoDebug extends App {

    def getLine(): IO[String] = IO(scala.io.StdIn.readLine())

    def putStr(s: String): IO[Unit] = IO(print(s))

    def toInt(s: String): Int = {
      try {
        s.toInt
      } catch {
        case e: NumberFormatException => 0
      }
    }

    // a class to track the sum of the ints that are given
    case class SumState(sum: Int)

    // an implementation of the `Monad` trait for the `IO` type
    implicit val IOMonad = new Monad[IO] {
      def lift[A](a: => A): IO[A] = {
        IO(a)
      }

      def flatMap[A, B](ma: IO[A])(f: A => IO[B]): IO[B] = ma.flatMap(f)
    }

    /**
     * given the int `i`, add it to the previous `sum` from the given SumState `s`;
     * then return a new state `newState`, created with the new sum;
     * at the end of the function, wrap `newState` in an `IO`;
     * the anonymous function creates a `StateT` wrapped around that `IO`.
     */
    def doSumWithStateT(newValue: Int): StateT[IO, SumState, Int] = StateT { (oldState: SumState) =>

      // create a new sum from `i` and the previous sum from `s`
      val newSum = newValue + oldState.sum

      // create a new SumState
      val newState: SumState = oldState.copy(sum = newSum)

      // return the new state and the new sum, wrapped in an IO
      IO(newState, newSum)
    }

    /**
     * the purpose of this function is to “lift” an IO action into the StateT monad.
     * given an IO instance named `io` as input, the anonymous function transforms
     * the `IO[A]` into an `IO[(SumState, A)]`; that result is then wrapped in a `StateT`.
     */
    def liftIoIntoStateT[A](io: IO[A]): StateT[IO, SumState, A] = StateT { s: SumState =>
      io.map(a => (s, a)) //IO[(SumState, A)]
    }

    // new versions of the i/o functions that uses StateT
    def getLineAsStateT(): StateT[IO, SumState, String] = liftIoIntoStateT(getLine)

    def putStrAsStateT(s: String): StateT[IO, SumState, Unit] = liftIoIntoStateT(putStr(s))

    /**
     * you have to kill this loop manually (i.e., CTRL-C)
     */
    def sumLoop: StateT[IO, SumState, Unit] = for {
      _ <- putStrAsStateT("\ngive me an int: ")
      input <- getLineAsStateT
      _ <- if (input == "q") {liftIoIntoStateT(IO(Unit))} else for {
        i <- liftIoIntoStateT(IO(toInt(input)))
        _ <- doSumWithStateT(i)
        _ <- sumLoop
      } yield Unit
    } yield Unit

    val result: (SumState, Unit) = sumLoop.run(SumState(0)).run

    // this line won't be reached because you have to kill the loop manually
    println(s"Final SumState: ${result._1}")

  }

object Foo extends App {
  import scala.concurrent.ExecutionContext.Implicits.global

  case class User(name: String)
  case class Address(postCode: String)
  def findUserById(id: Long): Future[Option[User]] = {Future(Option(User("Foo")))}
  def findAddressByUser(user: User): Future[Option[Address]] = Future(Option(Address("10092")))
  def findAddressByUserId(id: Long): Future[Option[Address]] =
    findUserById(id).flatMap {
      case Some(user) => findAddressByUser(user)
      case None       => Future.successful(None)
    }

  def findAddressByUserId2(id: Long): Future[Option[Address]] = {
    (for {
      user <- FutOpt(findUserById(id))
      address <- FutOpt(findAddressByUser(user))
    } yield address).value
  }

  case class FutOpt[A](value:Future[Option[A]]) {
    def map[B](f : A => B) : FutOpt[B] = {
      FutOpt(value.map(v => v.map(f)))
    }

    def flatMap[B](f: A => FutOpt[B]): FutOpt[B] = {
      val r = value.flatMap(opt => opt match {
        case Some(a) => f(a).value
        case None => Future.successful(None)
      })
      FutOpt(r)
    }
  }

  val r = findAddressByUserId(1)
  r.onComplete(r2 =>
  r2 match {
    case Success(value) => println("on complete", value)
    case Failure(exception) => println("error")
  })

  Await.result(r, 1000 millis)
  r.foreach(x=> {println("foreach", x)})
  r.foreach( x => assert(x match { case Some(value) => value.postCode == "10092"}))

  val r2 = findAddressByUserId2(1)
  Await.result(r2,  1000 millis)
  r2.foreach(x => assert(x match {case Some(value) => value.postCode == "10092"}))

  import cats.data.OptionT

  def findAddressByUserId3(id: Long) : Future[Option[Address]] = {
    (
      for {
        user <- OptionT(findUserById(id))
        address <- OptionT(findAddressByUser(user))
      } yield address
    ).value
  }

  def findAddressByUserId4(id: Long) : OptionT[Future,Address] = {
      for {
        user <- OptionT(findUserById(id))
        address <- OptionT(findAddressByUser(user))
      } yield address
  }

  val r3 = findAddressByUserId3(1)
  Await.result(r3,  1000 millis)
  r3.foreach(x => assert(x match {case Some(value) => value.postCode == "10092"}))


  val r4 = findAddressByUserId4(1)
  Await.result(r4.value,  1000 millis)
  r4.value.foreach(x => assert(x match {case Some(value) => value.postCode == "10092"}))
}



