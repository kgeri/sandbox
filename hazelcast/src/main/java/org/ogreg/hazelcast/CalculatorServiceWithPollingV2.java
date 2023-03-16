package org.ogreg.hazelcast;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.instance.impl.HazelcastInstanceProxy;
import com.hazelcast.internal.partition.InternalPartitionService;
import com.hazelcast.internal.serialization.Data;
import com.hazelcast.internal.serialization.SerializationService;
import com.hazelcast.map.IMap;
import com.hazelcast.map.impl.MapService;
import com.hazelcast.map.impl.MapServiceContext;
import com.hazelcast.map.impl.PartitionContainer;
import com.hazelcast.map.impl.record.Record;
import com.hazelcast.map.impl.recordstore.RecordStore;
import com.hazelcast.spi.impl.NodeEngineImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;

import static java.lang.String.format;

public class CalculatorServiceWithPollingV2<T extends Runnable> {
	private static final Logger log = LoggerFactory.getLogger(CalculatorServiceWithPollingV2.class);

	private final int workerCount;
	private final int workerBackoffMs;
	private final IMap<T, Long> map;
	private final SerializationService serializationService;
	private final InternalPartitionService partitionService;
	private final MapServiceContext mapServiceContext;

	public CalculatorServiceWithPollingV2(HazelcastInstance hz, String mapName, int workerCount, int workerBackoffMs) {
		this.map = hz.getMap(mapName);
		this.workerCount = workerCount;
		this.workerBackoffMs = workerBackoffMs;

		NodeEngineImpl nodeEngine = ((HazelcastInstanceProxy) hz).getOriginal().node.getNodeEngine();
		MapService mapService = nodeEngine.getService(MapService.SERVICE_NAME);
		this.serializationService = nodeEngine.getSerializationService();
		this.partitionService = nodeEngine.getPartitionService();
		this.mapServiceContext = mapService.getMapServiceContext();

		String memberUuid = hz.getCluster().getLocalMember().getUuid().toString();
		for (int i = 0; i < workerCount; i++) {
			CalculationWorker w = new CalculationWorker(i);
			w.setName(format("%s-%s-%d", memberUuid, mapName, i));
			w.setDaemon(true);
			w.start();
		}
	}

	private class CalculationWorker extends Thread {
		private final int workerId;

		public CalculationWorker(int workerId) {
			this.workerId = workerId;
		}

		@Override
		public void run() {
			while (!Thread.interrupted()) {
				try {
					calculateNext();
				} catch (InterruptedException e) {
					throw new RuntimeException("Worker interrupted", e);
				}
			}
		}

		@SuppressWarnings({"unchecked", "rawtypes"})
		private void calculateNext() throws InterruptedException {
			boolean foundTasks = false;

			PartitionContainer[] partitions = mapServiceContext.getPartitionContainers();
			for (PartitionContainer partition : partitions) {
				int partitionId = partition.getPartitionId();

				// We don't touch the partition if it's not our thread's
				if (partitionId % workerCount != workerId) {
					continue;
				}

				// Each Hazelcast Node contains all partitions for all maps. Backups are stored the same way as local entries, and as ownership might shift 
				// around because of migrations, we need to constantly check whether a particular partition is currently ours or not.
				if (!partitionService.isPartitionOwner(partitionId)) {
					continue;
				}

				// Digging into Hazelcast internals for an efficient way of iterating over the local keys
				RecordStore store = partition.getRecordStore(map.getName());
				if (store.isEmpty()) {
					continue;
				}

				Iterator<Map.Entry<Data, Record>> it = store.iterator();
				while (it.hasNext()) {
					Data keyData = it.next().getKey();
					T task = serializationService.toObject(keyData);
					foundTasks = true;
					calculate(task);
				}
			}

			if (!foundTasks) {
				// If there are no new tasks, we should not hammer the RecordStore
				Thread.sleep(workerBackoffMs);
			}
		}

		private void calculate(T task) throws InterruptedException {
			try {
				map.remove(task);
				task.run();
			} catch (Throwable e) {
				log.error("Task failed: " + task, e);
			}
		}
	}
}
