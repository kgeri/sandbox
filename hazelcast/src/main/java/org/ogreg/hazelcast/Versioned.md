# Hazelcast batch put with optimistic locking

## Use-case

* We want to ingest large volumes of data (eg. from a file or MQ)
* Possibly coming from multiple threads / processes, and possibly in batches of a few dozen
* The processing must be idempotent
* We can assume there's a globally unique sequence number (version) on each entry - when it's smaller than what we already have, it's safe to ignore that entry

As of Hazelcast 5.0, there's no support for optimistic locking for batch operations. There is [replace](https://docs.hazelcast.com/hazelcast/5.0/data-structures/locking-maps#optimistic-locking)
of course, but it's not _quite_ what we need:
* Can only update one entry at a time (latency!)
* Unaware of version numbers - it's just Compare-And-Swap - so forces one to do a `get-replace-get`... loop and check the version on the _client_ side (even more latency!!)

But Hazelcast partitions have a dedicated owner that can update entries, locally in a thread-safe way... it's a _beautiful model_, we should utilize that!

All we really need is the _nodes_ to be aware of versioning, and just simply ignore `put`s if they are outdated.

So how can we do that? I found two options:

## Option 1: EntryProcessor

Have a look at [VersioningEntryProcessor](VersioningEntryProcessor.java).

The idea is that we use `executeOnKeys(Set<K>, EntryProcessor<K, V>)` to **serialize and submit the entire batch** to **all the nodes**. Then, 
`EntryProcessor` will run on each node and each partition locally, and see whether the entry on the current key in the batch should be:
* Created
* Updated
* Ignored, because it's newer

See also [versioningEntryProcessorCorrectness()](../../../../../test/java/org/ogreg/hazelcast/VersioningTest.java).

**Benefits**:
* Clean solution, `EntryProcessor` was meant to solve issues like this

**Drawbacks**:
* You need to serialize the entire batch, and it will be sent to _every node_
* Actually, the _entire VersioningEntryProcessor_ must be serializable... watch out if you use custom serializers (write one for this class, too)!
* Using it is a bit cumbersome

## Option 2: MapStore

**IMPORTANT**: as of 5.2.2, below is not a feasible solution, because it deadlocks on `IMap.get` (which kind of makes sense, because we're accessing
a map entry on the same key we're currently trying to update). Well it was an ugly hack anyway. I've put its test on `@Disabled`.

But `putAll` is so nice... can't we just use that, and make the nodes aware of versioning somehow?

Reading the Hazelcast code, we can employ a devilish hack: `MapListener`s run out-of-band (after the `put`, on a separate thread), but custom `MapStore`
implementations run _before_ the put operation gets applied on the partition. They can even modify the entry (if they implement `PostProcessingMapStore`), 
however they can't tell the store to _ignore_ the new value.

But they can throw exceptions :) in fact that part is documented ("If an exception is thrown then the put operation will fail.").

So what if we:
* Enable and configure a special [VersioningMapStore](VersioningMapStore.java)
* We implement its `store` method in a way that it throws an [OptimisticLockingException](VersioningMapStore.java) if the new version is not newer

Well, it works! One thing to watch out for is stack traces - those are expensive to create, we don't want to litter the logs with them, and we know 
where `OptimisticLockingException` came from... so it implements an empty `fillInStackTrace`.

See also [versioningMapStoreCorrectness()](../../../../../test/java/org/ogreg/hazelcast/VersioningTest.java).

**Benefits**:
* The client is blissfully unaware of everything - just calls `putAll`
* Probably uses less bandwidth (**TODO**: measure), as `putAll` _might_ be smart enough to split up the batch by partition key and send them directly to the individual owners
* No additional serialization hacks needed

**Drawbacks**:
* An obvious hack. `MapStores` are not meant for this... we had to enable a feature totally unrelated to versioning
* Still results in `ERROR` level logs on the cluster, which you'll have to ignore
