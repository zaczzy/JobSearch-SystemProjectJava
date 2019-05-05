package crawler;

import crawler.info.RobotsTxtInfo;
import model.CrawlerConfig;
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichSpout;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

public class CrawlerQueueSpout implements IRichSpout {
//	static Logger log = LogManager.getLogger(CrawlerQueueSpout.class);
	SpoutOutputCollector collector;

	boolean flag = true;
	String executorId = UUID.randomUUID().toString();


	@Override
	public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
		this.collector = collector;
//		log.debug("Start Spout");
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
	 * Called when a spout has been activated out of a deactivated mode.
	 * nextTuple will be called on this spout soon. A spout can become activated
	 * after having been deactivated when the topology is manipulated using the
	 * `storm` client.
	 */
	@Override
	public void activate() {

	}

	/**
	 * Called when a spout has been deactivated. nextTuple will not be called while
	 * a spout is deactivated. The spout may or may not be reactivated in the future.
	 */
	@Override
	public void deactivate() {

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
		} else {
			RobotsTxtInfo robotsTxtInfo = nextTask.getRobotsTxtInfo();
			if (robotsTxtInfo != null && (robotsTxtInfo.getCrawlDelay("cis455crawler") != -1 || robotsTxtInfo.getCrawlDelay("*") != -1)) {
				//If have to wait
				double waitTime = robotsTxtInfo.getCrawlDelay("*");
				waitTime = robotsTxtInfo.getCrawlDelay("cis455crawler") == -1 ? waitTime : robotsTxtInfo.getCrawlDelay("cis455crawler");
				if (new Date().getTime() - (waitTime * 1000) < nextTask.getAddTime().getTime()) {
					QueueFactory.getQueueInstance().add(nextTask);
				} else {
					CrawlerConfig.increamentBuf();
					this.collector.emit(new Values(nextTask.getUrl()));
//					log.debug(getExecutorId() + " emitting " + nextTask.getUrl());
				}
			} else {
				CrawlerConfig.increamentBuf();
				this.collector.emit(new Values(nextTask.getUrl()));
//				log.debug(getExecutorId() + " emitting " + nextTask.getUrl());
			}
		}
	}

	/**
	 * Storm has determined that the tuple emitted by this spout with the msgId identifier
	 * has been fully processed. Typically, an implementation of this method will take that
	 * message off the queue and prevent it from being replayed.
	 *
	 * @param msgId
	 */
	@Override
	public void ack(Object msgId) {

	}

	/**
	 * The tuple emitted by this spout with the msgId identifier has failed to be
	 * fully processed. Typically, an implementation of this method will put that
	 * message back on the queue to be replayed at a later time.
	 *
	 * @param msgId
	 */
	@Override
	public void fail(Object msgId) {

	}


	public String getExecutorId() {
		return this.executorId;
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("url"));
	}

	@Override
	public Map<String, Object> getComponentConfiguration() {
		return null;
	}
}
