package crawler;

import model.CrawlerConfig;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichBolt;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

public class LinkExtractorBolt implements IRichBolt {

//	static Logger log = LogManager.getLogger(LinkExtractorBolt.class);
	OutputCollector collector;
	String executorId = UUID.randomUUID().toString();
	Fields schema = new Fields("url");

	FileWriter fr;


	/**
	 * Called when a bolt is about to be shut down
	 */
	@Override
	public void cleanup() {
		try {
			fr.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Processes a tuple
	 *
	 * @param input
	 */
	@Override
	public void execute(Tuple input) {
//		log.debug("Start extracting links for " + input.getStringByField("url"));
		if (input.getStringByField("type").equals("html")) {
			Document doc = Jsoup.parse(input.getStringByField("content"));
			Elements links = doc.select("a[href]");
			addNextPage(input.getStringByField("url"), links);
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
		try {
			fr = new FileWriter(new File(CrawlerConfig.getLinksFileLocation()), true);
		} catch (IOException e) {
			e.printStackTrace();
		}
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
		declarer.declare(this.schema);
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

	private void addNextPage(String url, Elements links) {
		for (Element e : links) {
			CrawlerConfig.increamentBuf();
			String nextUrl = e.attr("abs:href");
			if (!FilterSharedFactory.outerLinkSet.contains(nextUrl)
							&& !nextUrl.contains("?")
							&& nextUrl.startsWith("https://")
							&& !nextUrl.contains("login")
							&& !nextUrl.contains("submit")
							&& !nextUrl.contains("redirect")
							&& !nextUrl.contains("account")
							&& nextUrl.length() < 80) {
				collector.emit(new Values(nextUrl));
				FilterSharedFactory.outerLinkSet.add(nextUrl);
				writeLocalLinksFile(url, nextUrl);
//				log.debug(getExecutorId() + " emitting " + nextUrl);
			}
		}
	}

	private void writeLocalLinksFile(String from, String to) {
		try {
			fr.write(from + "\t" + to + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
