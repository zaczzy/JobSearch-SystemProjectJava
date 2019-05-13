import aws.rds.Credentials;
import aws.rds.Word;
import aws.s3.S3Service;
import com.chimbori.crux.articles.Article;
import com.chimbori.crux.articles.ArticleExtractor;
import org.javalite.activejdbc.Base;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class RealAgent {

    List<SearchResult> results;
    Map<String, Double> documentScores;
    Map<String, List<String>> documentMeta;

    public RealAgent(String query) {
        this.results = Collections.synchronizedList(new ArrayList<>());
        this.documentScores = Collections.synchronizedMap(new HashMap<>());
        this.documentMeta = Collections.synchronizedMap(new HashMap<>());

        /* Start Querying Services*/
        //Base.open(Credentials.jdbcDriver, Credentials.dbUrl, Credentials.dbUser, Credentials.dbUserPW);
        String[] queries = query.split(" ");
        searchFor(queries);
        //Base.close();

        /* Populate Results from Ranking */
        List<String> ranking = getTopK(20);
        addResultsFrom(ranking);
        System.out.println("☺️Query Completed.");
    }

    private void searchFor(String[] queries) {
        int size = queries.length;
        ExecutorService service = Executors.newFixedThreadPool(size);
        for (String q : queries) {
            WordQueryThread wordThread = new WordQueryThread(q);
            service.execute(wordThread);
        }
        try {
            service.awaitTermination(20, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        service.shutdown();

        System.out.println("SEARCH COMPLETED, number of documents found: " + Integer.toString(documentScores.size()));
    }

    private List<String> getTopK(int k) {
        return documentScores.entrySet().stream()
                .sorted(Comparator.comparing(Map.Entry::getValue, Comparator.reverseOrder()))
                .map(e -> e.getKey())
                .limit(k)
                .collect(Collectors.toList());
    }

    private void addResultFrom(String docId) {
        ContentQueryThread contentThread = new ContentQueryThread(docId);
        contentThread.start();
    }

    private void addResultsFrom(List<String> ranking) {
        int size = ranking.size();
        ExecutorService service = Executors.newFixedThreadPool(size);
        for(String doc : ranking) {
            ContentQueryThread contentThread = new ContentQueryThread(doc);
            service.execute(contentThread);
        }
        try {
            service.awaitTermination(20, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        service.shutdown();
        System.out.println("RESULTS ADDED, number of results: " + Integer.toString(results.size()));
    }

    private String getExerptFor(Article article) {
        String mainContent = article.description + article.document.text();
        if (mainContent.length() < 300) {
            return mainContent;
        } else {
            return mainContent.substring(0, 300);
        }
    }

    public List<SearchResult> getResults() {
        return results;
    }

    class WordQueryThread extends Thread {
        String q;
        public WordQueryThread(String q) {
            this.q = q;
        }

        public void run() {
            Base.open(Credentials.jdbcDriver, Credentials.dbUrl, Credentials.dbUser, Credentials.dbUserPW);
            String query = "word = '" + q + "'";
            List<Word> words = Word.where(query);
            for (Word word : words) {
                String docId = word.getString("docid");
                int tf = word.getInteger("tf");
                Double currScore = documentScores.getOrDefault(docId, 0.0);
                currScore += tf;
                documentScores.put(docId, currScore);

                /* document meta for testing */
                String hits = word.getString("hits");
                List<String> meta = new ArrayList<>();
                meta.add("word: " + q);
                meta.add("hits: " + hits);
                meta.add("tf: " + tf);
                List<String> data = documentMeta.getOrDefault(docId, new ArrayList<>());
                data.add(meta.toString() + "\n");
                documentMeta.put(docId, data);
            }
            Base.close();
            System.out.println("[✅: " + q + "]" + Integer.toString(words.size()));
            System.out.println(words.get(0).getString("docid"));
        }
    }

    class ContentQueryThread extends Thread {
        String docId;

        public ContentQueryThread(String docId) {
            this.docId = docId;
        }

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
            String url = "http://example.com/bed/ball.html";
            String excerpt = getExerptFor(article) + documentMeta.get(docId).toString();
            results.add(new SearchResult(title, url, excerpt));
        }
    }
}
