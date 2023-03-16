# Distributed, partitioned calculation - with even simpler polling!

This is a minor improvement to [Distributed, partitioned calculation - with polling!](CalculatorServiceWithPolling.md), please read the motivation and
details there.

## The problem with `CalculatorServiceWithPolling`

It _still_ feels more complicated than it could be :) Could we get rid of the dispatcher thread and that small `BlockingQueue`?

## Modulo to the rescue

Our scheme from the previous version only needs a tiny modification:

* Start `workerCount` `CalculationWorker` threads, which call `calculateNext` in an infinite loop:
    * For each partition, check whether our thread is the "owner" (`partitionId % workerCount != workerId`)
    * Check whether the given partitionId (the index of the `PartitionContainer` array) is ours or not
      using `InternalPartitionService.isPartitionOwner`
    * Then we get the `RecordStore`, and just to avoid creating/using objects, checks whether it's empty first
    * Then we traverse `RecordStore.iterator`, calling `calculate` on each entry
    * If we didn't find anything, we go to sleep for `workerBackoffMs` (to avoid spinning on the `RecordStore`)

The implementation can be found in [CalculatorServiceWithPollingV2](CalculatorServiceWithPollingV2.java), you can try it out
with [CalculationWithPollingTest](../../../../../test/java/org/ogreg/hazelcast/CalculationWithPollingTest.java).

Benefits compared to `CalculatorServiceWithPolling`:

* One less thread, plus threads are uniform
* No `BlockingQueue`

Drawbacks:

* Still relies on internal API that is also very deep and might change anytime :(
