Event sourcing

Why?
How do you query a previous state?How did you arrive to this state?

Eg:
- tracing orders in an online store
- transactions history in a bank
- chat message

=>
Instead of storing current state, we will store events
We can always recreate the current state by replaying events.


pros:
- high performance: events are onlly appended
- avoid relation stores and ORM entirely
- fully trace of every state
- fits the Akka actor model perfectly

Cons:
- query a state potentially expensive becasuse we need to replay all the events <= can mitigate by Akka Persistance Query
- potential perf issues with long-lived entities because the stream of events might be extreamly large <= snapshotting
- data model subjecte to change <= schema evolution


A dedicated actor - Persistent Actors

Can do everything a normal actor can do
- send / receive messages
- hold internal state
- run in parallel with many other actors

Extra capabilities
- have a persistence ID
- persist event to a long term store
  recover state by replaing events from the store

When an actor handles a command
- it can async persist an event to the store
- after the event is persisted, it changes its internal state

When an actor starts/restarts
- it replays all events with its persistence ID

Persistent Actors:
- When you receive a command
  1) you create an EVENT to persist into the store
  2) you persist the event, then pass in a callback that will get triggered once the event is written
  3) we update the actor's state when the event has persisted
- safe to access mutable state in the callback
- receiveRecover is called to restore the actor's state from the persisted events
- mainly use API from akka-persistence library
- persist method is actually message passing using actor, so it is order guaranteed

Snapshots:
- why?: long lived entities take long time to recover -> save checkpoints
- with snapshots, not all peristed events are recovered, the latest snapshot and all events after that are recovered

PersistAsync:
- high throughput use cae, relaxed event ordering guarantee
- https://github.com/rockthejvm/akka-persistence/blob/master/src/main/scala/part2_event_sourcing/PersistAsyncDemo.scala

==============================

Akka Stream

Source = publisher
- emits elements async
- may or may not terminate
Sink = subscriber
- receives elements
- terminates only when publisher terminates
Flow = processer

Materializing Streams
- After constructing the RunnableGraph by connecting sources, sink and different operators, no data will flow through the stream until the graph is materialized.
- Materialization is the process of allocating resources to the stream, such as actors, threads, buffers, sockets, files, etc. (in Akka Streams this will often involve starting up Actors)
- When materialize a graph, all components can return a materialized value, but the result of running graph is a single materialized value, need to choose which value to return at the end
- Source(1 to 10).to(Sink.foreach(println)) -> by default, the leftmost source is returned which is source of 1 to 10
  - the materialized value returned from the graph above is NotUsed type
- we can choosing which materialized value we want to use by Keep.left/right, etc
- can reuse the same componeent in different graphs, different runs = different matterializations
  - => a component can materialized multiple times, 
- a materialized value can be ANYTHING
- viaMat toMat, can specify the direction; runWith will keep right

Operator Fusion and Async Boundaries
- by default, stream components are fused =  running on the same actor
  - simpleSource.via(simpleFlow).via(simpleFlow2).to(simpleSink).run()
  - run sequentially on the same actor
- async boundaries between stream components
  - simpleSource.via(simpleFlow).async.via(simpleFlow2).async.to(simpleSink).run()
  - components run on different actors
  - better throughput, best when individual components are expensive
  - avoid when components are cheap
  - ordering guarantees

Backpressure
- synchronization of speed between upstream and downstream asynchronus components
- if consumer are slow, consumer will send a signal to producer to slow down
- default buffer in Akka Streams is 16
- reactions to backpressure (in order):
  - try to slow down if possible
  - buffer elements until there's more demand
  - drop down elements from the buffer if it overflows
  - tear down/kill the whole stream (failure)
- backpressure protocol is transparent to the user

Graphs
- fanout: broadcast, balance, unzip
- fanin: merge, balance, zip, concat
- merge ~> balance -> sinks: takes 2 different sources emitting element at different speeds and events out the rate of production in between these 2 sources and split them equally in between two sinks
  - useful when has some sources of elements and you don't know their rate of production. But at the end of the graph, you would like to have a steady flow of elements.

Open Graphs
- 

Intergrating with external services
- mapAsync(parallelism = 4)
  - mapAsync applies a a function to each element and returns a Future
  - parallelism determines how many futures can run at the same time
  - if one future fails, the whole stream fails
  - relative ordering of elements is preserved, can use mapAsyncUnordered if ordering is not important
  - increasing parralelism can increase throughput, if you don't require the ordering, mapAsyncUnordered is even faster
  - mapAsync has to always wait for the futures to complete so that it can keep its order, if one of the futures is slow, it will slow down the whole stream, the futures are still evaluated in parallel, but mapAsync always waits for the future

Akka features that surprise me:
- mapAsync: consume concurrently, but keep the order
- graph DSL: very powerful, but not easy to understand
- async boundaries: part of the stream that runs on a different actor, improve throughput
- backpressure: transparent to the user
