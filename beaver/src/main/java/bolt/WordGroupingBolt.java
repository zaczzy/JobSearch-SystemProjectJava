package bolt;

import aws.rds.DBBulkManager;
import model.DocObj;
import model.Sentinel;
import model.Word;
import model.WordEntry;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichBolt;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;

import java.util.*;

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

    private Map<String, DocObj> documents = new HashMap<String, DocObj>();

    private final Boolean SHOULD_BULK_INSERT_FLAG  = true;

    public WordGroupingBolt() {

    }

    @Override
    public void prepare(Map conf, TopologyContext context, OutputCollector collector) {
        this.collector = collector;
    }

    @Override
    public void execute(Tuple input) {
        int weight = input.getIntegerByField("weight");
        String id = input.getStringByField("Id");
        int pagerank = input.getIntegerByField("pagerank");
        DocObj doc = documents.get(id);
        if(weight < 0) {
            //EOS received
            if(doc == null) {
                System.err.println("EOS for non-existent document");
            } else {
                Set<String> words = doc.getAllWords();
                if (SHOULD_BULK_INSERT_FLAG) {
                    List<WordEntry> entries = new ArrayList<>();
                    for (String word : words) {
                        // prepare data
                        int tf = doc.getFreq(word);
                        float norm = doc.L2Norm();
                        float wtf = tf / norm;
                        List<Integer> list = doc.getPositions(word);
                        // add entry
                        WordEntry entry = new WordEntry(word, id, list.toString(), tf, pagerank, wtf, norm);
                        entries.add(entry);
                    }
                    DBBulkManager.getInstance().bulkInsert(entries);
                } else {
                    for (String word : words) {
                        List<Integer> list = doc.getPositions(word);
                        int tf = doc.getFreq(word);
                        float norm = doc.L2Norm();
                        float wtf = tf / norm;
                        collector.emit(new Values(word, id, list, tf, pagerank, norm, wtf));
                    }
                }
                System.out.println("[ ✅ FINSIHED " + id + " ]: finished sending to sender bolt for " + id);
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
