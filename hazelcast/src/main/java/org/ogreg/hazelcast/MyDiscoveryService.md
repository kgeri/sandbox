# Custom member discovery

This one's almost trivial, just wanted to check what it takes to implement the [Discovery SPI](https://docs.hazelcast.com/hazelcast/5.1/extending-hazelcast/discovery-spi)... surprisingly little.

See [MyDiscoveryService](MyDiscoveryService.java) and [MyDiscoveryServiceTest](../../../../../test/java/org/ogreg/hazelcast/MyDiscoveryServiceTest.java).
All it does is it delegates to multicast discovery, but shows how to configure a custom discovery service.
