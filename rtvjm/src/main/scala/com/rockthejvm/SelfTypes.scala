package com.rockthejvm

object SelfTypes extends App {
  trait Edible

  trait Person {
    def hasAllergiesTo(thing: Edible) : Boolean
  }

  trait Child  extends Person
  trait Adult extends Person

  trait Diet {
    this: Person => // == must mixin Person as well
    def eat(thing: Edible) : Boolean = {
      if (this.hasAllergiesTo(thing)) false else true
    }
  }

  trait Carnivore extends Diet with Person
  trait Vegetarian extends Diet with Person

  // PROBLEM: Diet should only be applicable to Person only
    class VegetarianAthlete extends Vegetarian with Adult {
      override def hasAllergiesTo(thing: Edible): Boolean = ???
    }

  // #1 Junior style - will add Person trait to Diet
  // #2 Pass type information
  // #3 self.type
}
