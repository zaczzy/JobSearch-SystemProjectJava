import com.alibaba.fastjson.JSON;

import static spark.Spark.get;
import static spark.Spark.port;

public class Main {

    public static void main(String[] args) {
        port(8089);
        CorsFilter.apply();
        /* Fake Results for testing Peacock Frontend*/
        get("fake", (req, res) -> {
            String query = req.queryParams("query");
            res.type("application/json");
            FakeAgent agent = new FakeAgent(query);
            return JSON.toJSONString(agent.getResults());
        });


    }

}
