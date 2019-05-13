import aws.rds.Credentials;
import aws.rds.Keyword;
import aws.s3.S3Service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.chimbori.crux.articles.Article;
import com.chimbori.crux.articles.ArticleExtractor;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.ModelListener;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.*;
import java.util.concurrent.*;

public class AdvancedAgent {

    /* records related to words */
    ConcurrentHashMap<String, Double> queryWordToIdf;
    ConcurrentHashMap<String, Double> queryWordToWtf;
    ConcurrentHashMap<String, HashSet<String>> queryWordToDocuments;

    /* records related to documents */
    ConcurrentHashMap<String, Double> docCosineSimScores;
    ConcurrentHashMap<String, Double> docPageRankScores;
    ConcurrentHashMap<String, List<List<Integer>>> docToPosList;
    ConcurrentHashMap<String, String> docToUrl;
    ConcurrentHashMap<String, int[]> positions;

    /* Results */
    PriorityQueue<DocumentRank> ranks;
    List<String> finalRanking;
    ConcurrentHashMap<String, SearchResult> results;

    /* records related to document */

    public AdvancedAgent(String rawQuery) {
        this.queryWordToIdf = new ConcurrentHashMap<>();
        this.queryWordToWtf = new ConcurrentHashMap<>();
        this.queryWordToDocuments = new ConcurrentHashMap<>();
        this.docCosineSimScores = new ConcurrentHashMap<>();
        this.docPageRankScores = new ConcurrentHashMap<>();
        this.docToPosList = new ConcurrentHashMap<>();
        this.docToUrl = new ConcurrentHashMap<>();
        this.positions = new ConcurrentHashMap<>();
        this.results = new ConcurrentHashMap<>();
        this.finalRanking = new ArrayList<>();
        this.ranks = new PriorityQueue<DocumentRank>(30, new Comparator<DocumentRank>() {
            @Override
            public int compare(DocumentRank o1, DocumentRank o2) {
                return (o2.score - o1.score) >= 0 ? 1 : -1;
            }
        });
        populateQueryWtf(rawQuery);
        fetchAndUpdate();
        List<String> finalists = mergeSets();
        for (String s : finalists) {
            System.out.println(docToUrl.get(s));
        }
        populatePriorityQueue(finalists);
        for (int i = 0; i < 20; i++)  {
            if (ranks == null) {
                System.out.println("🛑 WTF???");
            }
            DocumentRank rank = ranks.poll();
            if (rank == null) { break; }
            finalRanking.add(rank.docId);
        }
        populateResults();
    }

    /**
     * Calculate Weighted Frequency for each query word
     */
    private void populateQueryWtf(String query) {
        Double euclideanSum = 0.0;
        HashMap<String, Double> record = new HashMap<>();
        String[] words = query.split("\\P{L}+");
        for (String s : words) {
            String sk = s.toLowerCase();
            if (sk != null && !sk.equals("") && sk.length() > 0) {
                record.put(sk, record.getOrDefault(sk, 0.0) + 1.0);
            }
        }
        for (Map.Entry<String, Double> entry : record.entrySet()) {
            euclideanSum += entry.getValue();
        }
        euclideanSum = Math.sqrt(euclideanSum);
        for (Map.Entry<String, Double> entry : record.entrySet()) {
            queryWordToWtf.put(entry.getKey(), entry.getValue() / euclideanSum);
        }
        System.out.println("👼 Query WTF: " + queryWordToWtf.toString());
    }

