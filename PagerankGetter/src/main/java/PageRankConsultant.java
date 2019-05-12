import dynamodb.PageRankGetter;

import java.util.Map;

public class PageRankConsultant {
    private Map<String, Double> pageRank;
    private Map<String, String> idToURL;

    private static PageRankConsultant instance;

    public static PageRankConsultant getInstance() {
        if (instance == null) {
            try {
                instance = new PageRankConsultant();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return instance;
    }

    private PageRankConsultant() throws Exception {
        this.pageRank = PageRankGetter.getAll();
        this.idToURL = PageRankGetter.getUrl();
        PageRankGetter.clean();
    }

    public double getRankOf(String docId) {
        return pageRank.get(docId);
    }

    public String getUrlOf(String docId) {
        return idToURL.get(docId);
    }

}
