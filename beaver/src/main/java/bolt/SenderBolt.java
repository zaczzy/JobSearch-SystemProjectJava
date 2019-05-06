package bolt;

import aws.rds.DBManager;
import model.Sentinel;
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
    private Sentinel sentinel;

    @Override
    public void prepare(Map conf, TopologyContext context, OutputCollector collector) {
        this.collector = collector;
        this.sentinel = Sentinel.getInstance();
    }

    @Override
    public void execute(Tuple tuple) {
        sentinel.setWorking(true);
        /* Extract Field: "word", "Id", "hits", "tf" */
        String word = tuple.getStringByField("word");
        String docId = tuple.getStringByField("Id");
        String list = tuple.getValueByField("hits").toString();
        Integer tf = (Integer) tuple.getValueByField("tf");
        sentinel.setBuffer(false);
        //System.out.println(word + ":" + docId + ":" + list + ":" + tf);
        /* Send to DB*/
        DBManager.getInstance().addRecord(word, docId, list, tf);
        sentinel.setWorking(false);
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
