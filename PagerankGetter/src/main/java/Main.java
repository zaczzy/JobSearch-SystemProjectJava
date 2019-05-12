import aws.rds.DBBulkManager;
import bolt.*;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.topology.TopologyBuilder;
import spout.RecordSpout;

public class Main {

    public static void main(String[] args) throws Exception {
        PageRankConsultant.getInstance();

        Configurator.setLevel("", Level.WARN);

        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout("spout", new RecordSpout(), 1);
        builder.setBolt("augmentor", new AugmentorBolt(), 64).shuffleGrouping("spout");
        builder.setBolt("committer", new CommitBolt(), 32).shuffleGrouping("augmentor");

        Config conf = new Config();
        /* Turn off logging to console */
        conf.setDebug(true);
        /* set up parallelism */
        conf.setNumWorkers(1);

        /* Start Topology */
        String topologyName = "Indexer";
        LocalCluster cluster = new LocalCluster();
        DBBulkManager.getInstance().start();
        cluster.submitTopology(topologyName, conf, builder.createTopology());
    }
}
