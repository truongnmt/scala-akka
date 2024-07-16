Event sourcing

Why?
How do you query a previous state? How did you arrive to this state?

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











