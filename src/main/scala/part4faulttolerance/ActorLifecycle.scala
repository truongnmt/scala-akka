package part4faulttolerance

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}

object ActorLifecycle extends App {

  /**
    Actor instance: has method, may have internal state
    Actor reference:
       created with actorOf
      has mailbox and can receive messages
       contain 1 actor instance
       contain a UUID
    Actor path: may or may not have an actor reference

    Actor can be
      started   create a new ActorRef with a UUID at a given path
      suspended the actor ref will enqueue but NOT process more message
      resumed   actor ref will process more messages
      restarted internal state is destroyed on restart
            suspend
            swap the actor instance
              old instance calls preRestart
              replace actor instance
              new instance calls postRestart
            resume
      stopped  frees the actor ref within a path
            calls postStop
            all watching actors receive Terminated(ref)
            all watching actors receive Terminated(ref)
      after stopping, another actor may be created at the same path
            different UUID, so different ActorRef


  **/

  object StartChild
  class LifecycleActor extends Actor with ActorLogging {

    override def preStart(): Unit = log.info("I am starting")
    override def postStop(): Unit = log.info("I have stopped")

    override def receive: Receive = {
      case StartChild =>
        context.actorOf(Props[LifecycleActor], "child")
    }
  }

  val system = ActorSystem("LifecycleDemo")
  //  val parent = system.actorOf(Props[LifecycleActor], "parent")
  //  parent ! StartChild
  //  parent ! PoisonPill

  /**
   * restart
   */

  object Fail
  object FailChild
  object CheckChild
  object Check

  class Parent extends Actor {
    private val child = context.actorOf(Props[Child], "supervisedChild")

    override def receive: Receive = {
      case FailChild => child ! Fail
      case CheckChild => child ! Check
    }
  }

  class Child extends Actor with ActorLogging {

    override def preStart(): Unit = log.info("supervised child started")
    override def postStop(): Unit = log.info("supervised child stopped")

    override def preRestart(reason: Throwable, message: Option[Any]): Unit =
      log.info(s"supervised actor restarting because of ${reason.getMessage}")

    override def postRestart(reason: Throwable): Unit =
      log.info("supervised actor restarted")

    override def receive: Receive = {
      case Fail =>
        log.warning("child will fail now")
        throw new RuntimeException("I failed")
      case Check =>
        log.info("alive and kicking")
    }
  }

  val supervisor = system.actorOf(Props[Parent], "supervisor")
  supervisor ! FailChild
  supervisor ! CheckChild

  // supervision strategy
  // even if the child actor threw an exception when processing a message, the child still restarted and was able to
  // process more messages
  // if a an actor threw an exception while processing a message, this message, which caused the exception to be thrown,
  // is removed from the queue and not put back in mailbox again, and the actor is restarted, which mean the mail box is?
}