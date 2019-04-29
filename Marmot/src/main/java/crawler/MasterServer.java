package crawler;

import com.fasterxml.jackson.databind.ObjectMapper;
import crawler.handlers.AddURLHandler;
import crawler.info.RobotsTxtInfo;
import crawler.info.URLInfo;
import model.CrawlerConfig;
import org.apache.logging.log4j.Level;
import stormlite.Config;
import stormlite.LocalCluster;
import stormlite.Topology;
import stormlite.TopologyBuilder;
import stormlite.tuple.Fields;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static model.CrawlerConfig.*;
import static spark.Spark.get;
import static spark.Spark.port;


public class MasterServer {

	private static final String CRAWLER_QUEUE_SPOUT = "CRAWLER_QUEUE_SPOUT";
	private static final String DOC_FETCHER_BOLT = "DOC_FETCHER_BOLT";
	private static final String LINK_EXTRACTOR_BOLT = "LINK_EXTRACTOR_BOLT";
	private static final String LINK_FILTER_BOLT = "LINK_FILTER_BOLT";

	public static class WorkerStatus {
		String ip;
		String port;
		String requestsReceived;
		String pagesStored;
		String urlAdded2Queue;

		public WorkerStatus(String ip, String port, String requestsReceived, String pagesStored, String urlAdded2Queue) {
			this.ip = ip;
			this.port = port;
			this.requestsReceived = requestsReceived;
			this.pagesStored = pagesStored;
			this.urlAdded2Queue = urlAdded2Queue;
		}
	}

	private static Map<String, WorkerStatus> workerMap = new HashMap<>();

	/**
	 * Main program:  init database, start crawler, wait
	 * for it to notify that it is done, then close.
	 */

	public static void main(String[] args) {
		org.apache.logging.log4j.core.config.Configurator.setLevel("crawler", Level.INFO);

		if (args.length < 3 || args.length > 6) {
			System.out.println("Usage: MasterServer {start URL} {database environment path} {max doc size in MB} {number of files to index}");
			System.exit(1);
		}

		String startUrl = args[0];
		String envPath = args[1];
		int size = Integer.valueOf(args[2]);
		int count = args.length >= 4 ? Integer.valueOf(args[3]) : 100;
		int selfIndex = 0;

		port(8000);

		registerWorkerStatusPage();
		registerStatusPage();
		get("/add", new AddURLHandler());

		get("/shutdown", (request, response) -> {
			CrawlerConfig.setWhetherEnd(true);
			return "shutdown worker";
		});

		Config config = new Config();
		setStartURL(startUrl);
		setDatabaseDir(envPath);
		setCount(count);
		setSize(size);

		if (!Files.exists(Paths.get(getDatabaseDir()))) {
			try {
				Files.createDirectory(Paths.get(getDatabaseDir()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		CrawlerQueueSpout spout = new CrawlerQueueSpout();
		DocFetcherBolt docFetcherBolt = new DocFetcherBolt();
		LinkExtractorBolt linkExtractorBolt = new LinkExtractorBolt();
		LinkFilterBolt linkFilterBolt = new LinkFilterBolt();

		TopologyBuilder builder = new TopologyBuilder();

		builder.setSpout(CRAWLER_QUEUE_SPOUT, spout, 20);

		builder.setBolt(DOC_FETCHER_BOLT, docFetcherBolt, 20).shuffleGrouping(CRAWLER_QUEUE_SPOUT);

		builder.setBolt(LINK_EXTRACTOR_BOLT, linkExtractorBolt, 20).fieldsGrouping(DOC_FETCHER_BOLT, new Fields("url"));

		builder.setBolt(LINK_FILTER_BOLT, linkFilterBolt, 20).shuffleGrouping(LINK_EXTRACTOR_BOLT);

		LocalCluster cluster = new LocalCluster();
		Topology topo = builder.createTopology();
		ObjectMapper mapper = new ObjectMapper();

		URLInfo info = new URLInfo(getStartURL());
		String robotsLocation = info.isSecure() ? "https://" : "http://";
		robotsLocation += info.getHostName() + "/robots.txt";
		RobotsTxtInfo robotsTxtInfo = RobotsHelper.parseRobotsTxt(robotsLocation);
		CrawlerTask task = new CrawlerTask(getStartURL(), null);

		if (RobotsHelper.isOKtoCrawl(info, getStartURL(), task) && RobotsHelper.isOKtoParse(info, robotsTxtInfo)) {
				QueueFactory.getQueueInstance().add(task);
		}

		cluster.submitTopology("test", config,
						builder.createTopology());
		setWhetherEnd(false);
		while (!isWhetherEnd()) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		cluster.killTopology("test");
		cluster.shutdown();

		System.exit(0);
	}


	private static void registerWorkerStatusPage() {
		get("/workerstatus", (request, response) -> {
			String ip = request.ip();
			String port = request.queryParams("port");
			String getRequests = request.queryParams("requests");
			String webpageSaved = request.queryParams("pages");
			String urlAdd2Queue = request.queryParams("added");
			WorkerStatus status = new WorkerStatus(ip, port, getRequests, webpageSaved, urlAdd2Queue);
			workerMap.put(ip+ ":" + port, status);
			System.out.println("here");
			return "received from " + ip + ":" + port;
		});
	}


	public static void registerStatusPage() {
		get("/status", (request, response) -> {
			response.type("text/html");
			StringBuilder body = new StringBuilder();
			body.append("<html><head><title>Master</title></head>\n" +
							"<body><br>");

			WorkerStatus myStatus = new WorkerStatus("localhost", "8000", String.valueOf(CrawlerConfig.getRequestReceived()),
							String.valueOf(CrawlerConfig.getPagesStored()), String.valueOf(CrawlerConfig.getUrlAdded2Queue()));
			workerMap.put("localhost:8000", myStatus);

			for (Map.Entry<String, WorkerStatus> entry : workerMap.entrySet()) {
				body.append("<p>").append("IP:port: ").append(entry.getKey())
								.append(" received requests ").append(entry.getValue().requestsReceived)
								.append(" stored  ").append(entry.getValue().pagesStored).append(" pages")
								.append(" added ").append(entry.getValue().urlAdded2Queue).append(" links")
								.append("</p><br>");
			}
			body.append("</body></html>");
			return body.toString();
		});
	}
}
