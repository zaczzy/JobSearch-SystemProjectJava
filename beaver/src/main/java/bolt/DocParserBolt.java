package bolt;

import model.DocObj;
import org.apache.storm.Config;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.apache.storm.utils.TupleUtils;
import org.apache.log4j.Logger;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.Map;

/**
 * Bolt One
 *
 * Receive: ("Id", "doc")
 * Output: ("Id", "docObj")
 *
 * Note: apply stopwords, lemmatization, stemming, etc
 */

public class DocParserBolt extends BaseRichBolt {
    private OutputCollector collector;

    public DocParserBolt() {

    }

    @Override
    public void prepare(Map conf, TopologyContext context, OutputCollector collector) {
        this.collector = collector;
    }

    @Override
    public void execute(Tuple input) {
        String content = input.getStringByField("doc");
        Document doc = Jsoup.parse(content);

        String title = doc.title();
        Elements meta = doc.getElementsByTag("meta");
        Elements headerOne = doc.select("h1");
        Elements headerTwo = doc.select("h2");
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("Id", "docObj"));
    }
}
