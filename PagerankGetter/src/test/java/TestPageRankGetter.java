import aws.dynamodb.PageRankGetter;
import org.junit.Test;

public class TestPageRankGetter {
    @Test
    public void test() {
        PageRankGetter.get("01bd1190-0975-4061-998c-0bc46bc576b9");
    }

    @Test
    public void testAll() throws InterruptedException {
        PageRankGetter.getAll();
    }
}