    private void fetchAndUpdate() {
        ExecutorService fetcherPool = Executors.newFixedThreadPool(queryWordToWtf.size());
        CountDownLatch latch = new CountDownLatch(queryWordToWtf.size());
        System.out.println("latch!" + latch.toString());
        for (Map.Entry<String, Double> entry : queryWordToWtf.entrySet()) {
            fetcherPool.submit(new DBFetcherTask(entry.getKey(), latch));
        }
        try {
            latch.await();
            System.out.println(queryWordToIdf.toString());
            System.out.println(docPageRankScores.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void populatePriorityQueue(List<String> documents) {
        for (String id : documents) {
            Double cosineScore = docCosineSimScores.get(id);
            Double pageRankScore = docPageRankScores.get(id);
//            int[] pairs = ClosestPair.findClosestIndices(docToPosList.get(id));
//            if (pairs == null) {
//                System.out.println("[❌] Closest pair not found");
//                continue;
//            }
//            int closeness = pairs[0];
//            this.positions.put(id, pairs);
            System.out.println(id + ": " + "cosine = " + cosineScore + ", pageRank = " + pageRankScore + ", url=" + docToUrl.get(id));
            Double score = cosineScore * 8 + Math.log(2 + pageRankScore) * 2;
            DocumentRank rank = new DocumentRank(id, score);
            ranks.add(rank);
        }
    }

    public void populateResults() {
        ExecutorService fetcherPool = Executors.newFixedThreadPool(finalRanking.size());
        CountDownLatch latch = new CountDownLatch(finalRanking.size());
        for (String id : finalRanking) {
            fetcherPool.submit(new ContentQueryThread(id, latch));
        }
        try {
            latch.await();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<String> mergeSets() {
        int i = 0;
        HashSet<String> set = null;
        for (Map.Entry<String, HashSet<String>> entry : queryWordToDocuments.entrySet()) {
            if (i == 0) {
                set = entry.getValue();
            } else {
                set.retainAll(entry.getValue());
            }
            i++;
        }
        return new ArrayList<String>(set);
    }

    public List<SearchResult> getResults() {
        List<SearchResult> sr = new ArrayList<>();
        for (String id : finalRanking) {
            sr.add(results.get(id));
        }
        return sr;
    }

    private class DocumentRank{
        String docId; double score;
        public DocumentRank(String docId, double score) {
            this.docId = docId; this.score = score;
        }
    }

    /**
     * Preprocess
     */

    /**
     * DBFetcher
     */
    private class DBFetcherTask implements Runnable {
        String word; CountDownLatch latch;
        public DBFetcherTask(String word, CountDownLatch latch) { this.word = word; this.latch = latch; }
        @Override
        public void run() {
            Base.open(Credentials.jdbcDriver, Credentials.dbUrl, Credentials.dbUser, Credentials.dbUserPW);
            List<Keyword> keywords = Keyword.findBySQL("SELECT * FROM keywords WHERE word='" + word +"'");
            queryWordToIdf.put(word, getIDF(keywords));
            queryWordToWtf.put(word, queryWordToWtf.get(word) * queryWordToIdf.get(word));
            keywords.stream().forEach((entry) -> {
                String docId = entry.getString("docid");
                String url = entry.getString("url");
                double pageRank = entry.getDouble("pagerank");
                double wtf = entry.getDouble("wtf");
                List<Integer> hits = convertStringToHitList(entry.getString("hits"));
                /* URL */
                docToUrl.put(docId, url);
                /* Page Rank */
                docPageRankScores.putIfAbsent(docId, pageRank);
                /* Position List */
                synchronized (docToPosList) {
                    if (docToPosList.containsKey(docId)) {
                        List<List<Integer>> poses = docToPosList.get(docId);
                        poses.add(hits);
                        docToPosList.put(docId, poses);
                    } else {
                        List<List<Integer>> list = new ArrayList<>();
                        list.add(hits);
                        docToPosList.put(docId, list);
                    }
                }
                /* Cosine Similarity */
                docCosineSimScores.put(docId, docCosineSimScores.getOrDefault(docId, 0.0) + wtf * queryWordToWtf.get(word));
                /* Document Hash Set */
                synchronized (queryWordToDocuments) {
                    if (queryWordToDocuments.containsKey(word)) {
                        HashSet<String> docs = queryWordToDocuments.get(word);
                        docs.add(docId);
                        queryWordToDocuments.put(word, docs);
                    } else {
                        HashSet<String> docs = new HashSet<>();
                        docs.add(docId);
                        queryWordToDocuments.put(word, docs);
                    }
                }
            });
            System.out.println(word + ":" + getIDF(keywords));
            latch.countDown();
        }
    }

    class ContentQueryThread extends Thread {
        String docId; CountDownLatch latch;
        public ContentQueryThread(String docId, CountDownLatch latch) { this.docId = docId; this.latch = latch; }
        @Override
        public void run() {
            Article article = null;
            try {
                String url = docId + ".html";
                String content = S3Service.getInstance().getFileAsString(url);
                Document doc = Jsoup.parse(content);
                article = ArticleExtractor.with("", doc).extractMetadata().extractContent().article();
            } catch (Exception e) {
                System.err.println(docId);
                e.printStackTrace();
            }
            if (article == null) {
                System.err.println("[❌ERROR:] Article is NULL !!!");
            }
            String title = article.title;
            String url = docToUrl.get(docId);
            String excerpt = getExerptFor(article);
            results.put(docId, new SearchResult(title, url, excerpt));
            latch.countDown();
        }
    }

    private String getExerptFor(Article article) {
        String mainContent = article.description + article.document.text();
        if (mainContent.length() < 500) {
            return mainContent;
        } else {
            return mainContent.substring(0, 500);
        }
    }

    private double getIDF(List<Keyword> keywords) {
        double total = 700000; double count = 0;
        if (keywords.size()  < 1) { return 0.01; }
        if (keywords.size() < 1000)  { count = keywords.size(); }
        else {
            Double last = 1 + keywords.get(keywords.size() - 1).getDouble("pagerank");
            int multiplier = Long.valueOf(Math.round(last)).intValue();
            count = 2000 * multiplier;
        }
        return Math.log(total / count);
    }

    private List<Integer> convertStringToHitList(String hitList) {
        List<Integer> list = JSON.parseObject(hitList, new TypeReference<List<Integer>>() {});
        return list;
    }
}
