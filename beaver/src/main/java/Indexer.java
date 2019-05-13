import aws.rds.DBBulkManager;
import aws.rds.DBManager;
import aws.s3.S3Service;
import bolt.*;
import model.Sentinel;
import org.apache.logging.log4j.core.config.Configurator;
import spout.DocSpout;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Fields;
import org.apache.storm.utils.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.logging.log4j.*;

/**
 * Main Indexer class
 */

public class Indexer {
    static Logger log = LogManager.getLogger(Indexer.class);

    public static void main(String args[]) {
        Configurator.setLevel("", Level.WARN);

        if(args.length != 1) {
            System.err.println("Incorrect number of arguments to Indexer.");
            System.exit(1);
        }

        String folder = args[0];

        TopologyBuilder builder = new TopologyBuilder();

        builder.setSpout("spout", new DocSpout(), 1);
        builder.setBolt("adder", new DynamoDbBolt(), 5).shuffleGrouping("spout");
        builder.setBolt("downloader", new DocDownloaderBolt(), 8).shuffleGrouping("adder");
        builder.setBolt("parser", new DocParserBolt(), 4).shuffleGrouping("downloader");
        builder.setBolt("grouping", new WordGroupingBolt(), 4).fieldsGrouping("parser", new Fields("Id"));
        builder.setBolt("sender", new SenderBolt(), 10).shuffleGrouping("grouping");

        Config conf = new Config();
        /* Turn off logging to console */
        conf.setDebug(true);
        /* set up parallelism */
        conf.setNumWorkers(1);
        conf.put("folder", folder);

        /* Start DB Connection Pool */
        DBBulkManager.getInstance().start();

        /* Start Topology */
        String topologyName = "Indexer";
        LocalCluster cluster = new LocalCluster();
        cluster.submitTopology(topologyName, conf, builder.createTopology());

        /* Shutdown*/
        Utils.sleep(5000);
        System.out.println("Press [Enter] to shut down this node...");
        try {
            (new BufferedReader(new InputStreamReader(System.in))).readLine();
        } catch(IOException e) {

        }

        System.out.println("Shutdown cluster");

        cluster.killTopology(topologyName);
        cluster.shutdown();

        /* Close DB Connection Pool */
        DBBulkManager.getInstance().shutDown();
    }
}
