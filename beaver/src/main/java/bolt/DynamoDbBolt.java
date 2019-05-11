package bolt;

import aws.dynamodb.DynamoDBService;
import model.DocObj;
import model.Sentinel;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichBolt;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Level;
import org.apache.storm.tuple.Values;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public class DynamoDbBolt implements IRichBolt {
    private OutputCollector collector;

    @Override
    public void prepare(Map conf, TopologyContext context, OutputCollector collector) {
        this.collector = collector;
    }

    @Override
    public void execute(Tuple tuple) {
        String docName = tuple.getStringByField("docName");
        int index = docName.lastIndexOf('.');
        if(index < 0) {
            index = docName.length();
        }
        String id = docName.substring(0, index);

        //TODO: read pagerank from DynamoDB
        //Map<String, AttributeValue> table = DynamoDBService.getInstance().get(id);
        int pagerank = 100;
        collector.ack(tuple);
        collector.emit(new Values(docName, id, pagerank));
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
