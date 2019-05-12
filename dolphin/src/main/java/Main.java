import com.alibaba.fastjson.JSON;

import static spark.Spark.get;
import static spark.Spark.port;

public class Main {

    public static void main(String[] args) {
        port(8085);
        CorsFilter.apply();

        /* Fake Results for testing Peacock Frontend*/
        get("fake", (req, res) -> {
            String query = req.queryParams("query");
            res.type("application/json");
            FakeAgent agent = new FakeAgent(query);
            return JSON.toJSONString(agent.getResults());
        });

        /* Real Query Endpoint */
        get("real", (req, res) -> {
            String query = req.queryParams("query");
            System.out.println(query);
            try {
                res.type("application/json");
                RealAgent agent = new RealAgent(query);
                return JSON.toJSONString(agent.getResults());
            } catch (Exception e) {
                e.printStackTrace();
                return "Fucked up";
            }
        });



    }

}
