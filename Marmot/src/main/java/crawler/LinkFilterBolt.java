package crawler;

import crawler.info.RobotsTxtInfo;
import crawler.info.URLInfo;
import model.CrawlerConfig;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichBolt;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import storage.StorageFactory;
import storage.StorageInterface;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.UUID;

public class LinkFilterBolt implements IRichBolt {

//	static Logger log = LogManager.getLogger(LinkFilterBolt.class);
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
		int mod;
		if (host != null) {
			 mod = host.hashCode() % CrawlerConfig.getTotalWorker();
		} else {
			mod = -1;
		}
		if (mod != -1 && CrawlerConfig.getTotalWorker() > 1 && mod != CrawlerConfig.getMyIndex()) {
			URL obj;
			try {
				CrawlerConfig.incAddedUrl();
				obj = new URL("http://" + CrawlerConfig.contactMap.get(mod) + "/add?url=" + nextUrl);
				HttpURLConnection con = (HttpURLConnection) obj.openConnection();
				con.setRequestMethod("GET");
				con.setConnectTimeout(500);
				int responseCode = con.getResponseCode();
				if (responseCode != HttpURLConnection.HTTP_OK) {
					//Do it your self
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
						QueueFactory.getQueueInstance().offer(task);
					}
				}
			} catch (IOException e) {
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
					QueueFactory.getQueueInstance().offer(task);
				}
			}
		} else if (host != null) {
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
				QueueFactory.getQueueInstance().offer(task);
				CrawlerConfig.incAddedUrl();
			}
		}
	}
//	}

	/**
	 * Called when this task is initialized
	 *
	 * @param conf
	 * @param context
	 * @param collector
	 */
	@Override
	public void prepare(Map conf, TopologyContext context, OutputCollector collector) {
		this.collector = collector;
		this.size = CrawlerConfig.getSize();
		this.db = StorageFactory.getDatabaseInstance(CrawlerConfig.getDatabaseDir());
	}


	/**
	 * Get the list of fields in the stream tuple
	 *
	 * @return
	 */
	public Fields getSchema() {
		return new Fields();
	}

	public String getExecutorId() {
		return this.executorId;
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields());
	}

	/**
	 * Declare configuration specific to this component. Only a subset of the "topology.*" configs can
	 * be overridden. The component configuration can be further overridden when constructing the
	 * topology using {@link}
	 */
	@Override
	public Map<String, Object> getComponentConfiguration() {
		return null;
	}

}
