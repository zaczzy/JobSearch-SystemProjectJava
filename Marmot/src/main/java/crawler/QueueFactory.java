package crawler;

import java.util.concurrent.ConcurrentLinkedQueue;

public class QueueFactory {
	/*
		The factory for MasterServer Task Queue
	 */

	private static ConcurrentLinkedQueue queue;

	public static ConcurrentLinkedQueue getQueueInstance() {
		if (queue == null) {
			queue = new ConcurrentLinkedQueue<CrawlerTask>();
		}
		return queue;
	}

}
