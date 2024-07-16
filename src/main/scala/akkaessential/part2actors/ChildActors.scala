package akkaessential.part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import ChildActors.CreditCard.{AttachToAccount, CheckStatus}

object ChildActors extends App {

  // Actors can create other actors

  object Parent {
    case class CreateChild(name: String)
    case class TellChild(message: String)
  }
  class Parent extends Actor {
    import Parent._

    override def receive: Receive = {
      case CreateChild(name) =>
        println(s"${self.path} creating child")
        // create a new actor right HERE
        val childRef = context.actorOf(Props[Child], name)
        context.become(withChild(childRef))
    }

    def withChild(childRef: ActorRef): Receive = {
      case TellChild(message) => childRef forward message
    }
  }

  class Child extends Actor {
    override def receive: Receive = {
      case message => println(s"${self.path} I got: $message")
    }
  }

  import Parent._

  val system = ActorSystem("ParentChildDemo")
  val parent = system.actorOf(Props[Parent], "parent")
  parent ! CreateChild("child")
  parent ! TellChild("hey Kid!")

  // self.path
  // parent: akka://ParentChildDemo/user/parent
  // child:  akka://ParentChildDemo/user/parent/child

  // akka can create another actor by context.actorOf
  // then use context.become to create message handler with

  // child creation feature enable to create actor hierarchies
  // can have hierarchies as many levels deep as you want and the parent can create as many child actors as it wants

  // parent -> child -> grandChild
  //        -> child2 ->

  /*
    Guardian actors (top-level)
    - /system = system guardian
    - /user = user-level guardian: akka://ParentChildDemo/user/parent
    - / = the root guardian, own both system and user-level actor
   */

  /**
   * Actor selection: find actor by path
   */
  val childSelection = system.actorSelection("/user/parent/child2") // no such actor found, message went to dead letter
  childSelection ! "I found you!"

  /**
   * Danger!
   *
   * NEVER PASS MUTABLE ACTOR STATE, OR THE `THIS` REFERENCE, TO CHILD ACTORS.
   *
   * NEVER IN YOUR LIFE.
   *
   * because suddenly has access to the parent actor's state and can mutate it or directly call methods on it without sending messages
   */


  object NaiveBankAccount {
    case class Deposit(amount: Int)
    case class Withdraw(amount: Int)
    case object InitializeAccount
  }
  class NaiveBankAccount extends Actor {
    import NaiveBankAccount._
    import CreditCard._

    var amount = 0

    override def receive: Receive = {
      case InitializeAccount =>
        val creditCardRef = context.actorOf(Props[CreditCard], "card")
        creditCardRef ! AttachToAccount(this) // passing instance to another actor, will lead to concurrency issues,
                                              // breaking actor principle
      case Deposit(funds) => deposit(funds)
      case Withdraw(funds) => withdraw(funds)

    }

    def deposit(funds: Int) = {
      println(s"${self.path} depositing $funds on top of $amount")
      amount += funds
    }
    def withdraw(funds: Int) = {
      println(s"${self.path} withdrawing $funds from $amount")
      amount -= funds
    }
  }

  object CreditCard {
    case class AttachToAccount(bankAccount: NaiveBankAccount) // instead of an actor ref, passing an instance of a actual JVM object
                                                              // correct (backAccountRef: ActorRef)
                                                              // incorrect (backAccount: NaiveBankAccount)
    case object CheckStatus
  }
  class CreditCard extends Actor {
    override def receive: Receive = {
      case AttachToAccount(account) => context.become(attachedTo(account))
    }

    def attachedTo(account: NaiveBankAccount): Receive = {
      case CheckStatus =>
        println(s"${self.path} your messasge has been processed.")
        // benign
        account.withdraw(1) // this is WRONG because child actor can call method directly on an actor and change state of parent actor
                            // only send messages
                            // this is called Closing over - close over mutable state or `this`
    }
  }

  import NaiveBankAccount._
  import CreditCard._

  val bankAccountRef = system.actorOf(Props[NaiveBankAccount], "account")
  bankAccountRef ! InitializeAccount
  bankAccountRef ! Deposit(100)

  Thread.sleep(500)
  val ccSelection = system.actorSelection("/user/account/card")
  ccSelection ! CheckStatus

  // WRONG!!!!!!
}