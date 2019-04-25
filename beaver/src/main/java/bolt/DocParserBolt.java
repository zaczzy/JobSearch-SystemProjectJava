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

import edu.stanford.nlp.simple.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Bolt One
 *
 * Receive: ("Id", "doc")
 * Output: ("Id", "word", "position", "weight")
 *
 * Note: apply stopwords, lemmatization, stemming, etc
 */

public class DocParserBolt extends BaseRichBolt {
    private OutputCollector collector;
    private static int title_w = 5;
    private static int meta_w = 4;
    private static int headerOne_w = 2;
    private static int headerTwo_w = 1;

    public DocParserBolt() {

    }

    @Override
    public void prepare(Map conf, TopologyContext context, OutputCollector collector) {
        this.collector = collector;
    }

    @Override
    public void execute(Tuple input) {
        String content = input.getStringByField("doc");
        int id = input.getIntegerByField("Id");
        Document doc = Jsoup.parse(content);

        String title = doc.title();
        Elements meta = doc.getElementsByTag("meta");
        Elements headerOne = doc.select("h1");
        Elements headerTwo = doc.select("h2");

        int pos = 0;
        //Parse title
        Sentence t = new Sentence(title);
        List<String> lemmas = t.lemmas();
        for(String lemma : lemmas) {
            collector.emit(new Values(id, lemma, pos, title_w));
            pos++;
        }
        //Parse meta data
        for(Element ele : meta) {
            String text = ele.text();
            lemmas = new Sentence(text).lemmas();
            for(String lemma : lemmas) {
                collector.emit(new Values(id, lemma, pos, meta_w));
                pos++;
            }
        }
        //Parse h1
        for(Element ele : headerOne) {
            String text = ele.text();
            lemmas = new Sentence(text).lemmas();
            for(String lemma : lemmas) {
                collector.emit(new Values(id, lemma, -1, headerOne_w));
            }
        }
        //Parse h2
        for(Element ele : headerTwo) {
            String text = ele.text();
            lemmas = new Sentence(text).lemmas();
            for(String lemma : lemmas) {
                collector.emit(new Values(id, lemma, -1, headerTwo_w));
            }
        }
        //Parse body
        String body = doc.body().text();
        edu.stanford.nlp.simple.Document document = new edu.stanford.nlp.simple.Document(body);
        for(Sentence sent : document.sentences()) {
            lemmas = sent.lemmas();
            for(String lemma : lemmas) {
                collector.emit(new Values(id, lemma, pos, 1));
                pos++;
            }
        }
        //emit EOS
        collector.emit(new Values(id, "EOS", pos, -1));
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("Id", "word", "position", "weight"));
    }
}
