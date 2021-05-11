import org.scalatest.funsuite.AnyFunSuite

import java.awt.Color

class ch104 extends AnyFunSuite{

  trait  Animal
  abstract class AnimalWithTail(tailColor: Color) extends Animal
  trait DogTailServices {
    this: AnimalWithTail =>
    def wagTail = println("wag Tail")
    def lowerTail = println("lower Tail")
    def raiseTail = println("raise Tail")
  }
  trait DogMouthServices {
    this: AnimalWithTail =>
    def bark = println("bark")
    def lick = println("lick")
  }

  object IrishSetter extends AnimalWithTail(Color.RED) with DogTailServices with DogMouthServices

  test("IrishSetter") {
    IrishSetter.bark
    IrishSetter.wagTail
  }

}
