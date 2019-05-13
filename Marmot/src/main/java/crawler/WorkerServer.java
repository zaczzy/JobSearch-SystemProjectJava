package crawler;

import crawler.handlers.AddURLHandler;
import crawler.info.RobotsTxtInfo;
import crawler.info.URLInfo;
import model.CrawlerConfig;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.topology.TopologyBuilder;

import java.io.BufferedReader;
import java.io.FileReader;
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

  static String masterAddress = "127.0.0.1:8000";
  static String myPort = "";

  /**
   * Main program:  init database, start crawler, wait
   * for it to notify that it is done, then close.
   */

  public static void main(String[] args) {
//    org.apache.logging.log4j.core.config.Configurator.setLevel("crawler", Level.INFO);

    if (args.length < 3 || args.length > 6) {
      System.out.println("Usage: MasterServer {start URL} {database environment path} {max doc size in MB} {number of files to index}");
      System.exit(1);
    }

    String inputFile = args[0];
    String envPath = args[1];
    int size = Integer.valueOf(args[2]);
    int count = args.length == 4 ? Integer.valueOf(args[3]) : 100;
    int selfIndex = Integer.valueOf(args[4]);
    setLinksFileLocation("./links/out_links_" + selfIndex);


    CrawlerConfig.setMyIndex(selfIndex);
    myPort = String.valueOf(8000 + selfIndex);
    port(8000 + selfIndex);


    get("/add", new AddURLHandler());

    get("/shutdown", (request, response) -> {
      CrawlerConfig.setWhetherEnd(true);
      System.exit(0);
      return "shutdown worker";
    });

    TimerTask reportTask = new periodicallyReport();
    Timer timer = new Timer();
    timer.scheduleAtFixedRate(reportTask,500,30000);
    registerRcvNotice();

    Config config = new Config();
    config.setNumWorkers(2);
    config.setMaxSpoutPending(3000);
    setInputFile("input/" + inputFile);
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

    TopologyBuilder builder = new TopologyBuilder();

    builder.setSpout("CRAWLER_QUEUE_SPOUT", new CrawlerQueueSpout(), 4);

    builder.setBolt("DOC_FETCHER_BOLT", new DocFetcherBolt(), 8).shuffleGrouping("CRAWLER_QUEUE_SPOUT");

		builder.setBolt("DOC_UPLOAD_BOLT",  new DocUploadBolt(), 16).shuffleGrouping("DOC_FETCHER_BOLT");

    builder.setBolt("LINK_EXTRACTOR_BOLT", new LinkExtractorBolt(), 8).shuffleGrouping("DOC_FETCHER_BOLT");

    builder.setBolt("LINK_FILTER_BOLT", new LinkFilterBolt(), 16).shuffleGrouping("LINK_EXTRACTOR_BOLT");

    BufferedReader reader;
    try {
      reader = new BufferedReader(new FileReader(
              getInputFile()));
      String nextURL = reader.readLine();
      while (nextURL != null) {
        URLInfo info = new URLInfo(nextURL);
        String robotsLocation = info.isSecure() ? "https://" : "http://";
        robotsLocation += info.getHostName() + "/robots.txt";
        RobotsTxtInfo robotsTxtInfo = RobotsHelper.parseRobotsTxt(robotsLocation);
        CrawlerTask task = new CrawlerTask(nextURL, robotsTxtInfo);
        if (RobotsHelper.isOKtoCrawl(info, nextURL, task) && RobotsHelper.isOKtoParse(info, robotsTxtInfo)) {
          QueueFactory.getQueueInstance().add(task);
        }
        nextURL = reader.readLine();
      }
      reader.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

    LocalCluster cluster = new LocalCluster();
    cluster.submitTopology("test" + selfIndex, config,
            builder.createTopology());
    setWhetherEnd(false);
    while (true) {
      try {
        Thread.sleep(10);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
//    cluster.killTopology("test" + selfIndex);
//    cluster.shutdown();
//
//    System.exit(0);
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

  private static class periodicallyReport extends TimerTask {
    @Override
    public void run() {
      System.out.println("Report to master");
      String address = "http://" + masterAddress + "/workerstatus" +
              "?port=" + myPort +
              "&requests=" + CrawlerConfig.getRequestReceived() +
              "&pages=" + CrawlerConfig.getPagesStored() +
              "&added=" + CrawlerConfig.getUrlAdded2Queue();
      URL url;
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
      }
    }
  }
}
