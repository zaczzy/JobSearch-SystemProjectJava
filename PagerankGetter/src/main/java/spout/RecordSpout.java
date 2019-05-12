package spout;

import Models.Word;
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichSpout;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import org.apache.storm.utils.Utils;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.ModelListener;

import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Receive: fetch from S3
 * Output: ("Id", "doc")
 */
public class RecordSpout implements IRichSpout {

    private SpoutOutputCollector collector;
    int index = 0;
    private LinkedBlockingQueue<Word> recordQueue;
    private int requestOffset;
    private Thread loader;

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("entry"));
    }

    @Override
    public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
        this.collector = collector;
        this.requestOffset = 12350000;
        this.recordQueue = new LinkedBlockingQueue<>();
        fetchMoreRecords();
    }

    private void fetchMoreRecords() {
        Utils.sleep(50);
        if (loader == null || loader.getState() == Thread.State.TERMINATED) {
            loader = new Thread() {
                @Override
                public void run() {
                    Base.open(aws.rds.Credentials.Remote.jdbcDriver, aws.rds.Credentials.Remote.dbUrl,
                            aws.rds.Credentials.Remote.dbUser, aws.rds.Credentials.Remote.dbUserPW);
                    int end = requestOffset + 10000;
                    System.out.println("[⛱ NEW REQUEST:] BETWEEN " + requestOffset + " AND " + end);
                    Word.find("id BETWEEN " + requestOffset + " AND " + end, new ModelListener<Word>() {
                        @Override
                        public void onModel(Word word) {
                            recordQueue.add(word);
                        }
                    });
                    Base.close();
                    requestOffset = requestOffset + 10000;
                }
            };
            loader.start();
        }
    }

    @Override
    public void nextTuple() {
        if (recordQueue.size() < 5) {
            fetchMoreRecords();
        }
        try {
            Word name = recordQueue.take();
            collector.emit(new Values(name), index);
            index++;
        } catch(InterruptedException e) {
            e.printStackTrace();
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
        System.out.println(id);
        System.err.println("FAILED!");
    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        return null;
    }
}
