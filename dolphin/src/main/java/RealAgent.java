import aws.rds.Credentials;
import aws.rds.Word;
import org.javalite.activejdbc.Base;

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
            results.add(getResultFrom(doc));
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

    private SearchResult getResultFrom(String docId) {
        String title = docId;
        String url = "http://example.com/bed/ball.html";
        String excerpt = documentMeta.get(docId).toString();
        results.add(new SearchResult(title, url, excerpt));
    }

    public List<SearchResult> getResults() {
        return results;
    }
}
