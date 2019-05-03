package bolt;

import model.DocObj;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.storm.Config;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichBolt;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.apache.storm.utils.TupleUtils;
import org.apache.log4j.Logger;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.util.Version;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import edu.stanford.nlp.simple.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Bolt One
 *
 * Receive: ("Id", "doc")
 * Output: ("Id", "word", "position", "weight")
 *
 * Note: apply stopwords, lemmatization, stemming, etc
 */

public class DocParserBolt implements IRichBolt {
    private OutputCollector collector;
    String executorId = UUID.randomUUID().toString();

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
        Analyzer analyzer_title = new StandardAnalyzer();
        TokenStream tokenStream = analyzer_title.tokenStream("content", title);
        CharTermAttribute attr = tokenStream.addAttribute(CharTermAttribute.class);
        try {
            tokenStream.reset();
            while (tokenStream.incrementToken()) {
                collector.emit(new Values(id, attr.toString(), pos, title_w));
                pos++;
            }
        } catch (IOException e) {

        }

        //Parse meta data
        for(Element ele : meta) {
            String text = ele.text();
            Analyzer analyzer_meta = new StandardAnalyzer();
            tokenStream = analyzer_meta.tokenStream("content", text);
            attr = tokenStream.addAttribute(CharTermAttribute.class);
            try {
                tokenStream.reset();
                while (tokenStream.incrementToken()) {
                    collector.emit(new Values(id, attr.toString(), pos, meta_w));
                    pos++;
                }
            } catch (IOException e) {

            }
        }
        //Parse h1
        for(Element ele : headerOne) {
            String text = ele.text();
            Analyzer analyzer_h1 = new StandardAnalyzer();
            tokenStream = analyzer_h1.tokenStream("content", text);
            attr = tokenStream.addAttribute(CharTermAttribute.class);
            try {
                tokenStream.reset();
                while (tokenStream.incrementToken()) {
                    collector.emit(new Values(id, attr.toString(), pos, headerOne_w));
                    pos++;
                }
            } catch (IOException e) {

            }
        }
        //Parse h2
        for(Element ele : headerTwo) {
            String text = ele.text();
            Analyzer analyzer_h2 = new StandardAnalyzer();
            tokenStream = analyzer_h2.tokenStream("content", text);
            attr = tokenStream.addAttribute(CharTermAttribute.class);
            try {
                tokenStream.reset();
                while (tokenStream.incrementToken()) {
                    collector.emit(new Values(id, attr.toString(), pos, headerTwo_w));
                    pos++;
                }
            } catch (IOException e) {

            }
        }
        //Parse body
        String body = doc.body().text();
        Analyzer analyzer_body = new StandardAnalyzer();
        tokenStream = analyzer_body.tokenStream("content", body);
        attr = tokenStream.addAttribute(CharTermAttribute.class);
        try {
            tokenStream.reset();
            while (tokenStream.incrementToken()) {
                collector.emit(new Values(id, attr.toString(), pos, 1));
                pos++;
            }
        } catch (IOException e) {

        }
        //emit EOS
        collector.emit(new Values(id, "EOS", pos, -1));
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("Id", "word", "position", "weight"));
    }

    @Override
    public void cleanup() {

    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        return null;
    }
}
