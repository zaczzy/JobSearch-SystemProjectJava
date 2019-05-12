package bolt;

import aws.dynamodb.PageRankGetter;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.storm.shade.org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class PageRankConsultant {
    private Map<String, Double> pageRank;
    private Map<String, String> idToURL;

    private static PageRankConsultant instance;

    public static PageRankConsultant getInstance() {
        if (instance == null) {
            try {
                instance = new PageRankConsultant();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return instance;
    }

    private PageRankConsultant() throws Exception {
        Map<String, Double> cachedPageRank = loadPageRank();
        Map<String, String> cachedUrls = loadURLs();
        if (cachedPageRank == null || cachedUrls == null || cachedPageRank.size() < 700000 || cachedUrls.size() < 700000) {
            this.pageRank = PageRankGetter.getAll();
            this.idToURL = PageRankGetter.getUrl();
            savePageRank(pageRank);
            saveURLs(idToURL);
            PageRankGetter.clean();
        } else {
            this.pageRank = cachedPageRank;
            this.idToURL = cachedUrls;
            System.out.println("[🌊: Total Pageranks]" + pageRank.size());
            System.out.println("[🌊: Total Urls]" + idToURL.size());
        }
    }

    private void savePageRank(Map<String, Double> map) {
        String jsonString = JSON.toJSONString(map);
        try {
            saveString("pagerank.txt", jsonString);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Map<String, Double> loadPageRank() {
        try {
            String mapJson = loadString("pagerank.txt");
            JSONObject jsonObject = JSONObject.parseObject(mapJson);
            Map<String, Object> map = (Map<String, Object>) jsonObject;
            Map<String, Double> result = new HashMap<>();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                result.put(entry.getKey(), Double.parseDouble(String.valueOf(entry.getValue())));
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void saveURLs(Map<String, String> map) {
        String jsonString = JSON.toJSONString(map);
        try {
            saveString("urls.txt", jsonString);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Map<String, String> loadURLs() {
        try {
            String mapJson = loadString("urls.txt");
            JSONObject jsonObject = JSONObject.parseObject(mapJson);
            Map<String, Object> map = (Map<String, Object>) jsonObject;
            Map<String, String> result = new HashMap<>();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                result.put(entry.getKey(), String.valueOf(entry.getValue()));
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void saveString(String filenName, String data) throws Exception {
        FileUtils.writeStringToFile(new File(filenName), data);
    }

    private String loadString(String fileName) throws Exception {
        File file = new File(fileName);
        file.createNewFile();
        return FileUtils.readFileToString(file);
    }

    public double getRankOrDefault(String docId, double def) {
        try {
            Double result = pageRank.get(getTrueDocName(docId));
            return result.doubleValue();
        } catch (Exception e) {
//            System.out.println("[🤷: No Page Rank ] for: " + docId + " due to " + e.getMessage());
            return def;
        }
    }

    private String getTrueDocName(String docId) {
        String[] parts = docId.split("/");
        String name = parts[parts.length - 1].split("\\.")[0];
        return name;
    }

    public String getUrlOf(String docId) {
        return idToURL.get(getTrueDocName(docId));
    }

}
