# Partition Groups

This is a [well-documented feature](https://docs.hazelcast.com/hazelcast/5.1/clusters/partition-group-configuration) that we'll need to be able to do disaster recovery.

The only small trick in this one is that rather than using `CUSTOM` or `SPI`, we piggy-back on `ZONE_AWARE`, by setting `PARTITION_GROUP_ZONE`:

```
Config c = new Config();
String zone = "myZone1";
...
MemberAttributeConfig mac = new MemberAttributeConfig();
mac.setAttribute(PARTITION_GROUP_ZONE, zone);
c.setMemberAttributeConfig(mac);
c.getPartitionGroupConfig().setEnabled(true);
c.getPartitionGroupConfig().setGroupType(ZONE_AWARE);
```

See [PartitionGroupTest](../../../../../test/java/org/ogreg/hazelcast/PartitionGroupTest.java).
(Try uncommenting the grouping config, and observe `aZoneCanBeLostBecauseOfZoneAwarePartitioning` failing!)
