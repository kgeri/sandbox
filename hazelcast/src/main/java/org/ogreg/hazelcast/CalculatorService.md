# Distributed, partitioned calculation

We want to use Hazelcast to its fullest (as an IMDG, rather than just a distributed cache).
This sample explores whether it's possible to distribute a computation (eg. data enrichment) in a scalable, performant and safe manner.

## Problems of existing solutions

`IExecutor`s are the recommended way to push a calculation to one or more members. It even has `executeOnKeyOwner` which is convenient.

Key problems with it are:
* It blocks the caller by default (submit is implicitly waiting on an internal `Future`!), which may result in a bottleneck (or even a deadlock) as the number of partition operation threads are limited.
* One can explicitly ignore the `Future`, making the call async, but then there's no back-pressure - causing an OOME just by filling the `BlockingQueue` of the 
executor if the calculation is slower than the ingestion rate.
* Related to the previous two, it's a _very bad idea_ to push tasks to an `IExecutor` from an `EntryListener`
* It's not possible to customize or override the `Executor` (eg. with a dedupe-capable, or bounded work queue)
* If the member dies, all the tasks on the `Executor`'s `BlockingQueue` are lost

But then how can we have a computation that is:
* Distributed (partitioned, ie. only one node executes a task, and tasks are evenly spread across the cluster)
* Reactive (ie. triggering on some event of the cluster)
* Durable(ish) (ie. tolerates member failures to at least some extent)
* Optionally supporting de-duplication

Unfortunately there's no facility for routing (Akka-style), or pulling tasks from a work queue. So what do we do?

## Using IMap to distribute tasks

The simplest scheme I could come up with so far is:
* Create a `workQueue` for the tasks (an `IMap` keyed by whatever partition key you like)
* Set up standard `Executor`s, one on each node
* Register an `EntryAddedListener` with `workQueue.addLocalEntryListener` on each member on startup
* When the listener is triggered, submit the task to the executor
* We remove the task from the `workQueue` when the executor picks it up (or when it completes, both options have benefits and drawbacks)

Note: an `ISet` would make more sense, but as of 5.1, it's _not partitioned_, which means one member would own the entire data set, and so do all the calcs itself.

A basic implementation can be found in [CalculatorService](CalculatorService.java), you can try it out with [CalculationTest](../../../../../test/java/org/ogreg/hazelcast/CalculationTest.java).

Benefits:
* You can put tasks into `workQueue` from anywhere, yet they'll end up being calculated on the node that owns their partition key _and nowhere else_
* `IMap` acts as a natural de-duplication layer (we're only listening to `add`s, not `update`s!), so by choosing good keys, you can put an upper bound on the work queue
* For the same reason, the `BlockingQueue` becomes effectively bounded as well
* Member failures are handled by Hazelcast (this sample code lacks this feature, but it can be done with a `MigrationListener`: one can clear and re-populate the `BlockingQueue` of the Executors on `migrationFinished`)
* You can easily monitor the calculation backlog by checking `workQueue.size()`

Drawbacks:
* The `BlockingQueue` and the `IMap` contain the same objects, we're wasting some memory and recovery is more complicated than it should be. It'd be a lot better if our `Executor` could work off of some distributed object directly. `IQueue` immediately comes to mind, but it is not partitioned :(

## Further work

It'd be nice to investigate whether / how we could _pull_ tasks from an `IMap`. The `iterator()` method looks promising, but in the end what we want to do is glue together a data structure that is inherently network operation-based, with a computational primitive (`Thread`) that loves to spin/poll... the two are not a good match. Hazelcast could solve this internally of course (eg. by allowing one to `wait()` until the `IMap` gets a new element), or we could yet again lean on `EntryListener`s and hack together either a custom `Executor` or a custom `BlockingQueue`... meh...
