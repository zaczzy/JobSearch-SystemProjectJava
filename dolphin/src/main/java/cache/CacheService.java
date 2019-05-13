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

  private final String keywordCacheFile = "./cache/keyword/keywordCache.txt";
  private final String queryCacheFile = "./cache/query/queryCache.txt";



  private CacheService() {
    queryJSONMap = loadQueryMap();
    queryWordMap = loadKWDMap();
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
      saveString(keywordCacheFile, JSON.toJSONString(queryWordMap));
    } catch (Exception ignored) {
    }
  }

  public List<WordQryResult> readKWDCache(String keyword) {
    return queryWordMap.getOrDefault(keyword, null);
  }


  public void writeQueryCache(String query, String queryJSON) {
    queryJSONMap.put(query, queryJSON);
    try {
      saveString(queryCacheFile, JSON.toJSONString(queryJSONMap));
    } catch (Exception ignored) {
    }
  }

  public String readQueryCache(String query) {
    return queryJSONMap.getOrDefault(query, null);
  }

  private Map<String, List<WordQryResult>> loadKWDMap() {
    try {
      String mapJson = loadString(keywordCacheFile);
      JSONObject jsonObject = JSONObject.parseObject(mapJson);
      Map<String, Object> map = (Map<String, Object>) jsonObject;
      if (map == null) { map = new HashMap<>(); }
      Map<String, List<WordQryResult>> result = new HashMap<>();
      for (Map.Entry<String, Object> entry : map.entrySet()) {
        result.put(entry.getKey(), convertStringToWordQryResultList(String.valueOf(entry.getValue())));
      }
      return result;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  private Map<String, String> loadQueryMap() {
    try {
      String mapJson = loadString(queryCacheFile);
      JSONObject jsonObject = JSONObject.parseObject(mapJson);
      Map<String, Object> map = (Map<String, Object>) jsonObject;
      if (map == null) { map = new HashMap<>(); }
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

  private void saveString(String fileName, String data) throws Exception {
    BufferedWriter out = new BufferedWriter(new FileWriter(fileName));
    out.write(data);
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
