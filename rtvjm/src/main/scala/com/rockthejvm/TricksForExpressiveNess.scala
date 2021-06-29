package com.rockthejvm

import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue

object TricksForExpressiveNess extends  App {

  // single abstract method pattern

  trait Action {
    def act(x: Int) : Int
    def impl(x:Int) : Int = ???
  }

  val anAction : Action = (x:Int) => x + 1

  // 2 - right associative methods (magic comes from a : at the end)
  val predendedElement = 2 :: List(1,3)

  1::2::3::List() == 1::(2::(3::List()))

  List().::(3).::(2).::(1)

  class MessageQueue[T] {
    def  -->: (value: T) : MessageQueue[T] = new MessageQueue[T]
  }

  val queue = new MessageQueue[Int]
  3 -->: 2 -->: queue

  // 3 - backed in setters, magic is in _

  class MutableIntWrapper {
    private var internalValue = 0
    def value = internalValue
    def value_=(n: Int) = internalValue = n
  }

  val wrapper = new MutableIntWrapper
  wrapper.value
  wrapper.value = 4

  // 3 = multi-word members
  case class Person(name: String) {
    def `then said`(thing: String) = s"$name said, then said $thing"
  }
  val jim = Person("jim")
  jim `then said` "Scala is awesome"

    // example Akka HTTP

  // 4 = pattern matching

  val meaningOfLife = 42
  val data : Any = 45

  val pm = data match {
    case meaningOfLife => ???
  }
  val pm2 = data match {
    case x if x == meaningOfLife => ???
  }
  val pm3 = data match {
    case `meaningOfLife` => ???
  }
}
