package part4faulttolerance

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, PoisonPill, Props, Terminated}

object StartingStoppingActors extends App {

  val system = ActorSystem("StoppingActorsDemo")

  object Parent {
    case class StartChild(name: String)
    case class StopChild(name: String)
    case object Stop
  }

  class Parent extends Actor with ActorLogging {
    import Parent._

    override def receive: Receive = withChildren(Map())

    def withChildren(children: Map[String, ActorRef]): Receive = {
      case StartChild(name) =>
        log.info(s"Starting child $name")
        context.become(withChildren(children + (name -> context.actorOf(Props[Child], name))))
      case StopChild(name) =>
        log.info(s"Stopping child with the name $name")
        val childOption = children.get(name)
        childOption.foreach(childRef => context.stop(childRef))
        // non-blocking method, everything happen async (async)
        // send a signal to actor to stop, doesn't mean stop immediately
      case Stop =>
        log.info("Stopping myself")
        context.stop(self)
        // stop children first, wait until all children are stopped (async)
        // then stop child
      case message =>
        log.info(message.toString)
    }
  }

  class Child extends Actor with ActorLogging {
    override def receive: Receive = {
      case message => log.info(message.toString)
    }
  }

  /**
   * method #1 - using context.stop to stop actor
   */
  import Parent._
  //  val parent = system.actorOf(Props[Parent], "parent")
  //  parent ! StartChild("child1")
  //  val child = system.actorSelection("/user/parent/child1")
  //  child ! "hi kid!"
  //
  //  parent ! StopChild("child1")
  ////  for (_ <- 1 to 50) child ! "are you still there?" // child can still receive some message until it is actually stopped
  //
  //  parent ! StartChild("child2")
  //  val child2 = system.actorSelection("user/parent/child2")
  //  child2 ! "hi, second child"
  //
  //  parent ! Stop
  //  for (_ <- 1 to 10) parent ! "parent, are you still there?" // should not be received
  //  for (i <- 1 to 100) child2 ! s"[$i] second kid, are you still alive?"

  /**
   * method #2 - using special messages: PoisonPill Kill makes the actor throw an ActorKilledException
   */
  //  val looseActor = system.actorOf(Props[Child])
  //  looseActor ! "hello, loose actor"
  //  looseActor ! PoisonPill
  //  looseActor ! "loose actor, are you still there?"
  //
  //  val abruptlyTerminatedActor = system.actorOf(Props[Child])
  //  abruptlyTerminatedActor ! "you are about to be terminated"
  //  abruptlyTerminatedActor ! Kill
  //  abruptlyTerminatedActor ! "you have been terminated"

  /**
   *  Death watch: notify when actor die, like callback when received actor die message, can watch multiple actor, not just child actor
   */
  class Watcher extends Actor with ActorLogging {
    import Parent._

    override def receive: Receive = {
      case StartChild(name) =>
        val child = context.actorOf(Props[Child], name)
        log.info(s"Started and watching child $name")
        context.watch(child)
      case Terminated(ref) =>
        log.info(s"the reference that I'm watching $ref has been stopped")
    }
  }

  val watcher = system.actorOf(Props[Watcher], "watcher")
  watcher ! StartChild("watchedChild")
  val watchedChild = system.actorSelection("/user/watcher/watchedChild")
  Thread.sleep(500)

  watchedChild ! PoisonPill
}