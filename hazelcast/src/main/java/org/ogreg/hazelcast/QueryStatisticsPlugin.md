# Query Statistics

Based on the age and comments on [this ticket](https://github.com/hazelcast/hazelcast/issues/11234), I suspect Hazelcast folks are not inclined to
add proper query statistics (query execution times, slow query list, etc.) anytime soon.

So I started digging around the code, to see if it's possible to hook into it somehow.

Unfortunately, almost nothing is interfaced properly in the impl packages of Hazelcast, and all the components are instantiated with `new`from one
another :(

So ByteBuddy it is, then :P

Still, where to hook it? First I thought `QueryEngineImpl` is the one executing queries, however only from `MapProxyImpl`. From the client side
(`ClientMapProxy`), it's a `QueryOperation` that gets created, serialized and sent to the cluster nodes, where they're executed on an operation thread.

Fortunately, the API of `QueryOperation` is quite simple: it's basically a `call` method, and it has the `Query` (which in turn has the `Predicate`)
inside... wonderful!

So check out [QueryStatisticsPlugin](QueryStatisticsPlugin.java) and [QueryStatisticsPluginTest](../../../../../test/java/org/ogreg/hazelcast/QueryStatisticsPluginTest.java).
Once installed, this plugin will intercept method calls on `call`, and have access to the `Predicate` as well as being able to measure execution time.

**CAVEATS**:
* Obviously, this **messes with bytecode**, so your warranty is now void.
* Installing the ByteBuddy agent may interfere with other bytecode instrumentation you happen to do.
* Classes cannot be redefined once they are loaded (on almost all JVMs). This means `QueryStatisticsPlugin.install` must be done **before** you touch Hazelcast classes.
