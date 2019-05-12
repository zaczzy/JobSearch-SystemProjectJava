package bolt;

import Models.Keyword;
import Models.Word;
import aws.rds.DBBulkManager;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichBolt;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CommitBolt implements IRichBolt {
    private OutputCollector collector;
    private List<Keyword> buffer;

    @Override
    public void prepare(Map conf, TopologyContext context, OutputCollector collector) {
        this.collector = collector;
        this.buffer = new ArrayList<>();
    }

    @Override
    public void execute(Tuple tuple) {
        Keyword word = (Keyword) tuple.getValueByField("keyword");
        if (word != null) {
            buffer.add(word);
            if (buffer.size() >= 1000) {
                DBBulkManager.getInstance().bulkInsert(buffer);
                buffer = new ArrayList<>();
            }
        } else {
            System.err.println("[❓] No Keyword");
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("docName", "Id", "pagerank"));
    }

    @Override
    public void cleanup() {

    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        return null;
    }
}
