package com.rockthejvm

object ObjectsAndCompanions  {

  class Person(firstName: String, lastName : String) {

  }

  object MySingleton

  object ClusterSingleton {
    val MAX_NOTES = 20
    def numberOfNodes() : Int = {42}
  }

  val naxNodes = ClusterSingleton.numberOfNodes()

  class Kid(name: String, age: Int) {
    def greet(): String = s"Hello, my name is $name and I'm $age years old. ${Kid.LIKES_VEGETABLES}"
  }

  object Kid {
    private val LIKES_VEGETABLES : Boolean = false
  }


  def main(args: Array[String]): Unit = {

  }
}
