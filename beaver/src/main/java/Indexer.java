import aws.rds.DBManager;
import bolt.DocDownloaderBolt;
import bolt.DocParserBolt;
import bolt.SenderBolt;
import bolt.WordGroupingBolt;
import model.Sentinel;
import org.apache.logging.log4j.core.config.Configurator;
import spout.DocSpout;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Fields;
import org.apache.storm.utils.Utils;

import org.apache.logging.log4j.*;
import sun.rmi.runtime.Log;

/**
 * Main Indexer class
 */

public class Indexer {
    static Logger log = LogManager.getLogger(Indexer.class);

    public static void main(String args[]) {
        Configurator.setLevel("", Level.WARN);

        TopologyBuilder builder = new TopologyBuilder();

        builder.setSpout("spout", new DocSpout(), 1);
        builder.setBolt("downloader", new DocDownloaderBolt(), 4).shuffleGrouping("spout");
        builder.setBolt("parser", new DocParserBolt(), 4).shuffleGrouping("downloader");
        builder.setBolt("grouping", new WordGroupingBolt(), 4).fieldsGrouping("parser", new Fields("Id"));
        builder.setBolt("sender", new SenderBolt(), 10).shuffleGrouping("grouping");

        Config conf = new Config();
        /* Turn off logging to console */
        conf.setDebug(true);
        /* set up parallelism */
        conf.setNumWorkers(1);

        /* Start DB Connection Pool */
        DBManager.getInstance().start();

        /* Start Topology */
        String topologyName = "Indexer";
        LocalCluster cluster = new LocalCluster();
        cluster.submitTopology(topologyName, conf, builder.createTopology());

        Sentinel sentinel = Sentinel.getInstance();

        /* Shutdown*/
        Utils.sleep(600000);
        do {
            Utils.sleep(30000);
        } while(!sentinel.finished());

        System.out.println("Shutdown cluster");
        cluster.killTopology(topologyName);
        cluster.shutdown();

        /* Close DB Connection Pool */
        DBManager.getInstance().shutDown();
    }
}
