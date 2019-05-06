package spout;

import java.util.ArrayList;
import java.util.Map;
import java.util.List;
import java.util.Random;

import aws.s3.S3Service;
import model.Sentinel;
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
    int index = 0;
    List<String> fileNames;
    private Sentinel sentinel;
    private boolean finished = false;

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("docName"));
    }

    @Override
    public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
        this.collector = collector;
        this.sentinel = Sentinel.getInstance();
        this.sentinel.inc();
        fileNames = S3Service.getInstance().listAllFiles("documents/2/");
    }

    @Override
    public void nextTuple() {
        if (index < fileNames.size()) {
            collector.emit(new Values(fileNames.get(index)));
            index++;
        } else if(!finished) {
            sentinel.dec();
            finished = true;
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
