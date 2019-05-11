package bolt;

import aws.s3.S3Service;
import model.Sentinel;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichBolt;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import java.util.Map;

public class DocDownloaderBolt implements IRichBolt {

    private OutputCollector collector;
    private Sentinel sentinel;

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        this.collector = outputCollector;
        this.sentinel = Sentinel.getInstance();
    }

    @Override
    public void execute(Tuple tuple) {
        //sentinel.setWorking(true);
        String docName = tuple.getStringByField("docName");
        String id = tuple.getStringByField("Id");
        int pagerank = tuple.getIntegerByField("pagerank");
        //sentinel.setBuffer(false);
        try {
            String content = S3Service.getInstance().getFileAsString(docName);
            System.out.println("[DOC NAME 💫]:" + docName + "[DOC ID ⛱]" + id);
            //sentinel.setBuffer(true);
            collector.emit(new Values(id, pagerank, content));
        } catch (Exception e) {
            e.printStackTrace();
        }
        collector.ack(tuple);
        //sentinel.setWorking(false);
    }

    @Override
    public void cleanup() {

    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("Id", "pagerank", "doc"));
    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        return null;
    }
}
