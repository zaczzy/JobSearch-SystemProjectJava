package crawler;

import crawler.info.RobotsTxtInfo;
import crawler.info.URLInfo;
import model.CrawlerConfig;
import storage.StorageFactory;
import storage.StorageInterface;
import stormlite.OutputFieldsDeclarer;
import stormlite.TopologyContext;
import stormlite.bolt.IRichBolt;
import stormlite.bolt.OutputCollector;
import stormlite.routers.IStreamRouter;
import stormlite.tuple.Fields;
import stormlite.tuple.Tuple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.UUID;

public class LinkFilterBolt implements IRichBolt {

	static Logger log = LogManager.getLogger(LinkFilterBolt.class);
	OutputCollector collector;
	String executorId = UUID.randomUUID().toString();
	Fields schema = new Fields();
	int size = CrawlerConfig.getSize();
	StorageInterface db;




	/**
	 * Called when a bolt is about to be shut down
	 */
	@Override
	public void cleanup() {

	}

	/**
	 * Processes a tuple
	 *
	 * @param input
	 */
	@Override
	public void execute(Tuple input) {
		String nextUrl = input.getStringByField("url");
		URLInfo info = new URLInfo(nextUrl);
		String robotsLocation = info.isSecure() ? "https://" : "http://";
		info.setPortNo(info.isSecure() ? 443 : 80);
		robotsLocation += info.getHostName() + "/robots.txt";
		info.setFilePath(robotsLocation);
		String host = info.getHostName();
//		int mod = host.hashCode() % FilterSharedFactory.totalMachines;
//		System.out.println(mod);
//		if (mod != FilterSharedFactory.myNumber) {
//			int destPort = mod + 8000;
//			URL obj = null;
//			try {
//				obj = new URL("http://localhost:" + destPort + "/add?url=" + nextUrl);
//				HttpURLConnection con = (HttpURLConnection) obj.openConnection();
//				con.setRequestMethod("GET");
//				int responseCode = con.getResponseCode();
//				if (responseCode == HttpURLConnection.HTTP_OK) {
//					log.info(getExecutorId() + " sending " + nextUrl  +  "to " +destPort);
//				}
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		} else {
			RobotsTxtInfo robotsTxtInfo;
			if (FilterSharedFactory.robotsTxtInfoHashMap.containsKey(robotsLocation)) {
				robotsTxtInfo = FilterSharedFactory.robotsTxtInfoHashMap.get(robotsLocation);
			} else {
				robotsTxtInfo = RobotsHelper.parseRobotsTxt(robotsLocation);
				if (robotsTxtInfo != null) {
					FilterSharedFactory.robotsTxtInfoHashMap.put(robotsLocation, robotsTxtInfo);
				}
			}
			CrawlerTask task = new CrawlerTask(nextUrl, robotsTxtInfo);
			if (RobotsHelper.isOKtoCrawl(info, nextUrl, task) && RobotsHelper.isOKtoParse(info, robotsTxtInfo)) {
				QueueFactory.getQueueInstance().add(task);
				CrawlerConfig.incAddedUrl();
				log.info(getExecutorId() + " enqueueing " + task.getUrl());
				Thread.yield();
			}
			CrawlerConfig.decreamentBuf();
			if (CrawlerConfig.bufEmpty() && QueueFactory.getQueueInstance().isEmpty()) {
				CrawlerConfig.setWhetherEnd(true);
			}
		}
//	}

	/**
	 * Called when this task is initialized
	 *
	 * @param stormConf
	 * @param context
	 * @param collector
	 */
	@Override
	public void prepare(Map<String, String> stormConf, TopologyContext context, OutputCollector collector) {
		this.collector = collector;
		this.size = CrawlerConfig.getSize();
		this.db = StorageFactory.getDatabaseInstance(CrawlerConfig.getDatabaseDir());
	}

	/**
	 * Called during topology creation: sets the output router
	 *
	 * @param router
	 */
	@Override
	public void setRouter(IStreamRouter router) {
		this.collector.setRouter(router);
	}

	/**
	 * Get the list of fields in the stream tuple
	 *
	 * @return
	 */
	@Override
	public Fields getSchema() {
		return new Fields();
	}

	@Override
	public String getExecutorId() {
		return this.executorId;
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields());
	}

}
