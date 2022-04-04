package org.ogreg.hazelcast;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.map.listener.EntryAddedListener;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public class CalculatorService<T extends Runnable> {
	private final IMap<T, Long> workQueue;
	private final Semaphore available = new Semaphore(1);
	private final ExecutorService executor;

	public CalculatorService(HazelcastInstance hz, int poolSize) {
		this.workQueue = hz.getMap("WorkQueue");
		this.executor = Executors.newFixedThreadPool(poolSize, runnable -> {
			Thread t = new Thread(runnable, hz.getCluster().getLocalMember().getUuid().toString());
			t.setDaemon(true);
			return t;
		});
		workQueue.addLocalEntryListener((EntryAddedListener<T, Long>) event -> {
			T task = event.getKey();
			executor.submit(() -> {
				workQueue.remove(task);
				task.run();
			});
		});
	}
}