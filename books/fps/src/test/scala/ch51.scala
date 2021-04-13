import org.scalactic.{Bad, Good, Or}
import org.scalatest.funsuite.AnyFunSuite

import scala.util.{Failure, Success, Try}

class ch51 extends AnyFunSuite{

  def toIntOption(s:String): Option[Int] = {
    try Some(s.trim.toInt)
    catch {
      case _: Exception => None
    }
  }

  def toIntTry(s:String) : Try[Int] = Try[Int](s.trim.toInt)

  def toIntEither(s:String) : Either[String, Int] = {
    try {
      Right(s.trim.toInt)
    }
    catch {
      case e: Exception => Left(e.toString)
    }
  }

  def toIntOr(s:String) : Or[Int, String]  = {
    try {
      Good(s.trim.toInt)
    }
    catch {
      case e: Exception => Bad(e.toString)
    }
  }



  test("test") {
    val r  = toIntOption("7")
    assert(r == Some(7))
    r match {
      case Some(i) => assert(i == 7)
      case None => assert(false)
    }
  }

  test("test2"){
    val r = toIntOption("f")
    assert(r == None)
  }

  test("optionWithFor"){
    val generator = for {
      i <- toIntOption("1")
      j <- toIntOption("2")
      k <- toIntOption("2")
    } yield i + j + k

    generator match {
      case Some(x) => assert(true)
      case None => println("Not able to convert")
    }
  }

  test("tryInt") {
    val s = toIntTry("7")
    s match {
      case Success(value) => assert(value == 7)
      case Failure(x) => assert(false)
    }

    val r = toIntTry("z")
    r match {
      case Success(x) => assert(false)
      case Failure(exception) => assert(true); println(exception)
    }
  }

  test("eitherInt") {
    val s = toIntEither("7")
    s match {
      case  Right(i) => assert(i == 7)
      case Left(r) => println(r); assert(false)
    }

    val r = toIntEither("z")
    r match {
      case Right(i) => println(i); assert(false)
      case Left(r) => println(r); assert(r.nonEmpty)
    }
  }

  test("orInt") {
    val s = toIntOr("7")
    s match {
      case Good(g) => assert(g == 7)
      case Bad(b) => println(b) ; assert(false)
    }

    val r = toIntOr("z")
    r match {
      case Good(x) => assert(false)
      case Bad(b) => println(b) ; assert(true)
    }
  }


}
