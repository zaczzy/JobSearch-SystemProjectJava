/**
 * Main Indexer class
 */

import bolt.DocParserBolt;
import bolt.SenderBolt;
import bolt.WordGroupingBolt;
import spout.DocSpout;
import edu.stanford.nlp.coref.data.CorefChain;
//import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.ie.util.*;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.semgraph.*;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.simple.*;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Fields;
import org.apache.storm.utils.Utils;
import org.apache.storm.topology.IRichBolt;
import org.apache.storm.topology.IRichSpout;
import spout.DocSpout;

import java.util.*;

public class Indexer {
    public static String text = "<html><head><title>Jane</title></head><body>" +
            "After hearing about Joe's trip, Jane decided she might go to France one day.</body></html>";

    public static void main(String args[]) {
        TopologyBuilder builder = new TopologyBuilder();

        builder.setSpout("spout", new DocSpout(), 1);
        builder.setBolt("parser", new DocParserBolt(), 2).shuffleGrouping("spout");
        builder.setBolt("grouping", new WordGroupingBolt(), 2).fieldsGrouping("parser", new Fields("Id"));
        builder.setBolt("sender", new SenderBolt(), 1).shuffleGrouping("grouping");

        Config conf = new Config();
        // set up parallelism
        conf.setNumWorkers(2);
        //conf.setDebug(true);

        String topologyName = "Indexer";

        LocalCluster cluster = new LocalCluster();
        cluster.submitTopology(topologyName, conf, builder.createTopology());

        Utils.sleep(10000);
        cluster.killTopology(topologyName);
        cluster.shutdown();
    }
}
