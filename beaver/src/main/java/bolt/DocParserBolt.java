package bolt;

import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichBolt;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.chimbori.crux.articles.ArticleExtractor;
import com.chimbori.crux.articles.Article;

import java.io.IOException;
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

    public DocParserBolt() { }

    @Override
    public void prepare(Map conf, TopologyContext context, OutputCollector collector) {
        this.collector = collector;
    }

    private class IntegerWrapper {
        private int val = 0;
        public IntegerWrapper(int i) { this.val = i; }
        public int incr() { return val++; }
    }

    @Override
    public void execute(Tuple input) {
        String content = input.getStringByField("doc");
        String id = input.getStringByField("Id");
        int pagerank = input.getIntegerByField("pagerank");

        // Parse with Jsoup
        Document doc = Jsoup.parse(content);

        // Parse with Crux
        Article article = ArticleExtractor.with("", doc).extractMetadata().extractContent().article();

        // Eliminate irrelevant tags
        doc.select("select").remove();
        doc.select("script").remove();
        doc.select("form").remove();

        Document newdoc = article.document;
        if (newdoc == null) {
            System.err.println("Crux: NULL");
            return;
        }

        // Extract elements
        String body = "";
        if (newdoc.text().length() >= doc.body().text().length() / 5) {
            doc = newdoc;
            body = newdoc.text();
        } else {
            Element ele = doc.body();
            if(ele != null) { body = ele.text(); }
        }
        //TODO: Send body off an additional branch

        String title = article.title;
        String description = article.description;
        Elements headerOne = doc.select("h1");
        Elements headerTwo = doc.select("h2");

        // Iterate and emit
        IntegerWrapper pos = new IntegerWrapper(0);

        sendSentence(title, title_w, id, pagerank, pos);
        sendSentence(description, meta_w, id, pagerank, pos);

        sendElements(headerOne, headerOne_w, id, pagerank);
        sendElements(headerTwo, headerTwo_w, id, pagerank);
        sendSentence(body, 1, id, pagerank, pos);

        //emit EOS
        collector.emit(new Values(id, "EOS", pos, -1, pagerank));
    }

    private void sendElements(Elements elements, int weight, String docId, int pagerank) {
        Analyzer analyzer = new StandardAnalyzer();
        TokenStream tokenStream = null;
        for(Element ele : elements) {
            String text = ele.text();
            tokenStream = analyzer.tokenStream("content", text);
            CharTermAttribute attr = tokenStream.addAttribute(CharTermAttribute.class);
            try {
                tokenStream.reset();
                while (tokenStream.incrementToken()) {
                    collector.emit(new Values(docId, attr.toString(), -1, weight, pagerank));
                }
                tokenStream.reset();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendSentence(String sentence, int weight, String docId, int pagerank, IntegerWrapper pos) {
        if(sentence != null && !sentence.equals("")) {
            Analyzer analyzer = new StandardAnalyzer();
            TokenStream tokenStream = analyzer.tokenStream("content", sentence);
            CharTermAttribute attr = tokenStream.addAttribute(CharTermAttribute.class);
            try {
                tokenStream.reset();
                while (tokenStream.incrementToken()) {
                    collector.emit(new Values(docId, attr.toString(), pos.incr(), weight, pagerank));
                }
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("Id", "word", "position", "weight", "pagerank"));
    }

    @Override
    public void cleanup() { }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        return null;
    }

}
