package com.rockthejvm

object FunctionalProgramming extends App {

  // Scala is OO
  class Person(name: String) {
    def apply(age: Int) = println(s"I have aged $age years")
  }

  val bob = new Person("Bob")
  bob.apply(43)
  bob(43) // INVOKING bob as a function === bob.apply(43)


  /*
    Scala runs on the JVM
    - fundamentally build for java, JVM doesnt know that function is as first-class citizen
    In Functional programming, we want to:
    - compose functions
    - pass functions as args
    - return functions as results
    => stuff that we normally operate on when we work with object or plain value,
    well, we want to do that with functions as well

    Conclusion: FunctionX = Function1, Function2, ... Function22
   */

  val simpleIncrementer = new Function1[Int, Int] {
    override def apply(arg: Int): Int = arg + 1
  }
  // instantiated Function1 trait (name is not important)
  // the object can be invoked like a function because it has an apply method

  simpleIncrementer.apply(23) // 24
  simpleIncrementer(23) // simpleIncrementer.apply(23)
  // we defined a function! because the only thing it does is acts like a function

  // ALL SCALA FUNCTIONS ARE INSTANCES OF THESE FUNCTION_X TYPES

  // function with 2 arguments and a String return type
  val stringConcatenator = new Function2[String, String, String] {
    override def apply(arg1: String, arg2: String): String = arg1 + arg2
  }

  stringConcatenator("I love", " Scala") // "I love Scala"

  // syntax sugars
  // val doubler: Int => Int = (x: Int) => 2 * x
  val doubler = (x: Int) => 2 * x
  doubler(4) // 8

  /*
    equivalent to the much longer:

    val doubler: Function1[Int, Int] = new Function1[Int, Int] {
      override def apply(x: Int) = 2 * x
    }

    Function1[Int, Int] -> (Int => Int): 2 parameter, input Int, return Int
   */

  // higher-order functions: take functions as args/return functions as results
  val aMappedList: List[Int] = List(1,2,3).map(x => x + 1) // HOF???
  val aMappedList2: List[Int] = List(1,2,3).map((x: Int) => x + 1)
  val aMappedList3: List[Int] = List(1,2,3).map(new Function1[Int, Int] {
    override def apply(x: Int): Int = x +1
  })
  // [2, 3, 4]

  val aFlatMappedList = List(1,2,3).flatMap { x =>
    List(x, 2 * x)
  }
  // map then concat together
  // [1, 2] [2, 4] [3,6]
  // [1, 2, 2, 4, 4, 6]
  // alternative syntax, same as .flatMap(x => List(x, 2 * x))

  val aFilteredList = List(1,2,3,4,5).filter(_ <= 3) // equivalent to x => x <= 3

  // all pairs between the numbers 1, 2, 3 and the letters 'a', 'b', 'c'
  val allPairs = List(1,2,3).flatMap(number => List('a', 'b', 'c').map(letter => s"$number-$letter"))

  // for comprehensions
  val alternativePairs = for {
    number <- List(1,2,3)
    letter <- List('a', 'b', 'c')
  } yield s"$number-$letter"
  // equivalent to the map/flatMap chain above

  /**
   * Collections
   */

  // lists
  val aList = List(1,2,3,4,5)
  val firstElement = aList.head
  val rest = aList.tail
  val aPrependedList = 0 :: aList // List(0,1,2,3,4,5)
  val anExtendedList = 0 +: aList :+ 6 // List(0,1,2,3,4,5,6)
  // pros: adding/removing element from the head
  //       access item sequential
  //       prepending element to a list is O(1)
  // cons: access element by index is O(n)

  // sequences
  // depends on concrete implementation
  // vector random access O(1)
  val aSequence: Seq[Int] = Seq(1,2,3) // Seq.apply(1,2,3)
  val accessedElement = aSequence(1) // the element at index 1: 2

  // vectors: fast Seq implementation
  val aVector = Vector(1,2,3,4,5)

  // sets = no duplicates
  val aSet = Set(1,2,3,4,1,2,3) // Set(1,2,3,4)
  val setHas5 = aSet.contains(5) // false
  val anAddedSet = aSet + 5 // Set(1,2,3,4,5)
  val aRemovedSet = aSet - 3 // Set(1,2,4)

  // ranges
  val aRange = 1 to 1000
  val twoByTwo = aRange.map(x => 2 * x).toList // List(2,4,6,8..., 2000)

  // tuples = groups of values under the same value
  val aTuple = ("Bon Jovi", "Rock", 1982)

  // maps
  val aPhonebook: Map[String, Int] = Map(
    ("Daniel", 6437812),
    "Jane" -> 327285 // ("Jane", 327285)
  )


}