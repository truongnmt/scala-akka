package akkaessential.part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import ChangingActorBehavior.Mom.MomStart

object ChangingActorBehavior extends App {

  import ActorsIntro.{Counter, actorSystem}

  object FussyKid {
    case object KidAccept
    case object KidReject
    val HAPPY = "happy"
    val SAD = "sad"
  }
  class FussyKid extends Actor {
    import FussyKid._
    import Mom._
    // internal state of the kid
    var state = HAPPY
    override def receive: Receive =  {
      case Food(VEGETABLE) => state = SAD
      case Food(CHOCOLATE) => state = HAPPY
      case Ask(_) =>
        if (state == HAPPY) sender() ! KidAccept
        else sender() ! KidReject
        // problem 1: usually different kinds of behavior depending on the state of actor
        // the state of actor might be complex, logic for handling message might blow up to hundreds of lines
        // problem 2: use state that less mutable, stateless
    }
  }

  class StatelessFussyKid extends Actor {
    import FussyKid._
    import Mom._

    override def receive: Receive = happyReceive

    def happyReceive: Receive = {
      case Food(VEGETABLE) => context.become(sadReceive) // change my receive handler to sadReceive
      case Food(CHOCOLATE) =>
      case Ask(_) => sender() ! KidAccept
    }

    def sadReceive: Receive = {
      case Food(VEGETABLE) => // stay sad
      case Food(CHOCOLATE) => context.become(happyReceive) // change my receive handler to happyReceive
      case Ask(_) => sender() ! KidReject
    }

    // basically switching message handler base on input, no internal state
    // message comes in order
  }

  object Mom {
    case class MomStart(kidRef: ActorRef)
    case class Food(food: String)
    case class Ask(messages: String) // do you want to play
    val VEGETABLE = "veggies"
    val CHOCOLATE = "chocolate"
  }
  class Mom extends Actor {
    import FussyKid._
    import Mom._

    override def receive: Receive = {
      case MomStart(kidRef) =>
        // test our interacton
        kidRef ! Food(VEGETABLE)
        kidRef ! Ask("Do you want to play")
      case KidAccept => println("Yay, my id is happy!")
      case KidReject => println("My kid is sad, but as he's healthy")
    }
  }

  val system = ActorSystem("changingActorBehaviorDemo")
  val fussyKid = system.actorOf(Props[FussyKid])
  val statelessFussyKid = system.actorOf(Props[StatelessFussyKid])
  val mom = system.actorOf(Props[Mom])

  mom ! MomStart(statelessFussyKid)

  /*
    Food(veg) -> message handler turns to sadReceive
    Food(chocolate) -> become happyReceive

    specify boolean to context.become(_, true/false)
    instead of replacing the old message handler, we simply stack the new message handler onto a stack of message handler

    context.become(_, false)
    Food(veg) -> handler turns to sadReceive: stack.push(sadReceive), put sadReceive on top
    Food(chocolate) -> stack.push(happyReceive)

    Stack:
    1. happyReceive
    2. happyReceive -> sadReceive
    3. happyReceive

    when actor needs to handle message, aka will call the topmost message handler onto the stack
    if stack empty, call plain receive method

    to pop the message handler, use context.unbecome()

    =>>>>>>>>>>>>>>>

    Use context.become() and unbecome() to changing actor message handler to get rid of actors mutable state.
    context.become() default true: replace current handler(default)
                     pass false to stack new handler on top

    Rules:
      Akka always uses the latest handlr on top of the stack
      if the stack is empty call plain receive

   */


  /**
   * Excercise
   * 1. recreate Counter without mutable state
   */

  object Counter {
    case object Increment
    case object Decrement
    case object Print
  }
  class Counter extends Actor {
    import Counter._
//    var count = 0
    override def receive: Receive = countReceive(0)

    def countReceive(currentCount: Int): Receive = {
      case Increment =>
        println(s"$currentCount incrementing")
        context.become(countReceive(currentCount + 1))
      case Decrement =>
        println(s"$currentCount decrementing")
        context.become(countReceive(currentCount - 1))
      case Print => println(s"[counter] my current count is $currentCount")
    }
    // does this become stack overflow???
  }

  import Counter._
  val counter = system.actorOf(Props[Counter], "counter")
  (1 to 5).foreach(_ => counter ! Increment)
  (1 to 3).foreach(_ => counter ! Decrement)

  /**
   * Excercise 2 - a simplified voting system
   */

  case class Vote(candidate: String)
  case object VoteStatusRequest
  case class VoteStatusReply(candidate: Option[String])
  class Citizen extends Actor {
//    var candidate: Option[String] = None
    override def receive: Receive = {
      case Vote(c) => context.become(voted(c)) // candidate = Some(c)
      case VoteStatusRequest => sender() ! VoteStatusReply(None)
    }

    def voted(candidate: String): Receive = {
      case VoteStatusRequest => sender() ! VoteStatusReply(Some(candidate))
    }
  }

  case class AggregateVotes(citizens: Set[ActorRef])
  class VoteAggregator extends Actor {
//    var stillWaiting: Set[ActorRef] = Set()
//    var currentStats: Map[String, Int] = Map()

    override def receive: Receive = awaitingCommand

    def awaitingCommand: Receive = {
      case AggregateVotes(citizens) =>
//        stillWaiting = citizens
        citizens.foreach(citizenRef => citizenRef ! VoteStatusRequest)
        context.become(awaitingStatuses(citizens, Map()))
    }

    def awaitingStatuses(stillWaiting: Set[ActorRef], currentStats: Map[String, Int]): Receive = {
      case VoteStatusReply(None) =>
        // a citizen hasn't voted yet
        sender() ! VoteStatusRequest // this might end up in an infinite loop
      case VoteStatusReply(Some(candidate)) =>
        val newStillWaiting = stillWaiting - sender()
        val currentVotesOfCandidate = currentStats.getOrElse(candidate, 0)
        val newStats = currentStats + (candidate -> (currentVotesOfCandidate + 1))
        if (newStillWaiting.isEmpty) {
          println(s"[aggregator] pool stats: $newStats")
        } else {
          // still need to process some statuses
          context.become(awaitingStatuses(newStillWaiting, newStats))
        }
    }
  }

  val alice = system.actorOf(Props[Citizen])
  val bob = system.actorOf(Props[Citizen])
  val charlie = system.actorOf(Props[Citizen])
  val daniel = system.actorOf(Props[Citizen])

  alice ! Vote("Martin")
  bob ! Vote("Jonas")
  charlie ! Vote("Roland")
  daniel ! Vote("Roland")

  val voteAggregator = system.actorOf(Props[VoteAggregator])
  voteAggregator ! AggregateVotes(Set(alice, bob, charlie, daniel))

}
