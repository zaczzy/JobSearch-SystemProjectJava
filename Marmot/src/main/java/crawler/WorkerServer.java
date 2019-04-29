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
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Timer;
import java.util.TimerTask;

import static model.CrawlerConfig.*;
import static spark.Spark.get;
import static spark.Spark.port;


public class WorkerServer {

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

  static String masterAddress = "localhost:8000";
  static String myPort = "";

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
    int count = args.length == 4 ? Integer.valueOf(args[3]) : 100;
    int selfIndex = Integer.valueOf(args[4]);

    CrawlerConfig.setMyIndex(8000+selfIndex);
    myPort = String.valueOf(8000 + selfIndex);
    port(8000 + selfIndex);


    get("/add", new AddURLHandler());

    get("/shutdown", (request, response) -> {
      CrawlerConfig.setWhetherEnd(true);
      return "shutdown worker";
    });

    TimerTask reportTask = new periodicallyReport();
    Timer timer = new Timer();
    timer.scheduleAtFixedRate(reportTask,500,10000);


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

  private static class periodicallyReport extends TimerTask {
    @Override
    public void run() {
      System.out.println("Report to master");
      String address = "http://" + masterAddress + "/workerstatus" +
              "?port=" + myPort +
              "&requests=" + CrawlerConfig.getRequestReceived() +
              "&pages=" + CrawlerConfig.getPagesStored() +
              "&added=" + CrawlerConfig.getUrlAdded2Queue();
      URL url = null;
      try {
        url = new URL(address);
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        System.out.println(url.toString());
        conn.setDoOutput(true);
        conn.setRequestMethod("GET");
        if (conn.getResponseCode() != 200) {
          System.out.println("Something wrong");
        }
      } catch (IOException e) {
        CrawlerConfig.setWhetherEnd(true);
      }
    }
  }
}
