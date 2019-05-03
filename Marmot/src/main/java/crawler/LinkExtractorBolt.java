package crawler;

import model.CrawlerConfig;
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
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Map;
import java.util.UUID;

public class LinkExtractorBolt implements IRichBolt {

	static Logger log = LogManager.getLogger(LinkExtractorBolt.class);
	OutputCollector collector;
	String executorId = UUID.randomUUID().toString();
	Fields schema = new Fields("url");


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
		if ((boolean) input.getObjectByField("isHtml")) {
			log.debug("Start extracting links for " + input.getStringByField("url"));
			Document doc = (Document) input.getObjectByField("content");
			Elements links = doc.select("a[href]");
			addNextPage(links);
		}
		CrawlerConfig.decreamentBuf();
		if (CrawlerConfig.bufEmpty() && QueueFactory.getQueueInstance().isEmpty()) {
			CrawlerConfig.setWhetherEnd(true);
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
		declarer.declare(this.schema);
	}

	private void addNextPage(Elements links) {
		for (Element e : links) {
			CrawlerConfig.increamentBuf();
			String nextUrl = e.attr("abs:href");
			if (!FilterSharedFactory.outerLinkSet.contains(nextUrl)) {
				collector.emit(new Values<>(nextUrl));
				FilterSharedFactory.outerLinkSet.add(nextUrl);
				log.debug(getExecutorId() + " emitting " + nextUrl);
			}
		}
	}
}
