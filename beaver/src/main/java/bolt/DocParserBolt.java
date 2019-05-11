package bolt;

import model.DocObj;
import model.Sentinel;
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

import com.chimbori.crux.articles.ArticleExtractor;
import com.chimbori.crux.articles.Article;

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
    private Sentinel sentinel;
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
        this.sentinel = Sentinel.getInstance();
    }

    @Override
    public void execute(Tuple input) {
        sentinel.setWorking(true);
        String content = input.getStringByField("doc");
        String id = input.getStringByField("Id");
        int pagerank = input.getIntegerByField("pagerank");
        sentinel.setBuffer(false);

        // Parse with Jsoup
        Document doc = Jsoup.parse(content);
        Elements meta = doc.getElementsByTag("meta");

        // Parse with Crux
        Article article = ArticleExtractor.with("", doc).extractMetadata().extractContent().article();

        Document newdoc = article.document;
        if(newdoc == null) {
            System.err.println("Crux: NULL");
            sentinel.setWorking(false);
            return;
        }

        // Extract elements
        String body = "";
        if(newdoc.text().length() >= doc.body().text().length() / 5) {
            doc = newdoc;
            body = newdoc.text();
        } else {
            Element ele = doc.body();
            if(ele != null) {
                body = ele.text();
            }
        }

        String title = doc.title();
        Elements headerOne = doc.select("h1");
        Elements headerTwo = doc.select("h2");

        // Iterate and emit
        int pos = 0;
        //Parse title
        if(title != null && !title.equals("")) {
            Analyzer analyzer_title = new StandardAnalyzer();
            TokenStream tokenStream = analyzer_title.tokenStream("content", title);
            CharTermAttribute attr = tokenStream.addAttribute(CharTermAttribute.class);
            try {
                tokenStream.reset();
                while (tokenStream.incrementToken()) {
                    sentinel.setBuffer(true);
                    collector.emit(new Values(id, attr.toString(), pos, title_w, pagerank));
                    pos++;
                }
            } catch (IOException e) {

            }
        }

        if(title == null) {
            title = "[No title for this document]";
        }
        //Parse meta data
        for(Element ele : meta) {
            String text = ele.attr("content");
            Analyzer analyzer_meta = new StandardAnalyzer();
            TokenStream tokenStream = analyzer_meta.tokenStream("content", text);
            CharTermAttribute attr = tokenStream.addAttribute(CharTermAttribute.class);
            try {
                tokenStream.reset();
                while (tokenStream.incrementToken()) {
                    sentinel.setBuffer(true);
                    collector.emit(new Values(id, attr.toString(), pos, meta_w, pagerank));
                    pos++;
                }
            } catch (IOException e) {

            }
        }
        //Parse h1
        for(Element ele : headerOne) {
            String text = ele.text();
            Analyzer analyzer_h1 = new StandardAnalyzer();
            TokenStream tokenStream = analyzer_h1.tokenStream("content", text);
            CharTermAttribute attr = tokenStream.addAttribute(CharTermAttribute.class);
            try {
                tokenStream.reset();
                while (tokenStream.incrementToken()) {
                    sentinel.setBuffer(true);
                    collector.emit(new Values(id, attr.toString(), -1, headerOne_w, pagerank));
                }
            } catch (IOException e) {

            }
        }
        //Parse h2
        for(Element ele : headerTwo) {
            String text = ele.text();
            Analyzer analyzer_h2 = new StandardAnalyzer();
            TokenStream tokenStream = analyzer_h2.tokenStream("content", text);
            CharTermAttribute attr = tokenStream.addAttribute(CharTermAttribute.class);
            try {
                tokenStream.reset();
                while (tokenStream.incrementToken()) {
                    sentinel.setBuffer(true);
                    collector.emit(new Values(id, attr.toString(), -1, headerTwo_w, pagerank));
                }
            } catch (IOException e) {

            }
        }
        //Parse body
        Analyzer analyzer_body = new StandardAnalyzer();
        TokenStream tokenStream = analyzer_body.tokenStream("content", body);
        CharTermAttribute attr = tokenStream.addAttribute(CharTermAttribute.class);
        try {
            tokenStream.reset();
            while (tokenStream.incrementToken()) {
                sentinel.setBuffer(true);
                collector.emit(new Values(id, attr.toString(), pos, 1, pagerank));
                pos++;
            }
        } catch (IOException e) {

        }

        //emit EOS
        sentinel.setBuffer(true);
        collector.emit(new Values(id, "EOS", pos, -1, pagerank));
        sentinel.setWorking(false);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("Id", "word", "position", "weight", "pagerank"));
    }

    @Override
    public void cleanup() {

    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        return null;
    }

}
