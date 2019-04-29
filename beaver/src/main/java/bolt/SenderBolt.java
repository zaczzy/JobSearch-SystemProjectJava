package bolt;

import aws.rds.DBManager;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichBolt;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Tuple;

import java.util.Map;

/**
 * Receive: ("word", "Id", "hits", "tf")
 * Output: Store in DB
 */
public class SenderBolt implements IRichBolt {

    private OutputCollector collector;

    @Override
    public void prepare(Map conf, TopologyContext context, OutputCollector collector) {
        this.collector = collector;
    }

    @Override
    public void execute(Tuple tuple) {
        /* Extract Field: "word", "Id", "hits", "tf" */
        String word = tuple.getStringByField("word");
        String docId = tuple.getValueByField("Id").toString();
        String list = tuple.getValueByField("hits").toString();
        Integer tf = (Integer) tuple.getValueByField("tf");
        System.out.println(word + ":" + docId + ":" + list + ":" + tf);
        /* Send to DB*/
        DBManager.getInstance().addRecord(word, docId, list, tf);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
    }

    @Override
    public void cleanup() {
    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        return null;
    }
}
