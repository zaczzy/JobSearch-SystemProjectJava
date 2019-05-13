package crawler;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Stub class for implementing the queue of HttpTasks
 */
public class CrawlerTaskQueue extends ConcurrentLinkedQueue {
//	private List<CrawlerTask> list;
//	private int volume;

//	public CrawlerTaskQueue() {
//		this.list = new LinkedList<>();
//		this.volume = CrawlerConfig.getCount();
//	}
//
//	public CrawlerTaskQueue(int v) {
//		this.volume = v;
//		this.list = new LinkedList<>();
//	}
//
//	public synchronized void enqueue(CrawlerTask task) throws InterruptedException {
////		if (list.size() == 0) {
////			notifyAll();
////		}
//		if (list.size() == volume) {
////			wait();
//			return;
//		}
//		list.add(task);
//	}

//	public synchronized CrawlerTask dequeue() throws InterruptedException {
////		while (true) {
//		if (list.size() != 0) {
//			return list.remove(0);
//		} else {
//			return null;
//		}
////			else wait();
////		}
//	}
//
//	public synchronized boolean isEmpty() {
//		return list.size() == 0;
//	}
//
//	public synchronized int getSize() {
//		return list.size();
//	}
}
