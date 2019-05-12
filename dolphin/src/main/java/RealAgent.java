import aws.rds.Credentials;
import aws.rds.Word;
import aws.s3.S3Service;
import com.chimbori.crux.articles.Article;
import com.chimbori.crux.articles.ArticleExtractor;
import org.javalite.activejdbc.Base;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.*;
import java.util.stream.Collectors;

public class RealAgent {

    List<SearchResult> results;
    Map<String, Double> documentScores;
    Map<String, List<String>> documentMeta;

    public RealAgent(String query) {
        this.results = new ArrayList<>();
        this.documentScores = new HashMap<>();
        this.documentMeta = new HashMap<>();

        /* Start Querying Services*/
        Base.open(Credentials.jdbcDriver, Credentials.dbUrl, Credentials.dbUser, Credentials.dbUserPW);
        String[] queries = query.split(" ");
        searchFor(queries);
        Base.close();

        /* Populate Results from Ranking */
        List<String> ranking = getTopK(20);
        for (String doc : ranking) {
            try {
                addResultFrom(doc);
            } catch (Exception e) {
                System.out.println("[🧨 ERROR: ] Failed to add result");
                e.printStackTrace();
            }
        }
    }

    private void searchFor(String[] queries) {
        for (String q : queries) {
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
        }
    }

    private List<String> getTopK(int k) {
        return documentScores.entrySet().stream()
                .sorted(Comparator.comparing(Map.Entry::getValue, Comparator.reverseOrder()))
                .map(e -> e.getKey())
                .limit(k)
                .collect(Collectors.toList());
    }

    private void addResultFrom(String docId) {
        Article article = null;
        try {
            String url = docId + ".html";
            String content = S3Service.getInstance().getFileAsString(url);
            Document doc = Jsoup.parse(content);
            article = ArticleExtractor.with("", doc).extractMetadata().extractContent().article();
        } catch (Exception e) {
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
}
