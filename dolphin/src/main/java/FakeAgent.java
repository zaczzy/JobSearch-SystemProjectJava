import com.thedeanda.lorem.Lorem;
import com.thedeanda.lorem.LoremIpsum;

import java.util.ArrayList;
import java.util.List;

public class FakeAgent {

    List<SearchResult> results;

    public FakeAgent(String query) {
        Lorem lorem = LoremIpsum.getInstance();
        results = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            String title = query + " " + lorem.getTitle(15,20);
            String url = "http://example.com/bed/ball.html";
            String excerpt = lorem.getWords(10, 15) + " " + query + " " + " " +  lorem.getWords(20, 30);
            results.add(new SearchResult(title, url, excerpt));
        }
    }

    public List<SearchResult> getResults() {
        return results;
    }
}
