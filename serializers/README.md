# Java Serializers

This is a quick-and-dirty, absolutely opinionated and very likely unfair comparison of serialization libraries in Java.

## Goals

* Be able to serialize **arbitrary** objects
* In an efficient, **binary** format
* **Without having to implement `Serializable`**
* The library should have a small footprint
* Performance is not critical

Because of the above, a few well-known frameworks are instantly disqualified:

* Avro, FlatBuffers, Protobuf, Thrift: they require upfront schema design
* CBOR, Ion: looks too low-level, arbitrary object graphs are hard
* GSON, Jackson XML/JSON, Jettison, etc.: they emit text

## Sample data

See [Samples](src/main/java/org/ogreg/serializers/Samples.java) - the library should be able to deserialize an arbitrarily complex, but reasonably
sized object graph.

## Evaluation

| Library                                                                                 | Dependencies | Maintained? (2023.03) | Conclusion                                                                                         |
|-----------------------------------------------------------------------------------------|--------------|-----------------------|----------------------------------------------------------------------------------------------------|
| [fst](https://github.com/RuedigerMoeller/fast-serialization)                            | 178 (WTF??)  | yes (2022.10.31)      | 💀 178 deps?? Didn't even bother                                                                   |
| [kryo](https://github.com/EsotericSoftware/kryo)                                        | 3            | yes (2022.12.30)      | ✅ [KryoTest](src/test/java/org/ogreg/serializers/KryoTest.java) (and its performance is awesome!)  |
| [msgpack-jackson](https://github.com/msgpack/msgpack-java/tree/develop/msgpack-jackson) | 13           | yes (2022.06.28)      | ❌ [MsgPackTest](src/test/java/org/ogreg/serializers/MsgPackTest.java) (fails on polymorphic types) |
