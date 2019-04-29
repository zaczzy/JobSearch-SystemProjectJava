package bolt;

import aws.s3.S3Service;
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

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        this.collector = outputCollector;
    }

    @Override
    public void execute(Tuple tuple) {
        String docName = tuple.getStringByField("docName");
        try {
            String content = S3Service.getInstance().getFileAsString(docName);
            /* TODO: should fix id issue*/
            int id = content.length();
            System.out.println("[DOC NAME 💫]:" + docName + "[DOC ID ⛱]" + id);
            collector.emit(new Values(id, content));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void cleanup() {

    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("Id", "doc"));
    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        return null;
    }
}
