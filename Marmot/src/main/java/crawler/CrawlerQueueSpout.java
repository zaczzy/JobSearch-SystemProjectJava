package crawler;

import crawler.info.RobotsTxtInfo;
import model.CrawlerConfig;
import stormlite.OutputFieldsDeclarer;
import stormlite.TopologyContext;
import stormlite.routers.IStreamRouter;
import stormlite.spout.IRichSpout;
import stormlite.spout.SpoutOutputCollector;
import stormlite.tuple.Fields;
import stormlite.tuple.Values;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

public class CrawlerQueueSpout implements IRichSpout {
	static Logger log = LogManager.getLogger(CrawlerQueueSpout.class);
	SpoutOutputCollector collector;

	boolean flag = true;
	String executorId = UUID.randomUUID().toString();

	/**
	 * Called when a task for this component is initialized within a
	 * worker on the cluster. It provides the spout with the environment
	 * in which the spout executes.
	 *
	 * @param config    The Storm configuration for this spout. This is
	 *                  the configuration provided to the topology merged in
	 *                  with cluster configuration on this machine.
	 * @param topo
	 * @param collector The collector is used to emit tuples from
	 *                  this spout. Tuples can be emitted at any time, including
	 *                  the open and close methods. The collector is thread-safe
	 *                  and should be saved as an instance variable of this spout
	 */
	@Override
	public void open(Map<String, String> config, TopologyContext topo, SpoutOutputCollector collector) {
		this.collector = collector;
		log.debug("Start Spout");
	}

	/**
	 * Called when an ISpout is going to be shutdown.
	 * There is no guarantee that close will be called, because the
	 * supervisor kill -9’s worker processes on the cluster.
	 */
	@Override
	public void close() {

	}

	/**
	 * When this method is called, Storm is requesting that the Spout emit
	 * tuples to the output collector. This method should be non-blocking,
	 * so if the Spout has no tuples to emit, this method should return.
	 */
	@Override
	public void nextTuple() {
		CrawlerTask nextTask = (CrawlerTask)QueueFactory.getQueueInstance().poll();
		if (nextTask == null) {
			if (CrawlerConfig.bufEmpty() && QueueFactory.getQueueInstance().isEmpty()) {
				CrawlerConfig.setWhetherEnd(true);
			}
//			Thread.yield();
		} else {
			RobotsTxtInfo robotsTxtInfo = nextTask.getRobotsTxtInfo();

			if (robotsTxtInfo != null && (robotsTxtInfo.getCrawlDelay("cis455crawler") != -1 || robotsTxtInfo.getCrawlDelay("*") != -1)) {
				int waitTime = robotsTxtInfo.getCrawlDelay("*");
				waitTime = robotsTxtInfo.getCrawlDelay("cis455crawler") == -1 ? waitTime : robotsTxtInfo.getCrawlDelay("cis455crawler");
				if (new Date().getTime() - (waitTime * 1000) < nextTask.getAddTime().getTime()) {
					QueueFactory.getQueueInstance().add(nextTask);
//					Thread.yield();
				} else {
					CrawlerConfig.increamentBuf();
					this.collector.emit(new Values<>(nextTask));
					log.debug(getExecutorId() + " emitting " + nextTask.getUrl());
//					Thread.yield();
				}
			} else {
				CrawlerConfig.increamentBuf();
				this.collector.emit(new Values<>(nextTask));
				log.debug(getExecutorId() + " emitting " + nextTask.getUrl());
//				Thread.yield();
			}
		}
	}

	@Override
	public void setRouter(IStreamRouter router) {
		this.collector.setRouter(router);
	}

	@Override
	public String getExecutorId() {
		return this.executorId;
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("task"));
	}
}
