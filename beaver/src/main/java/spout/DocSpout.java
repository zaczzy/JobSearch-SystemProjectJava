package spout;

import java.util.ArrayList;
import java.util.Map;
import java.util.List;
import java.util.Random;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import aws.s3.S3Service;
import model.Sentinel;
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichSpout;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import org.apache.storm.utils.Utils;

/**
 * Receive: fetch from S3
 * Output: ("Id", "doc")
 */
public class DocSpout implements IRichSpout {

    private SpoutOutputCollector collector;
    int index = 0;
    private LinkedBlockingQueue<String> fileNames;
    private Sentinel sentinel;
    private boolean finished = false;
    private final String folder = "test3/";
    private Loader loader;

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("docName"));
    }

    @Override
    public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
        this.collector = collector;
        this.sentinel = Sentinel.getInstance();
        sentinel.setWorking(true);
        this.fileNames = new LinkedBlockingQueue<>();
        this.loader = new Loader(folder, fileNames);
        loader.start();
        System.out.println("Loader started!");
    }

    @Override
    public void nextTuple() {
        try {
            String name = fileNames.take();
            collector.emit(new Values(name), index);
            index++;
        } catch(InterruptedException e) {

        }
    }

    @Override
    public void close() {
        System.out.println("SPOUT INDEX: " + Integer.toString(index));
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
        System.err.println("FAILED!");
    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        return null;
    }
}
