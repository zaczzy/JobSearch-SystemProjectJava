package bolt;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;

import java.util.Map;

/**
 * Bolt Three
 *
 * Receive: ("Id", "word", "position", "weight")
 * Output: ("word", "Id", "hits", "tf-idf")
 */
public class WordGroupingBolt extends BaseRichBolt{
    private OutputCollector collector;

    public WordGroupingBolt() {

    }

    @Override
    public void prepare(Map conf, TopologyContext context, OutputCollector collector) {
        this.collector = collector;
    }

    @Override
    public void execute(Tuple input) {
        int tfidf = input.getIntegerByField("tf-idf");
        if(tfidf < 0) {

        } else {

        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("word", "Id", "hits", "tf-idf"));
    }
}
