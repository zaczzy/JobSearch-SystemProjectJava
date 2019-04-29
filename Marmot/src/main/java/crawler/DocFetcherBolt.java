package crawler;

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
import stormlite.tuple.Values;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

public class DocFetcherBolt implements IRichBolt {
	static Logger log = LogManager.getLogger(DocFetcherBolt.class);
	String executorId = UUID.randomUUID().toString();
	Fields schema = new Fields("url", "content", "isHtml");
	StorageInterface db;

	OutputCollector collector;

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
		CrawlerTask task = (CrawlerTask) input.getObjectByField("task");
		String content = "";
		String url = task.getUrl();
		log.debug(getExecutorId() + " received " + url);
		try {
			Document document = Jsoup.connect(url).userAgent("cis455crawler").get();
			content = document.outerHtml();
			if (db.checkIfFull()) {
				CrawlerConfig.setWhetherEnd(true);
			}
			if (!db.ifMD5Exists(content)) {
				log.info(url + ": Downloading");
				CrawlerConfig.incPagesStored();
				db.addDocument(url, content, task.getIsHtml());
			}
			collector.emit(new Values<>(url, document, task.getIsHtml()));
			log.debug(getExecutorId() + " emitting " + url);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

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
		return this.schema;
	}

	@Override
	public String getExecutorId() {
		return this.executorId;
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(schema);
	}
}
