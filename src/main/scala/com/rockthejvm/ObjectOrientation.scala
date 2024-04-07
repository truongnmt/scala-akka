package com.rockthejvm

object ObjectOrientation extends App {

  // java equivalent: public static void main(String[] args)

  // class and instance
  class Animal {
    // define fields
    val age: Int = 0
    // define methods
    def eat() = println("I'm eating")
  }

  val anAnimal = new Animal

  // inheritance
  // inherit all its members and methods
  class Dog(val name: String) extends Animal // constructor definition
  val aDog = new Dog("Lassie")

  // constructor arguments are NOT fields: need to put a val before the constructor argument
  aDog.name

  // subtype polymorphism
  val aDeclaredAnimal: Animal = new Dog("Hachi")
  aDeclaredAnimal.eat() // the most derived method will be called at runtime

  // abstract class
  // not all fields or methods need to have implementation
  // class that extend need to write implementation
  abstract class WalkingAnimal {
    val hasLegs = true // by default public, can restrict by adding protected or private
    def walk(): Unit
  }

  // "interface" = ultimate abstract type, meaning leave everything unimplemented
  trait Carnivore {
    def eat(animal: Animal): Unit
  }

  trait Philosopher {
    def ?!(thought: String): Unit // valid method name
  }

  // single-class inheritance, multi-trait "mixing"
  // mix-in multiple trait
  class Crocodile extends Animal with Carnivore with Philosopher {
    override def eat(animal: Animal): Unit = println("I am eating you, animal!")

    override def ?!(thought: String): Unit = println(s"I was thinking: $thought")
  }

  val aCroc = new Crocodile
  aCroc.eat(aDog)
  aCroc eat aDog // infix notation = object method argument, only available for methods with ONE argument
  aCroc ?! "What if we could fly?"

  // operators in Scala are actually methods (like Ruby?)
  val basicMath = 1 + 2
  val anotherBasicMath = 1.+(2) // equivalent

  // anonymous classes
  val dinosaur = new Carnivore {
    override def eat(animal: Animal): Unit = println("I am a dinosaur so I can eat pretty much anything")
  }

  /*
    What you tell the compiler:

    class Carnivore_Anonymous_35728 extends Carnivore {
      override def eat(animal: Animal): Unit = println("I am a dinosaur so I can eat pretty much anything")
    }

    val dinosaur = new Carnivore_Anonymous_35728
   */

  // singleton object
  object MySingleton { // the only instance of the MySingleton type
    val mySpecialValue = 53278
    def mySpecialMethod(): Int = 5327

    def apply(x: Int): Int = x + 1 // can specify in object, class as well
  }

  MySingleton.mySpecialMethod()
  MySingleton.apply(65)
  MySingleton(65) // equivalent to MySingleton.apply(65)
  // allow instance of a class to be invoked like function

  object Animal { // companions - companion object
    // companions can access each other's private fields/methods
    // singleton Animal and instances of Animal are different things
    // companion object defined in the same file, same class
    val canLiveIndefinitely = false
  }

  val animalsCanLiveForever = Animal.canLiveIndefinitely // "static" fields/methods

  /*
   case classes = lightweight data structures with some boilerplate
   - sensible equals and hash code
   - serialization (send these instance over the wire, then serialize when received)
   - companion with apply
   - pattern matching
  */
  case class Person(name: String, age: Int)
  // may be constructed without new
  val bob = Person("Bob", 54) // Person.apply("Bob", 54)

  // exceptions
  try {
    // code that can throw
    val x: String = null
    x.length
  } catch { // in Java: catch(Exception e) {...}
    case e: Exception => "some faulty error message"
  } finally {
    // execute some code no matter what
  }

  // generics
  abstract class MyList[T] {
    def head: T
    def tail: MyList[T]
  }

  // using a generic with a concrete type
  val aList: List[Int] = List(1,2,3) // List.apply(1,2,3)
  val first = aList.head // 1
  val rest = aList.tail // List(2, 3)
  // Int already has the implementation of first and tail

  val aStringList = List("hello", "Scala")
  val firstString = aStringList.head // string

  // Point #1: in Scala we usually operate with IMMUTABLE values/objects
  // Any modification to an object must return ANOTHER object
  /*
    Benefits:
    1) works miracles in multithreaded/distributed env
    2) helps making sense of the code ("reasoning about")
   */
  val reversedList = aList.reverse // returns a NEW list
  // NG: aList.reverse without assignment

  // Point #2: Scala is closest to the OO ideal, althoght has mixing with functional programming


}