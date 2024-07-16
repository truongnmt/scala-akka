package akkaessential.part5infra

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}

import scala.concurrent.{ExecutionContext, Future}

/*
  Dispatchers in charge of:
    - delivering and handling message within actor system
    - creating the threads for the actors
    - managing a thread pool
    - assigning tasks to the actors

  Every dispatcher will have an executor, which is a JVM thread handler - which handles which messages are handled on which thread
  - thread-pool-executor: for CPU-bound actors
  - fork-join-executor: for blocking actors

  Dispatcher throughput: number of messages that a dispatcher can handle for one actor until that thread moves to another actor
  - dispatcher has pool size, a x number of threads in pool
  - those x threads, each thread handle one actor, consume messages up to the number of configured throughput then move to another actor

  Actor are simply not scheduled until the dispatcher decides to allocate a thread for them

  Dispatcher binds actors to a thread pool.

*/

object Dispatchers extends App {

  class Counter extends Actor with ActorLogging {
    var count = 0

    override def receive: Receive = {
      case message =>
        count += 1
        log.info(s"[$count] $message")
    }
  }

  val system = ActorSystem("DispatchersDemo") // , ConfigFactory.load().getConfig("dispatchersDemo")

  // method #1 - programmatic/in code
  val actors = for (i <- 1 to 10) yield system.actorOf(Props[Counter].withDispatcher("my-dispatcher"), s"counter_$i")
  //  val r = new Random()
  //  for (i <- 1 to 1000) {
  //    actors(r.nextInt(10)) ! i
  //  }

  // method #2 - from config
  val rtjvmActor = system.actorOf(Props[Counter], "rtjvm")

  /**
   * Dispatchers implement the ExecutionContext trait
   */

  class DBActor extends Actor with ActorLogging {
    // solution #1
    implicit val executionContext: ExecutionContext = context.system.dispatchers.lookup("my-dispatcher")
    // solution #2 - use Router

    override def receive: Receive = {
      case message => Future {
        // wait on a resource
        Thread.sleep(5000)
        log.info(s"Success: $message")
      }
    }
  }

  val dbActor = system.actorOf(Props[DBActor])
  //  dbActor ! "the meaning of life is 42"

  val nonblockingActor = system.actorOf(Props[Counter])
  for (i <- 1 to 1000) {
    val message = s"important message $i"
    dbActor ! message
    nonblockingActor ! message
  }
}