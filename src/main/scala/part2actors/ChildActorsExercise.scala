package part2actors

import akka.actor.{Actor, ActorRef, Props}
import akka.actor.ActorSystem

object ChildActorsExercise extends App {

  // Distributed Word counting
  object WordCounterMaster {
    case class Initialize(nChildren: Int)
    case class WordCountTask(id: Int, text: String)
    case class WordCountReply(id: Int, count: Int)
  }

  class WordCounterMaster extends Actor {
    import WordCounterMaster._

    override def receive: Receive = {
      case Initialize(nChildren) =>
        val childrenRefs = for (i <- 1 to nChildren) yield context.actorOf(Props[WordCounterWorker], s"child$i")
        context.become(withChildren(childrenRefs, 0, 0, Map()))
    }

    def withChildren(childrenRefs: Seq[ActorRef], currentChildIndex: Int, currentTaskId: Int, requestMap: Map[Int, ActorRef]): Receive = {
      case message: String =>
        childrenRefs(currentChildIndex) ! WordCountTask(currentTaskId, message) // why forward not works?
        val newTaskId = currentTaskId + 1
        // why need requestMap?
        // because master will forward message to children, when child reply result in L37, sender() is child
        // need to save requestMap to save the original requester
        val newRequestMap = requestMap + (currentTaskId -> sender())
        context.become(withChildren(childrenRefs, (currentChildIndex + 1) % childrenRefs.length, newTaskId, newRequestMap))
      case WordCountReply(id, count) =>
        // send the reply to the original requester
        requestMap(id) ! count
        context.become(withChildren(childrenRefs, currentChildIndex, currentTaskId, requestMap - id))
    }
  }

  class WordCounterWorker extends Actor {
    import WordCounterMaster._

    override def receive: Receive = {
      case WordCountTask(id, text) =>
        println(s"${self.path} I have received task $id with $text")
        sender() ! WordCountReply(id, text.split(" ").length)
    }
  }

  class TestActor extends Actor {
    import WordCounterMaster._

    override def receive: Receive = {
      case "go" =>
        val master = context.actorOf(Props[WordCounterMaster], "master")
        master ! Initialize(3)
        val texts = List("I love Akka", "Scala is super dope", "yes", "me too")
        texts.foreach(text => master ! text)
      case count: Int =>
        println(s"[test actor] I received a reply: $count")
    }
  }

  val system = ActorSystem("roundRobinWordCountExercise")
  val testActor = system.actorOf(Props[TestActor], "testActor")
  testActor ! "go"

  /*
    create WordCounterMaster
    send Initialize(10) to wordCounterMaster
    send "Akka is awesome" to wordCounterMaster
      wcm will send a WordCountTask("...") to one of its children
        child replies with a WordCountReply(3) to the master
      master replies with 3 to the sender.

    requester -> wcm -> wcw
           r  <- wcm <-

   */
  // round robin logic
  // 1,2,3,4,5 and 7 tasks
  // 1,2,3,4,5,1,2
}
