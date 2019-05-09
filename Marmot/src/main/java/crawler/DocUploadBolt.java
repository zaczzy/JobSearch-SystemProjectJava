package crawler;

import aws.dynamoDB.DynamoDBService;
import aws.s3.S3Service;
import crawler.info.URLInfo;
import model.CrawlerConfig;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichBolt;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

public class DocUploadBolt implements IRichBolt {
//	static Logger log = LogManager.getLogger(DocFetcherBolt.class);
	String executorId = UUID.randomUUID().toString();
	Fields schema = new Fields("url", "content", "type", "id");

	OutputCollector collector;

	/**
	 * Called when a bolt is about to be shut down
	 */
	@Override
	public void cleanup() {

	}

	/**
	 * Processes a tuple
	 *
	 * @param input
	 */
	@Override
	public void execute(Tuple input) {
		String content = input.getStringByField("content");
		String suffix = "." + input.getStringByField("type");
		String url = input.getStringByField("url");
		URLInfo info = new URLInfo(url);
		String docID = input.getStringByField("id");
		try {
			S3Service.getInstance().putFile("test3/" + docID + suffix, content);
			System.out.println("Upload " + url);
			String[][] fields = new String[4][2];
			fields[0][0] = "url";
			fields[0][1] = input.getStringByField("url");
			fields[1][0] = "host";
			fields[1][1] = info.getHostName();
			fields[2][0] = "added time";
			fields[2][1] = new Date().toString();
			fields[3][0] = "isNew";
			fields[3][1] = "true";
			DynamoDBService.getInstance().put(docID, fields);
			CrawlerConfig.incPagesStored();
		} catch (IOException | software.amazon.awssdk.core.exception.SdkClientException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Called when this task is initialized
	 *
	 * @param conf
	 * @param context
	 * @param collector
	 */
	@Override
	public void prepare(Map conf, TopologyContext context, OutputCollector collector) {
		this.collector = collector;
	}


	/**
	 * Get the list of fields in the stream tuple
	 *
	 * @return
	 */
	public Fields getSchema() {
		return this.schema;
	}

	public String getExecutorId() {
		return this.executorId;
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(schema);
	}

	/**
	 * Declare configuration specific to this component. Only a subset of the "topology.*" configs can
	 * be overridden. The component configuration can be further overridden when constructing the
	 * topology using {@link }
	 */
	@Override
	public Map<String, Object> getComponentConfiguration() {
		return null;
	}
}
