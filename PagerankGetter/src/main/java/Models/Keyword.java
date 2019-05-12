package Models;

import bolt.PageRankConsultant;

/* A New Model for words */
public class Keyword {
    public String word;
    public String docId;
    public String hits;
    public int tf;
    public double wtf;
    public double normalizer;
    public double pagerank;
    public String url;

    public Keyword(Word origin) {
        this.word = origin.getString("word");
        this.docId = origin.getString("docid");
        this.url = PageRankConsultant.getInstance().getUrlOf(docId);
        this.hits = origin.getString("hits");
        this.tf = origin.getInteger("tf");
        this.wtf = origin.getDouble("wtf");
        this.normalizer = origin.getDouble("normalizer");
        this.pagerank = PageRankConsultant.getInstance().getRankOrDefault(docId, 0.1);
        this.url = PageRankConsultant.getInstance().getUrlOf(docId);
    }

    public Keyword(String word, String docid, String url, String hits, int tf, double wtf, double normalizer, double pagerank) {
        this.word = word;
        this.docId = docid;
        this.url = url;
        this.hits = hits;
        this.tf = tf;
        this.wtf = wtf;
        this.normalizer = normalizer;
        this.pagerank = pagerank;
        this.url = url;
    }
}
