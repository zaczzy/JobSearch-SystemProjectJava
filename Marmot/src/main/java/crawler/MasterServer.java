package crawler;

import crawler.handlers.AddURLHandler;
import crawler.info.RobotsTxtInfo;
import crawler.info.URLInfo;
import model.CrawlerConfig;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.topology.TopologyBuilder;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static model.CrawlerConfig.*;
import static spark.Spark.get;
import static spark.Spark.port;

public class MasterServer {


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

	private static Map<String, WorkerStatus> workerMap = new TreeMap<>();

	/**
	 * Main program:  init database, start crawler, wait
	 * for it to notify that it is done, then close.
	 */

	public static void main(String[] args) {
//		org.apache.logging.log4j.core.config.Configurator.setLevel("", Level.INFO);


		if (args.length < 3 || args.length > 6) {
			System.out.println("Usage: MasterServer {start URL} {database environment path} {max doc size in MB} {number of files to index}");
			System.exit(1);
		}

		String startUrl = args[0];
		String envPath = args[1];
		int size = Integer.valueOf(args[2]);
		int count = args.length >= 4 ? Integer.valueOf(args[3]) : 100;
		int selfIndex = Integer.valueOf(args[4]);
		CrawlerConfig.setMyIndex(selfIndex);

		port(8000 + selfIndex);
		WorkerStatus myStatus = new WorkerStatus("127.0.0.1", String.valueOf(8000+selfIndex), String.valueOf(CrawlerConfig.getRequestReceived()),
						String.valueOf(CrawlerConfig.getPagesStored()), String.valueOf(CrawlerConfig.getUrlAdded2Queue()));
		workerMap.put("127.0.0.1:" + (8000 + selfIndex), myStatus);

		registerWorkerStatusPage();
		registerStatusPage();
		get("/add", new AddURLHandler());

		get("/shutdown", (request, response) -> {
			CrawlerConfig.setWhetherEnd(true);
			System.exit(0);
			return "shutdown worker";
		});

		Config config = new Config();
		setStartURL(startUrl);
		setDatabaseDir(envPath);
		setCount(count);
		setSize(size);

		TimerTask reportTask = new periodicallyNotice();
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(reportTask,2500,10000);

		if (!Files.exists(Paths.get(getDatabaseDir()))) {
			try {
				Files.createDirectory(Paths.get(getDatabaseDir()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		TopologyBuilder builder = new TopologyBuilder();

		builder.setSpout("CRAWLER_QUEUE_SPOUT", new CrawlerQueueSpout(), 10);

//		builder.setBolt("DOC_FETCHER_BOLT", new DocFetcherBolt(), 10).shuffleGrouping("CRAWLER_QUEUE_SPOUT");

//		builder.setBolt("DOC_UPLOAD_BOLT",  new DocUploadBolt(), 20).shuffleGrouping("DOC_FETCHER_BOLT");

//		builder.setBolt("LINK_EXTRACTOR_BOLT", new LinkExtractorBolt(), 20).shuffleGrouping("DOC_FETCHER_BOLT");

//		builder.setBolt("LINK_FILTER_BOLT", new LinkFilterBolt(), 20).shuffleGrouping("LINK_EXTRACTOR_BOLT");

		LocalCluster cluster = new LocalCluster();

		URLInfo info = new URLInfo(getStartURL());
		String robotsLocation = info.isSecure() ? "https://" : "http://";
		robotsLocation += info.getHostName() + "/robots.txt";
		RobotsTxtInfo robotsTxtInfo = RobotsHelper.parseRobotsTxt(robotsLocation);
		CrawlerTask task = new CrawlerTask(getStartURL(), robotsTxtInfo);

		if (RobotsHelper.isOKtoCrawl(info, getStartURL(), task) && RobotsHelper.isOKtoParse(info, robotsTxtInfo)) {
				QueueFactory.getQueueInstance().add(task);
		}

		info = new URLInfo("http://www.reddit.com");
		robotsLocation = info.isSecure() ? "https://" : "http://";
		robotsLocation += info.getHostName() + "/robots.txt";
		robotsTxtInfo = RobotsHelper.parseRobotsTxt(robotsLocation);
		task = new CrawlerTask(getStartURL(), robotsTxtInfo);

		if (RobotsHelper.isOKtoCrawl(info, getStartURL(), task) && RobotsHelper.isOKtoParse(info, robotsTxtInfo)) {
			QueueFactory.getQueueInstance().add(task);
		}

		info = new URLInfo("https://news.google.com/");
		robotsLocation = info.isSecure() ? "https://" : "http://";
		robotsLocation += info.getHostName() + "/robots.txt";
		robotsTxtInfo = RobotsHelper.parseRobotsTxt(robotsLocation);
		task = new CrawlerTask(getStartURL(), robotsTxtInfo);

		if (RobotsHelper.isOKtoCrawl(info, getStartURL(), task) && RobotsHelper.isOKtoParse(info, robotsTxtInfo)) {
			QueueFactory.getQueueInstance().add(task);
		}

		registerRcvNotice();


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
		while (true);
//    cluster.killTopology("test");
//    cluster.shutdown();

//    System.exit(0);
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

	public static void registerRcvNotice() {
		get("/notice", (request, response) -> {
			CrawlerConfig.setTotalWorker(Integer.valueOf(request.queryParams("total")));
			CrawlerConfig.setMyIndex(Integer.valueOf(request.queryParams("you")));
			for (int i = 0; i < CrawlerConfig.getTotalWorker(); i++) {
				contactMap.put(i, request.queryParams("worker" + i));
			}
			return "receive notice";
		});
	}

	private static class periodicallyNotice extends TimerTask {
		@Override
		public void run() {
			int index = 0;
			Set<String> toRemove = new HashSet<>();
			for (String addr : workerMap.keySet()) {
				StringBuilder address = new StringBuilder("http://");
				address.append(addr);
				address.append("/notice");
				address.append("?total=").append(workerMap.size());
				int cnt = 0;
				for (String innerAddr : workerMap.keySet()) {
					address.append("&worker").append(cnt++).append("=").append(innerAddr);
				}
				address.append("&you=").append(index++);
				URL url;
				try {
					url = new URL(address.toString());
					HttpURLConnection conn = (HttpURLConnection)url.openConnection();
					System.out.println(url.toString());
					conn.setDoOutput(true);
					conn.setRequestMethod("GET");
					conn.setConnectTimeout(2000);
					if (conn.getResponseCode() != 200) {
						System.out.println("Something wrong");
					}
				} catch (IOException e) {
					toRemove.add(addr);
				}
			}
			for (String remove : toRemove) {
				workerMap.remove(remove);
			}
		}
	}
}
