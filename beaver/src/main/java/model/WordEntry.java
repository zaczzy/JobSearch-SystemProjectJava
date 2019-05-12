package model;

public class WordEntry {

    private String word, docid, hits;
    private int tf, pagerank;
    private double wtf, normalizer;

    public WordEntry(String word, String docid,String hits,int tf, int pagerank, double wtf, double normalizer) {
        this.word = word;
        this.docid = docid;
        this.hits = hits;
        this.tf = tf;
        this.pagerank = pagerank;
        this.wtf = wtf;
        this.normalizer = normalizer;
    }

    public double getNormalizer() {
        return normalizer;
    }

    public double getWtf() {
        return wtf;
    }

    public int getPagerank() {
        return pagerank;
    }

    public int getTf() {
        return tf;
    }

    public String getDocid() {
        return docid;
    }

    public String getHits() {
        return hits;
    }

    public String getWord() {
        return word;
    }
}
