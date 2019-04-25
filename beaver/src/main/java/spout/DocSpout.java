package spout;

import java.util.Map;
import java.util.Random;
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;

public class DocSpout extends BaseRichSpout {

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("Id", "doc"));
    }

    @Override
    public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {

    }

    @Override
    public void nextTuple() {

    }

}
