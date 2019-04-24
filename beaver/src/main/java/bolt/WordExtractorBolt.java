package bolt;

import model.DocObj;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;

import java.util.Map;

/**
 * Bolt Two
 *
 * Receive: ("Id", "docObj")
 * Output: ("Id", "word", "position", "weight")
 *
 * Note: emit EOS eg. (id, xxx, -1, xxx) upon finishing extracting from a document
 */
public class WordExtractorBolt extends BaseRichBolt {
    private OutputCollector collector;

    public WordExtractorBolt() {

    }

    @Override
    public void prepare(Map conf, TopologyContext context, OutputCollector collector) {
        this.collector = collector;
    }

    @Override
    public void execute(Tuple input) {
        DocObj doc = (DocObj) input.getValueByField("docObj");

    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("Id", "word", "pos", "weight"));
    }

}
