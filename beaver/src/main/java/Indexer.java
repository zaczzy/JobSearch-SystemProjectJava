import aws.rds.DBManager;
import bolt.DocDownloaderBolt;
import bolt.DocParserBolt;
import bolt.SenderBolt;
import bolt.WordGroupingBolt;
import model.Sentinel;
import spout.DocSpout;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Fields;
import org.apache.storm.utils.Utils;

/**
 * Main Indexer class
 */

public class Indexer {
    public static void main(String args[]) {
        TopologyBuilder builder = new TopologyBuilder();

        builder.setSpout("spout", new DocSpout(), 1);
        builder.setBolt("downloader", new DocDownloaderBolt(), 3).shuffleGrouping("spout");
        builder.setBolt("parser", new DocParserBolt(), 4).shuffleGrouping("downloader");
        builder.setBolt("grouping", new WordGroupingBolt(), 2).fieldsGrouping("parser", new Fields("Id"));
        builder.setBolt("sender", new SenderBolt(), 10).shuffleGrouping("grouping");

        Config conf = new Config();
        /* Turn off logging to console */
        conf.setDebug(false);
        /* set up parallelism */
        conf.setNumWorkers(2);

        /* Start DB Connection Pool */
        DBManager.getInstance().start();

        /* Start Topology */
        String topologyName = "Indexer";
        LocalCluster cluster = new LocalCluster();
        cluster.submitTopology(topologyName, conf, builder.createTopology());

        Sentinel sentinel = Sentinel.getInstance();

        /* Shutdown*/
        try {
            do {
                Thread.sleep(5000);
            } while(sentinel.getCount() != 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        cluster.killTopology(topologyName);
        cluster.shutdown();

        /* Close DB Connection Pool */
        DBManager.getInstance().shutDown();
    }
}
