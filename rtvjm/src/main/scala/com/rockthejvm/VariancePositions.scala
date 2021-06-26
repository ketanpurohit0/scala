package com.rockthejvm

object VariancePositions extends  App {

  class Animal
  class Dog extends Animal
  class Cat extends Animal
  class Crocodile extends Animal

  class MyList[+T]

  // Is MyList[Dog] also MyList[Animal] = variance question

  // if 1 - YES generic type is COVARIANT ie +T MyList[+T]
  val anAnimal : Animal = new Dog
  val animals: MyList[Animal] = new MyList[Dog]

  // 2 = no => generic type is INVARIANT MyList[T]

  // 3 = backwards ? generic type is CONTRAVARIANT
  class Vet[-T]
  val lassiesVet: Vet[Dog] = new Vet[Animal]

  // variance problem in following code - commented out as it will not compile
  // Covariant type T occurs in contravariant position in type T of value element
//  abstract class MyList2[+T] {
//    def head: T
//    def tail: MyList2[T]
//    def add(element: T) : MyList2[T]
//  }

  //class Vet2[-T](val favouriteAnimal: T)
//  val garfield = new Cat
//  val theVet : Vet2[Animal] = new Vet2[Animal](garfield)
//  val lassiesVet2 : Vet2[Dog] = theVet
//  // here the type of lassie is Dog
//  // but in actual facts its a Cat
//  // this is a conflict the compiler is trying to disallow
//  val lassie = lassiesVet2.favouriteAnimal
  // Same problem as above
//  class Vet3[-T](var favouriteAnimal: T)

  class MutableOption[+T](var contents: T)
  val maybeAnimal: MutableOption[Animal] = new MutableOption[Dog](new Dog)
  maybeAnimal.contents = new Cat

//    abstract class MyList2[+T] {
//      def add(element: T) : MyList2[T] = ???
//    }
//
//  val animal2: MyList2[Animal] = new MyList2[Cat]
//  val moreAnimal2 = animal2.add(new Dog)

  class Vet3[-T] {
    def heal(animal: T): Boolean = true
  }

  val lassiesVet3: Vet3[Dog] = new Vet3[Animal]
  lassiesVet3.heal(new Dog)
  //lassiesVet3.heal(new Cat)

//  abstract class Vet4[-T] {
//    def rescueAnimal(): T
//  }
//
//  val vet4: Vet4[Animal] = new Vet4[Animal] {
//    override def rescueAnimal(): Animal = new Cat
//  }
//
//  val lassiesVet4: Vet4[Dog] = vet4
//  val rescued: Dog = lassiesVet4.rescueAnimal()

      abstract class MyListCovariant[+T] {
        def add[S >: T](element: S ) : MyList[S] = new MyList[S]
      }
    abstract class MyListContra[-T] {
      def add[S <: T](element: S ) : MyList[S] = new MyList[S]
    }

  class VetC[-T] {
    def rescueAnimal[S <: T](): S = ???
  }

  val lassiesVetC  : VetC[Dog] = new VetC[Animal]
  val rescuedDogC: Dog = lassiesVetC.rescueAnimal()
}
