package com.rockthejvm

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

object Advanced extends App {

  /**
   lazy evaluation: an expression is not evaluated until it's first used
   */
  lazy val aLazyValue = 2
  lazy val lazyValueWithSideEffect = {
    println("I am so very lazy!")
    43
  }
  // the print in L14 won't be print because val is not used yet

  val eagerValue = lazyValueWithSideEffect + 1
  // print in L14 will be print because lazy val is used
  // => useful in infinite collections

  /**
   "pseudo-collections": Option, Try
   */
  def methodWhichCanReturnNull(): String = "hello, Scala"
  val anOption = Option(methodWhichCanReturnNull()) // Some("hello, Scala")
  // option = "collection" which contains at most one element: Some(value) or None

  val stringProcessing = anOption match {
    case Some(string) => s"I have obtained a valid string: $string"
    case None => "I obtained nothing"
  }
  // map, flatMap, filter

  def methodWhichCanThrowException(): String = throw new RuntimeException
  val aTry = Try(methodWhichCanThrowException())
  // a try = "collection" with either a value if the code went well, or an exception if the code threw one

  val anotherStringProcessing = aTry match {
    case Success(validValue) => s"I have obtained a valid string: $validValue"
    case Failure(ex) => s"I have obtained an exception: $ex"
  }
  // map, flatMap, filter


  /**
   * Evaluate something on another thread
   * (asynchronous programming)
   */
  val aFuture = Future {
    // this expression will be evaluated on another thread
    println("Loading...")
    Thread.sleep(1000)
    println("I have computed a value.")
    67
  } // equal to Future ({})
  // import scala.concurrent.ExecutionContext.Implicits.global
  // the global value is available to run this future
  // the global value is equivalent of a thread pool that is a collection of threads
  // on which we can schedule the evaluation of this expression

  // future is a "collection" which contains a value when it's evaluated
  // future is composable with map, flatMap and filter
  // (future Try and Option types are called monads,
  // BUT for simple, think of future try option as some sort of collection)

  /**
   * Implicits basics
   */
  // #1: implicit arguments
  def aMethodWithImplicitArgs(implicit arg: Int) = arg + 1
  implicit val myImplicitInt = 46
  println(aMethodWithImplicitArgs)  // aMethodWithImplicitArgs(myImplicitInt)
  // the compiler figures out that the method takes an implicit argument and
  // tries to find a value of type int that it can inject to method with implicit argument

  // #2: implicit conversions
  // to add methods to existing types over which we don't have any control over the code
  implicit class MyRichInteger(n: Int) {
    def isEven() = n % 2 == 0
  }

  println(23.isEven()) // new MyRichInteger(23).isEven()
  // use this carefully

}