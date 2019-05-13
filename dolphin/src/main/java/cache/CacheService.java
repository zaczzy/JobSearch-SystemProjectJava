package cache;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;


public class CacheService {

  private static Map<String, List<WordQryResult>> queryWordMap;

  private static Map<String, String> queryJSONMap;

  private final String keywordCacheDir = "./cache/keyword/";
  private final String queryCacheDir = "./cache/query/";

  private CacheService() {
    queryJSONMap = loadQueryMap();
    queryWordMap = loadKWDMap();
    File cacheDir = new File("./cache");
    cacheDir.mkdir();
    File kwdDir = new File("./cache/keyword");
    kwdDir.mkdir();
    File queryDir = new File("./cache/query");
    queryDir.mkdir();
  }

  private static CacheService instance;

  public static CacheService getInstance() {
    if (instance ==null) {
      instance = new CacheService();
    }
    return instance;
  }

  public void writeKWDCache(String keyword, List<WordQryResult> qryResultList) {
    queryWordMap.put(keyword, qryResultList);
    try {
      saveString(keywordCacheDir + keyword, JSON.toJSONString(qryResultList));
    } catch (Exception ignored) {
    }
  }

  public List<WordQryResult> readKWDCache(String keyword) {
    return queryWordMap.getOrDefault(keyword, null);
  }


  public void writeQueryCache(String query, String queryJSON) {
    queryJSONMap.put(query, queryJSON);
    try {
      saveString(queryCacheDir + query, queryJSON);
    } catch (Exception ignored) {
    }
  }

  public String readQueryCache(String query) {
    return queryJSONMap.getOrDefault(query, null);
  }

  private Map<String, List<WordQryResult>> loadKWDMap() {
    try {
      Map<String, List<WordQryResult>> result = new HashMap<>();
      File folder = new File(keywordCacheDir);
      File[] listOfFiles = folder.listFiles();
      if (listOfFiles == null) return result;
      for (File file : listOfFiles) {
        result.put(file.getName(), convertStringToWordQryResultList(loadString(file.getName())));
      }
      return result;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  private Map<String, String> loadQueryMap() {
    try {
      Map<String, String> result = new HashMap<>();
      File folder = new File(queryCacheDir);
      File[] listOfFiles = folder.listFiles();
      if (listOfFiles == null) return result;
      for (File file : listOfFiles) {
        result.put(file.getName(), loadString(file.getName()));
      }
      return result;
    } catch (Exception e) {
      return new HashMap<>();
    }
  }

  private void saveString(String fileName, String data) throws Exception {
    BufferedWriter out = new BufferedWriter(new FileWriter(fileName));
    out.write(data);
    out.close();
  }

  private String loadString(String fileName) throws Exception {
    File file = new File(fileName);
    file.createNewFile();
    return new String (Files.readAllBytes( Paths.get(fileName) ) );
  }

  private List<WordQryResult> convertStringToWordQryResultList(String queryResult) {
    return JSON.parseObject(queryResult, new TypeReference<List<WordQryResult>>() {});
  }
}
