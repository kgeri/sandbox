# Distributed, partitioned calculation - with polling!

As an improvement to [Distributed, partitioned calculation](CalculatorService.md), this work investigates whether it'd be possible to **pull** tasks
from the distributed and partitioned `IMap`.

## The problem with `CalculatorService`

It feels more complicated than it could be :) Could we get rid of that `MigrationListener` and the `ExecutorService`'s implicit queue?

## Finding a way to make an IMap behave like BlockingQueue

The problem with pulling tasks is that we need an efficient way to query a few elements from the `IMap`. We'd like to have a `Queue.take()` method,
really, that'd block until there's a new element. We also must make sure to look at **local entries only**, otherwise the nodes would replicate the
work.

A few APIs that I considered but then rejected:

* `IMap.localKeySet` sounded promising, but looking into the code it's horribly wasteful. It is essentially running a **query operation**, and
  returning a clone of the results (resulting in so-so much garbage to be collected). This makes it prohibitive for polling.
* `IMap.iterator(int)` would be nice, but it returns non-local entries as well (ew, network calls!).
* `IMap.getLocalMapStats()` might be useful to reduce the impact of polling, but even that returns a copy (facepalm)

It soon turned out that the public APIs are too limited. So time to dig in :)

Interesting things I found out along the way:

* There's an awesome little method (used for cache expiration) that does _exactly_ what I
  need: `com.hazelcast.map.impl.recordstore.Storage.getRandomSamples(int)`
* You can get to it
  via `HazelcastInstanceImpl -> Node -> NodeEngine -> MapService -> MapServiceContext -> PartitionContainer[] -> RecordStore[] -> Store[]` :)
* You'd think that the ownerships are like `Hazelcast -> IMap[mapName] -...-> partitioned data`, but it's actually
  `Hazelcast -> MapServiceContext -> PartitionContainer[] -> RecordStore[mapName]`
* This has the interesting consequence of Hazelcast _not distinguishing between owned and backup entries_ below the `MapServiceContext` level. The
  `RecordStore` has both!
* The way to find out whether something is currently (!) owned or a backup is by looking at `MapServiceContext.getOrInitCachedMemberPartitions()`,
  which is essentially a `BitSet` (nice!). It's not actually the _entries_ that are migrated when nodes come and go, but rather entire `Partitions`...
  in retrospect this makes a lot of sense!

So the scheme I came up with is:

* Create a `workQueue` for the tasks (an `IMap` keyed by whatever partition key you like)
* Start a single `dispatcher` thread per node, that does the polling (calling `Storage.getRandomSamples(int)` across all `RecordStore`s)
    * First this checks whether the given partitionId (the index of the `PartitionContainer` array) is ours or not
      with `getOrInitCachedMemberPartitions()`
    * Then it does the `getRandomSamples` trick, feeding the entries into an `ArrayBlockingQueue` **with a fixed capacity**
    * This means the dispatcher will block until the workers do the work
    * The size of this dispatch queue will be tiny (which means we're okay to lose it if the node goes down - and on startup we'd automatically poll
      again anyway)
    * If the dispatcher didn't find anything, it goes to sleep for a bit (to avoid spinning on the `Store`)
* Start N `worker` queues, which just do a `BlockingQueue.take()` in an infinite loop, and of course remove the task from the `IMap` before they start
  calculating it

The implementation can be found in [CalculatorServiceWithPolling](CalculatorServiceWithPolling.java), you can try it out
with [CalculationWithPollingTest](../../../../../test/java/org/ogreg/hazelcast/CalculationWithPollingTest.java).

Benefits:

* Using less memory than the previous solution (as there's no need for an `ExecutorService` queue, which could grow as large as the local entries)
* Quicker recovery from nodes joining/leaving, as it does not need a `MigrationListener`

Drawbacks:

* Relies on tons of internal API that are also very deep and might change anytime :(
