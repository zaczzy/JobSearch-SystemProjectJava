package crawler;

import model.CrawlerConfig;
import org.apache.storm.shade.org.apache.commons.codec.digest.DigestUtils;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichBolt;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import storage.StorageFactory;
import storage.StorageInterface;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

public class DocFetcherBolt implements IRichBolt {
//	static Logger log = LogManager.getLogger(DocFetcherBolt.class);
	String executorId = UUID.randomUUID().toString();
	Fields schema = new Fields("url", "content", "type", "id");
	StorageInterface db;
	int inc = 0;

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
		String content;
		String url = input.getStringByField("url");
		String type;
		//		log.debug(getExecutorId() + " received " + url);
		try {
			Document document = Jsoup.connect(url).userAgent("cis455crawler").get();
			content = document.outerHtml();
			String md5 = DigestUtils.md5Hex(content).toLowerCase();
			if (content.toLowerCase().startsWith("<!doctype html") || content.toLowerCase().startsWith("<html")) {
				type = "html";
				String lang = document.getElementsByTag("html").first().attr("lang");
				if (lang.toLowerCase().startsWith("en")) {
					String docID = UUID.randomUUID().toString();
					if (!db.ifMD5Exists(md5)) {
						collector.emit(new Values(url, content, type, docID));
					} else {
						System.out.println(url + " is up to date");
					}
					db.addDocument(url, md5, docID);
				}
			} else {
				type = "xml";
				String docID = UUID.randomUUID().toString();
				if (!db.ifMD5Exists(md5)) {
					collector.emit(new Values(url, content, type, docID));
				}
				db.addDocument(url, md5, docID);
			}
		} catch (IOException e) {
//			e.printStackTrace();
		}
	}

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
		this.db = StorageFactory.getDatabaseInstance(CrawlerConfig.getDatabaseDir());
	}


	/**
	 * Get the list of fields in the stream tuple
	 *
	 * @return
	 */
	public Fields getSchema() {
		return this.schema;
	}

	public String getExecutorId() {
		return this.executorId;
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(schema);
	}

	/**
	 * Declare configuration specific to this component. Only a subset of the "topology.*" configs can
	 * be overridden. The component configuration can be further overridden when constructing the
	 * topology using {@link }
	 */
	@Override
	public Map<String, Object> getComponentConfiguration() {
		return null;
	}
}
