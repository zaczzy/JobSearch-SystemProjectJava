import cache.CacheService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import java.util.List;

import static spark.Spark.get;
import static spark.Spark.port;

public class Main {

    public static void main(String[] args) {
        port(8085);
        CorsFilter.apply();

        CacheService.getInstance();

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

        // The following code is used with cache
        get("advanced", (req, res) -> {
            String query = req.queryParams("query");
            if (CacheService.getInstance().readQueryCache(query) != null) {
                res.type("application/json");
                JSONObject jobj = new JSONObject();
                List<SearchResult> loadedResults = JSON.parseObject(CacheService.getInstance().readQueryCache(query), new TypeReference<List<SearchResult>>(){});
                jobj.put("results", loadedResults);
                jobj.put("total", 1000);
                return jobj;
            }
            System.out.println("advanced" + query);
            try {
                res.type("application/json");
                AdvancedAgent agent = new AdvancedAgent(query);
                JSONObject jobj = new JSONObject();
                jobj.put("results", agent.getResults());
                jobj.put("total", agent.getNumResultsFound());
                CacheService.getInstance().writeQueryCache(query, JSON.toJSONString(agent.getResults()));
                return jobj;
            } catch (Exception e) {
                e.printStackTrace();
                return "Fucked up";
            }
        });



    }

}
