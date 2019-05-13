package cache;

import java.util.List;

public class WordQryResult {
  String docId;
  String url;
  double pageRank ;
  double wtf;
  List<Integer> hits;

  public WordQryResult(String docId, String url, double pageRank, double wtf, List<Integer> hits) {
    this.docId = docId;
    this.url = url;
    this.pageRank = pageRank;
    this.wtf = wtf;
    this.hits = hits;
  }

  public String getDocId() {
    return docId;
  }

  public String getUrl() {
    return url;
  }

  public double getPageRank() {
    return pageRank;
  }

  public double getWtf() {
    return wtf;
  }

  public List<Integer> getHits() {
    return hits;
  }
}
