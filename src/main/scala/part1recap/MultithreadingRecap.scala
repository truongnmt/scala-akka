package part1recap

import scala.util.{Failure, Success}


object MultithreadingRecap extends App {

  import scala.concurrent.Future
  // creating threads on the JVM

  val aThread = new Thread(new Runnable {
    override def run(): Unit = println("I'm running in parallel")
  })
  val a1Thread = new Thread(() => println("I'm running in parallel")) // syntax sugar
  aThread.start()
  aThread.join() // method to wait a thread to finish

  val threadHello = new Thread(() => (1 to 1000).foreach(_ => println("hello")))
  val threadGoodbye = new Thread(() => (1 to 1000).foreach(_ => println("goodbye")))

  threadHello.start()
  threadGoodbye.start()
  // different ordering
  // different runs produce different results!

  class BankAccount(@volatile private var amount: Int) {
    override def toString: String = "" + amount

    def withdraw(money: Int) = this.amount -= money // not thread safe method

    // solution #1: add sync to thread, no 2 threads can go evaluate the expression,
    // block each other
    def safeWithdraw(money: Int) = this.synchronized {
      this.amount -= money
    }

    // solution #2: add volatile so that read is atomic
    // note that write are still not atomic and still need sync
    // only work for primative type
  }

  /*
  BA(10000)

  T1 -> withdraw 1000
  T2 -> withdraw 2000

  T1 -> this.amount = this.amount - ... // PREEMPTED by the OS
  T2 -> this.amount = this.amount - 2000 = 8000
  T1 -> -1000 = 9000

  => result = 9000

  this.amount = this.amount - 1000 is not ATOMIC
   */

  // inter-thread communicatino on the JVM
  // wait - notify mechanism

  // Scala Future
  import scala.concurrent.ExecutionContext.Implicits.global
  val future = Future {
    // long computation - on a different thread
    42
  }

  // what to do with future -> callbacks
  future.onComplete {
    case Success(42) => println("I found the meaning of life")
    case Failure(_) => println("smt happened with the meaning of Life")
  }

  val aProcessedFuture = future.map(_ + 1) // Future with 43
  val aFlatFuture = future.flatMap { value =>
    Future(value+2)
  } // Future with 44

  val filteredFuture = future.filter(_ % 2 == 0) // NoSuchElementException

  // for comprehensions
  val aNonsenseFuture = for {
    meaningOfLife <- future
    filteredMeaning <- filteredFuture
  } yield meaningOfLife + filteredMeaning

  // andThen, recover/recoverWith

  // Promises

  ///////////////////////////////////////////////

  // JVM threads
  // creating thread
  // synchronizing (blocking)
  // wait and notify

//  Scala Futures
//    creating futures, onComplete
//    monadic Future: map, flatMap, filter, for-comprehensions

//  Promises
//  completing Futures "manually"
}
