package main.scala.com.rockthejvm

object WhyIsContraVarianceSoHard extends App {
  val x : List[Int] = List(1,2,3)  // List[+A]

  class Animal
  case class Dog(name:String) extends Animal
  case class Elephant(name:String)
  // above Dog <: Animal
  // is it the case that List[Dog] <: List[Animal]? The variance question

  val lassie = Dog("Lassie")
  val hachi = Dog("Hachi")
  val laika = Dog("Laika")
  val dogsList : List[Animal] = List(lassie, hachi, laika)
  // answer YES
  val dogsList2 = List(lassie, hachi, laika)

  // following fails to compile
  // error: You may wish to define T as +T instead. (SLS 4.5)
  //class MyInvariantList[T]  // invariant
  //val list : MyInvariantList[Animal] = new MyInvariantList[Dog]

  // following fails to compile
  // Elephant does not subclass Animal
//  class MyVariantList[+T]
//  val list2: MyVariantList[Animal] = new MyVariantList[Elephant]

  class MyContraVariantList[-T]
  val contravariant_list : MyContraVariantList[Dog] = new MyContraVariantList[Animal]
  // following fails to compile
  // rhs must be higher up the hiearachy or same
//  val variant_list : MyContraVariantList[Animal] = new MyContraVariantList[Dog]

  // contravariance example
  trait Vet[-T] {
    def heal(animal: T):Boolean = {true}
  }

  // An animal vet can heal a dog
  val vet1 = new Vet[Animal] {}
  vet1.heal(lassie)

  // A dog vet can heal a dog
  val vet2 = new Vet[Dog]{}
  vet2.heal(lassie)

  // but a elephant vet cannot heal a dog
  // so contravariance required a 'more general' ability
  // the following will not compile
//  val vet3 = new Vet[Elephant]{}
//  vet3.heal(lassie)


  // rule of thumb
  // use covariance +T if generic type creates or contains elements of type T
  // use contravariance -T if generic type acts or consumes elements of type T
}
