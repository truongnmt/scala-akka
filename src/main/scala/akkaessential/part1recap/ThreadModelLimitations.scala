package akkaessential.part1recap

import java.util.concurrent.Executors
import scala.concurrent.{ExecutionContext, Future}

object ThreadModelLimitations {

  // Daniel's rants
  // DR #1: OO encapsulation is only valid in the SINGLE-THREADED MODEL

  class BankAccount(private var amount: Int) {
    override def toString = s"$amount"

    def withdraw(money: Int) = synchronized {
      this.amount -= money
    }

    def deposit(money: Int) = synchronized {
      this.amount += money
    }

    def getAmount = amount
  }

  val account = new BankAccount(2000)
  val depositThreads = (1 to 1000).map(_ => new Thread(() => account.deposit(1)))
  val withdrawThreads = (1 to 1000).map(_ => new Thread(() => account.withdraw(1)))

  def demoRace(): Unit = {
    (depositThreads ++ withdrawThreads).foreach(_.start())
    Thread.sleep(1000)
    println(account.getAmount)
  }

  /*
    - we don't know when the threads are finished
    - race conditions

    solution: synchronization, lock to the rescue!
    other problems:
      - deadlocks
      - livelocks
      - more complex, perf problem
   */

  // => need a datastructure: fully encapsulated, with no locks

  // DR #2 - delegating a task to a thread is a PAIN
  // eg: you have a running thread and you want to pass a runnable to that thread
  var task: Runnable = null

  val runningThread: Thread = new Thread(() => {
    while (true) {
      while (task == null) {
        runningThread.synchronized {
          println("[background] waiting for a task")
          runningThread.wait()
        }
      }

      task.synchronized {
        println("[background] I have a task!")
        task.run()
        task = null
      }
    }
  })

  def delegateToBackgroundThread(r: Runnable) = {
    if (task == null) {
      task = r
      runningThread.synchronized {
        runningThread.notify()
      }
    }
  }

  def demoBackgroundDelegation(): Unit = {
    runningThread.start()
    Thread.sleep(1000)
    delegateToBackgroundThread(() => println("I'm running from another thread"))
    Thread.sleep(1000)
    delegateToBackgroundThread(() => println("This should run in the background again"))
  }
  // => DIY pub/sub is messy, problems:
  // how we can do with other signals, like run x times a second
  // multiple backgorund tasks and threads: how to you identify which thread gets whith task from running thread
  // identify who gave signal
  // what if crash

  // => need a data structure which
  // can safetly receive messages
  // can identify the sender
  // is easily identifiable
  // can uard against errors

  // DR #3: tracing and dealing with errors is a PITN (pain in the neck) in multithreaded/distributed apps

  implicit val ec: ExecutionContext = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(8))
  // sum 1M numbers in between 10 threads

  val futures = (0 to 9)
    .map(i => BigInt(100000 * i) until BigInt(100000 * (i + 1))) // 0 - 99999, 100000 - 199999, and so on
    .map(range => Future {
      // bug
      if (range.contains(BigInt(546732))) throw new RuntimeException("invalid number")
      range.sum
    })

  val sumFuture = Future.reduceLeft(futures)(_ + _) // future with the sum of all the numbers

  def main(args: Array[String]): Unit = {
    sumFuture.onComplete(println)
  }
}