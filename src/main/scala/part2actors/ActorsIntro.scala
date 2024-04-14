package part2actors

import akka.actor.{Actor, ActorSystem, Props}

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



}
