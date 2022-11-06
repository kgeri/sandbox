package org.ogreg.hazelcast;

import com.hazelcast.core.EntryView;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.instance.impl.HazelcastInstanceProxy;
import com.hazelcast.map.IMap;
import com.hazelcast.map.impl.MapService;
import com.hazelcast.map.impl.MapServiceContext;
import com.hazelcast.map.impl.PartitionContainer;
import com.hazelcast.map.impl.recordstore.RecordStore;
import com.hazelcast.spi.impl.NodeEngineImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static java.lang.String.format;

public class CalculatorServiceWithPolling<T extends Runnable> {
	private static final Logger log = LoggerFactory.getLogger(CalculatorServiceWithPolling.class);

	private final int dispatchSleepMs;
	private final IMap<T, Long> map;
	private final MapServiceContext mapServiceContext;
	private final RecordStore<?>[] recordStores;
	private final BlockingQueue<T> tasks;

	public CalculatorServiceWithPolling(HazelcastInstance hz, String mapName, int poolSize, int dispatchSleepMs) {
		this.dispatchSleepMs = dispatchSleepMs;
		this.map = hz.getMap(mapName);

		NodeEngineImpl nodeEngine = ((HazelcastInstanceProxy) hz).getOriginal().node.getNodeEngine();
		MapService mapService = nodeEngine.getService(MapService.SERVICE_NAME);
		this.mapServiceContext = mapService.getMapServiceContext();
		this.recordStores = getLocalRecordStores(mapServiceContext, mapName);

		this.tasks = new ArrayBlockingQueue<>(poolSize);
		String memberUuid = hz.getCluster().getLocalMember().getUuid().toString();
		startWorker(format("%s-%s-dispatcher", memberUuid, mapName), this::dispatch);
		for (int i = 0; i < poolSize; i++) {
			startWorker(format("%s-%s-%d", memberUuid, mapName, i), this::calculate);
		}
	}

	private RecordStore<?>[] getLocalRecordStores(MapServiceContext mapServiceContext, String mapName) {
		PartitionContainer[] partitionContainers = mapServiceContext.getPartitionContainers();
		RecordStore<?>[] recordStores = new RecordStore[partitionContainers.length];
		for (int i = 0; i < partitionContainers.length; i++) {
			recordStores[i] = partitionContainers[i].getRecordStore(mapName);
		}
		return recordStores;
	}

	private void startWorker(String threadName, InterruptableRunnable block) {
		Thread t = new Thread(() -> {
			while (!Thread.interrupted()) {
				try {
					block.run();
				} catch (InterruptedException e) {
					throw new RuntimeException("Worker interrupted", e);
				}
			}
		});
		t.setName(threadName);
		t.setDaemon(true);
		t.start();
	}

	private void dispatch() throws InterruptedException {
		boolean foundTasks = false;
		for (int i = 0; i < recordStores.length; i++) {

			// Each Hazelcast Node contains all partitions for all maps. Backups are stored the same way as local entries, and we as partition
			// ownership might shift around because of migrations, we need to constantly check whether a particular partition is ours or not.
			if (!mapServiceContext.getOrInitCachedMemberPartitions().contains(i)) {
				// This partition is currently not ours, skipping
				continue;
			}

			@SuppressWarnings("unchecked")
			Iterable<EntryView<?, ?>> it = recordStores[i].getStorage().getRandomSamples(tasks.remainingCapacity());
			for (EntryView<?, ?> sample : it) {
				@SuppressWarnings("unchecked")
				T task = (T) sample.getKey();
				foundTasks = true;
				tasks.put(task);
			}
		}

		if (!foundTasks) {
			// If there are no new tasks, we should not hammer the RecordStore
			Thread.sleep(dispatchSleepMs);
		}
	}

	private void calculate() throws InterruptedException {
		T task = tasks.take();
		try {
			map.remove(task);
			task.run();
		} catch (Throwable e) {
			log.error("Task failed: " + task, e);
		}
	}

	@FunctionalInterface
	private interface InterruptableRunnable {
		void run() throws InterruptedException;
	}
}
