package com.rockthejvm

object Kinds {

  // kinds = type of types

  val aNumber: Int = 42 // Level 0 types == kind ..
  case class Person(name: String, age: Int) // Level 0 can be attached to a value

  class LinkedList[T] {  // 'generic' aka level-1
    //level-1 cannot be attached to a value
    // level-1 takes argument of level-0 kind
  }

  val aList : LinkedList[Int] = ???
  // This is now a level-0 kind since it is attached to a value
  // we used level-1 type with  level-0 to create level-0

  // level-2 type example
  class Functor[F[_]]
  // this is a level-2 kind since it has defined in terms of level-1
  val functorList = new Functor[List]
  // now a level-0 type since level-2 used with level-1

  // level-3 type example
  class Meta[F[_[_]]] // level-3
  val meta = new Meta[Functor]

  //
  class HashMap[K,V] // level-1
  val aHashMap = new HashMap[Int, String]

  //
  class ComposedFunctor[F[_], G[_]] // level-2
  val aComposedFunctor = new ComposedFunctor[List, LinkedList]

  //
  class Formatter[F[_], T] // level-2
  val aFormatter = new Formatter[List, String]

}
