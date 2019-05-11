package bolt;

import model.DocObj;
import model.Sentinel;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichBolt;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Level;
import org.apache.storm.tuple.Values;

/**
 * Bolt Two
 *
 * Receive: ("Id", "word", "position", "weight")
 * Output: ("word", "Id", "hits", "tf")
 */
public class WordGroupingBolt implements IRichBolt {
    private OutputCollector collector;
    private Sentinel sentinel;

    private Map<String, DocObj> documents = new HashMap<String, DocObj>();

    public WordGroupingBolt() {

    }

    @Override
    public void prepare(Map conf, TopologyContext context, OutputCollector collector) {
        this.collector = collector;
        this.sentinel = Sentinel.getInstance();
    }

    @Override
    public void execute(Tuple input) {
        //sentinel.setWorking(true);
        int weight = input.getIntegerByField("weight");
        String id = input.getStringByField("Id");
        int pagerank = input.getIntegerByField("pagerank");
        //sentinel.setBuffer(false);
        DocObj doc = documents.get(id);
        if(weight < 0) {
            //EOS received
            if(doc == null) {
                System.err.println("EOS for non-existent document");
            } else {
                Set<String> words = doc.getAllWords();
                for(String word : words) {
                    List<Integer> list = doc.getPositions(word);
                    int tf = doc.getFreq(word);
                    float norm = doc.L2Norm();
                    float wtf = tf / norm;
                    //sentinel.setBuffer(true);
                    collector.emit(new Values(word, id, list, tf, pagerank, norm, wtf));
                }
            }
        } else {
            if(doc == null) {
                doc = new DocObj(id);
            }
            String word = input.getStringByField("word");
            int pos = input.getIntegerByField("position");
            if(pos >= 0) {
                doc.addOccurrence(word, pos);
            }
            doc.addFreq(word, weight);
            documents.put(id, doc);
        }
        //sentinel.setWorking(false);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("word", "Id", "hits", "tf", "pagerank", "norm", "wtf"));
    }

    @Override
    public void cleanup() {

    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        return null;
    }
}
