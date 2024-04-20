package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ActorsIntro extends App {

  // part1 - actor systems
  // heavyweight data structure that controls a number of threads under the hood
  // which then allocates to running actors
  // should be 1 per application
  val actorSystem = ActorSystem("firstActorSystem")
  println(actorSystem.name)

  // create actor
  // send message
  // may or may not response back
  // actors are uniquely identified
  // message are async
  // actors are (really) encapsulated

  class WordCountActor extends Actor {
    // definition of an Actor:
    // 1. internal data
    var totalWords = 0

    // 2. behavior
    // def receive: PartialFunction[Any, Unit] ...
    def receive: Receive = {
      case message: String =>
        println(s"[word counter] I have received: $message")
        totalWords += message.split(" ").length
      case msg => println(s"[word counter] I cannot understand ${msg.toString}")
    }
  }

  // part3 - instantiate our actor
  val wordCounter = actorSystem.actorOf(Props[WordCountActor], "wordCounter") // this return an Actor reference
  // uniquely identified, no 2 actors with the same name under the same actor system
  // fully encapsulated, interact through actor ref by sending message
  val anotherWordCounter = actorSystem.actorOf(Props[WordCountActor], "anotherWordCounter")

  // part4 - communicate!
  wordCounter ! "I am learning Akka and it's pretty damn cool!" // the ! is usually said "tell" method
  anotherWordCounter ! "A different message"
  // asynchronous!


  object Person {
    def props(name: String) = Props(new Person(name))
  }

  class Person(name: String) extends Actor {
    override def receive: Receive = {
      case "hi" => println(s"Hi, my name is $name")
      case _ =>
    }
  }

  // instantiate Actor with constructor?
  // 1. passing a props with an actor instance inside: not recommended
  // val person = actorSystem.actorOf(Props(new Person("Bob")))
  // although new Person("Bob") alone is not allowed
  // 2. create a companion object: best practice
  val person = actorSystem.actorOf(Person.props("Bob"))
  person ! "hi"


  class SimpleActor extends Actor {
    override def receive: Receive = {
      case "Hi!" => context.sender() ! "Hello,there!" // reply to a message
      // context.sender() = sender()
      case message: String => println(s"[simple actor] I have received $message")
      case number: Int => println(s"[${context.self}] I have received $number")
      // context.self: Actor[ackka://...simpleActor#-123123123] (name of actor)#identifier
      // context.self = self
      case SpecialMessage(contents) => println(s"[simple actor] I have received something Special: $contents")
      case SendMessageToYourself(content) =>
        self ! content // then being processed by the case message
      case SayHiTo(ref) => ref ! "Hi!" //(ref ! "Hi!")(self) // self is implicit value, can be ommit
      case WirelessPhoneMessage(content, ref) => ref forward (content + "s") // I keep the original sending of the class
    }
  }
  val simpleActor = actorSystem.actorOf(Props[SimpleActor], "simpleActor")
  simpleActor ! "hello, actor"
  simpleActor ! 42 // <- Akka retrieve this object and then invoke it with the argument that you have used for sending
  // 1 - can send, "tell"  almost any type! primitive type and custom type with the condition:
  // a) message must be IMMUTABLE
  // b) messages must be SERIALIZABLE: means that JVM can transform it into a byte stream and send it to another JVM,
  //    whether it is on the same machine or over the network
  //    there are numbers of serialize protocol
  //    but in practice we often use case class and case object, solve most of our need

  case class SpecialMessage(contents: String)
  simpleActor ! SpecialMessage("some special content")

  // 2 - actors have information about their context and about themselves
  // context.self == `this` in OOP == self

  case class SendMessageToYourself(content: String)
  simpleActor ! SendMessageToYourself("I am an actor and I am proud of it")

  // 3 - actors can REPLY to messages
  val alice = actorSystem.actorOf(Props[SimpleActor], "alice")
  val bob = actorSystem.actorOf(Props[SimpleActor], "bob")

  case class SayHiTo(ref: ActorRef)
  alice ! SayHiTo(bob)

  // 4 - dead letters
  alice ! "Hi!" // sender is Actor.noSender = null, failed to reply, moved to dead letter

  // 5 - forwarding messages
  // D -> A -> B
  // sending a message with the ORIGINAL sender
  case class WirelessPhoneMessage(content: String, ref: ActorRef)
  alice ! WirelessPhoneMessage("Hi", bob)

  /**
   * Excercises
   *
   * 1. a Counter actor
   *   - Increment
   *   - Decrement
   *   - Print
   */

  class Counter extends Actor {
    var count = 0
    override def receive: Receive = {
      case Increment(amount) => count += amount
      case Decrement(amount) => count -= amount
      case "show" => println(s"[counter] $count")
    }
  }

  case class Increment(amount: Int)
  case class Decrement(amount: Int)

  val counter = actorSystem.actorOf(Props[Counter], "counter")
  counter ! Increment(10)
  counter ! Increment(10)
  counter ! "show"
  counter ! Decrement(20)
  counter ! "show"

  /**
   * Excercises
   *
   * 2. a Back account as an actor
   *   receives
   *   - Deposit an amount
   *   - Withdraw an amount
   *   - Statement
   *   replies with
   *   - Success
   *   - Failure
   *
   *   some other kind of actor will check the result either Success / Failure and print the result
   */
  class Bank extends Actor {
    var balance = 0
    var statement = ""
    override def receive: Receive = {
      case Deposit(amount) => {
        balance += amount
        statement = s"$statement" + s"\nDeposit: $amount, Balance: $balance"
        println("Success")
      }
      case Withdraw(amount) => {
        if (amount >= balance) {
          balance -= amount
          statement = s"$statement" + s"\nWithdraw: $amount, Balance: $balance"
          println("Success")
        } else {
          println("Failure")
        }
      }
      case "Statement" => println(statement)
    }
  }

  case class Deposit(amount: Int)
  case class Withdraw(amount: Int)

  val bank = actorSystem.actorOf(Props[Bank], "bank")
  bank ! Deposit(10)
  bank ! Withdraw(10)
  bank ! Deposit(10)
  bank ! Deposit(20)
  bank ! "Statement"

}
