# Microstream sandbox

I found [Microstream](https://docs.microstream.one/manual/intro/welcome.html) to be particularly interesting because it promises to do away with the
hurdles of object-relational (and object-JSON, and object-`insert fancy NoSQL DB here`) mappings, and says: well, why not just map Java object graphs
as they are, to a file?

Scaling compute is almost trivial. Scaling storage (and making sure the compute plays along nicely!) is a _lot_ more difficult. Granted, there are
lots of solutions for scaling storage _separately_ (distributed filesystems/caches/DBs/logs/etc.), but they typically give you an API, and then you
have to maintain storage and compute separately. To be _truly_ P2P, compute nodes should _own their shard of the data_. This also allows one to move
the compute as close to the data as possible, which can have enormous performance benefits.

But there are some obvious questions:

* [What happens on schema changes](src/test/java/org/ogreg/microstream/BackwardsCompatibilityTest.java) (a field added then dropped again, DB file
  remaining the same)
