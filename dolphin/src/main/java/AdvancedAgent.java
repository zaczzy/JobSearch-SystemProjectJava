import aws.rds.Credentials;
import aws.rds.Keyword;
import aws.s3.S3Service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.chimbori.crux.articles.Article;
import com.chimbori.crux.articles.ArticleExtractor;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.DB;
import org.javalite.activejdbc.ModelListener;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

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
        this.ranks = new PriorityQueue<DocumentRank>(50, new Comparator<DocumentRank>() {
            @Override
            public int compare(DocumentRank o1, DocumentRank o2) {
                return (o2.score - o1.score) >= 0 ? 1 : -1;
            }
        });
        long start = System.currentTimeMillis();
        System.out.println("Start Query ");
        populateQueryWtf(rawQuery);
        System.out.println("Before FetchAndUpdate(): +" + (System.currentTimeMillis() - start));
        fetchAndUpdate();
        System.out.println("Before mergeSets(): +" + (System.currentTimeMillis() - start));
        List<String> finalists = mergeSets();
        System.out.println("Before populatePriorityQueue() : +" + (System.currentTimeMillis() - start));
        populatePriorityQueue(finalists);
        System.out.println("Before populateResults() : +" + (System.currentTimeMillis() - start));
        for (int i = 0; i < 50; i++)  {
            DocumentRank rank = ranks.poll();
            if (rank == null) { break; }
            finalRanking.add(rank.docId);
        }
        populateResults();
        System.out.print("Finish : +" + (System.currentTimeMillis() - start));
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
    }

    private void fetchAndUpdate() {
        CountDownLatch latch = new CountDownLatch(queryWordToWtf.size());
        for (Map.Entry<String, Double> entry : queryWordToWtf.entrySet()) {
            new DBFetcherTask(entry.getKey(), latch).start();
        }
        try {
            latch.await();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void populatePriorityQueue(List<String> documents) {
        HashSet<String> strippedUrls = new HashSet<>();
        for (String id : documents) {
            String first = docToUrl.get(id).split("#")[0];
            if (strippedUrls.contains(first)) { continue; }
            strippedUrls.add(first);
            Double cosineScore = docCosineSimScores.get(id);
            Double pageRankScore = docPageRankScores.get(id);
            int[] pairs = ClosestPair.findClosestIndices(docToPosList.get(id));
            double closeness = pairs[0];
            /* TODO: add closeness to the metrix */
            Double score;
            if (closeness > 0) {
                score = cosineScore * 8 + sigmoid(2 + pageRankScore) * 2 + 2 * (5.0 / closeness);
            } else {
                score = cosineScore * 8 + sigmoid(2 + pageRankScore) * 2;
            }
            DocumentRank rank = new DocumentRank(id, score);
            ranks.add(rank);
        }
    }

    private double sigmoid(double x) {
        return (1/( 1 + Math.pow(Math.E,(-1*x))));
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
            if (results.get(id) != null) {
                sr.add(results.get(id));
            }
        }
        return sr;
    }

    public int getNumResultsFound() {
        return ranks.size() + finalRanking.size();
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
    private class DBFetcherTask extends Thread {
        String word; CountDownLatch latch;
        public DBFetcherTask(String word, CountDownLatch latch) {
            this.word = word; this.latch = latch;
        }
        @Override
        public void run() {
            DB db = new DB("default");
            db.open(Credentials.jdbcDriver, Credentials.dbUrl, Credentials.dbUser, Credentials.dbUserPW);
            List<Keyword> keywords = Keyword.findBySQL("SELECT * FROM keywords WHERE word='" + word +"' ORDER BY wtf LIMIT 5000");
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
            db.close();
            latch.countDown();
        }
    }

    class ContentQueryThread extends Thread {
        String docId; CountDownLatch latch;
        public ContentQueryThread(String docId, CountDownLatch latch) { this.docId = docId; this.latch = latch; }
        @Override
        public void run() {
            Article article = null;
            String posContent = null;
            try {
                String url = docId + ".html";
                String content = S3Service.getInstance().getFileAsString(url);
                posContent = getContentString(content);
                Document doc = Jsoup.parse(content);
                article = ArticleExtractor.with("", doc).extractMetadata().extractContent().article();
            } catch (Exception e) {
                System.err.println(docId);
            }
            if (article == null || posContent == null) {
                System.err.println("[❌ERROR:] Article is NULL !!!");
                latch.countDown();
            } else {
                String title = article.title;
                String url = docToUrl.get(docId);
                String excerpt = getExerptFor(posContent, docId);
                results.put(docId, new SearchResult(title, url, excerpt));
                latch.countDown();
            }
        }
    }

    private String getExerptFor(String content, String docId) {
        int[] pair = positions.get(docId);
        if (pair == null || pair[0] > content.length()) {
            if (content.length() < 500) {
                return content;
            } else {
                return content.substring(0, 500);
            }
        } else {
            content = content.substring(pair[0], content.length());
            if (content.length() < 500) {
                return content;
            } else {
                return content.substring(0, 500);
            }
        }
    }

    private String getContentString(String content) {
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
            return "";
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

        String title = article.title;
        String description = article.description;
        return title + description + body;
    }

    private double getIDF(List<Keyword> keywords) {
        double total = 700000; double count = 0;
        if (keywords.size()  < 1) { return 0.01; }
        if (keywords.size() < 3000)  { count = keywords.size(); }
        else {
            Double last = 1 + keywords.get(keywords.size() - 1).getDouble("pagerank");
            int multiplier = Long.valueOf(Math.round(last)).intValue();
            count = 3000 * multiplier;
        }
        return Math.log(total / count);
    }

    private List<Integer> convertStringToHitList(String hitList) {
        List<Integer> list = JSON.parseObject(hitList, new TypeReference<List<Integer>>() {});
        return list;
    }
}
