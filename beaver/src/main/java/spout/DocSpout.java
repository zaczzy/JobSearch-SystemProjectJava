package spout;

import java.util.ArrayList;
import java.util.Map;
import java.util.List;
import java.util.Random;
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichSpout;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;

/**
 * Receive: fetch from S3
 * Output: ("Id", "doc")
 */
public class DocSpout implements IRichSpout {

    private SpoutOutputCollector collector;
    //Data structure for testing purpose
    private List<String> documents = new ArrayList<String>();
    private int index = 0;

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("Id", "doc"));
    }

    @Override
    public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
        documents.add("<html><head><title>Amy</title></head><body>My name is Amy. I likes singing.</body></html>");
        documents.add("<html><head><title>France</title></head><body>In 2017, he went to Paris, France in the summer.</body></html>");
        documents.add("<html><head><title>Jane</title></head><body>After hearing about Joe's trip, Jane decided she might go to France one day.</body></html>");
        this.collector = collector;
    }

    @Override
    public void nextTuple() {
        if(index < 3) {
            collector.emit(new Values(index, documents.get(index)));
            index++;
        }
    }

    @Override
    public void close() {

    }

    @Override
    public void activate() {

    }

    @Override
    public void deactivate() {

    }

    @Override
    public void ack(Object id) {
    }

    @Override
    public void fail(Object id) {
    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        return null;
    }
}
