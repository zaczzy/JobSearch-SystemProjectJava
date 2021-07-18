package com.apis;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import static spark.Spark.*;

public class APIServer {
    private Queue<String> globalQueue = new ConcurrentLinkedQueue<>();

    public static void main(String[] args) {
        APIServer server = new APIServer();
        port(8083);
        CorsFilter.apply();
//        http://localhost:8080/?query=something
        get("/", (req, res) -> {
            String query = req.queryParams("q");
            res.status(200);
            res.type("application/json");
            if (checkWeather(query)) {
                String ret = getWeather(weatherProcess(query));
                if (ret == null) return noMatchJSON();
                return ret;
            } else if (checkWalmart(query)) {
                String ret = getWalmart(walmartProcess(query));
                if (ret == null) return noMatchJSON();
                return ret;
            } else {
                return noMatchJSON();
            }
        });
//        POST http:localhost:8080/h/:history
        post("/h/:history", (req, res) -> {
            String hist = req.params(":history");
            server.globalQueue.add(hist);
            res.status(200);
            return "";
        });
//        GET http:localhost:8080/h/:partial
        get("/h/:partial", (req, res) -> {
            String[] t = stemmed(req.params(":partial"));
            JSONArray ja = new JSONArray();
            int size = server.globalQueue.size();
            for (int i = 0; i < size; i += 1) {
                String s = server.globalQueue.poll();
                if (s != null && isSubsequence(stemmed(s), t)) {
                    ja.put(s);
                }
                server.globalQueue.add(s);
            }
            res.status(200);
            res.type("application/json");
            return ja.toString();
        });
    }

    private static String[] stemmed(String part) {
        return part.toLowerCase().split("\\s+");
    }

    public static boolean isSubsequence(String[] full, String[] target) {
        int f = 0, t = 0;
        while (f < full.length) {
            while (f < full.length && !full[f].startsWith(target[t])) {
            f += 1;
        }
            if (f == full.length) return false;
            f += 1;
            t += 1;
            if (t == target.length) {
                return true;
            }
        }



        return false;
    }


    private static boolean containsChecker(String query, List<String> asList) {
        String[] tokens = query.toLowerCase().split("\\s+");
        Set<String> hitWords = new HashSet<>(asList);
        for (String token : tokens) {
            if (hitWords.contains(token)) {
                return true;
            }
        }
        return false;
    }

    private static String containsProcess(String query, List<String> asList) {
        query = query.toLowerCase();
        String[] tokens = query.split("\\s+");
        Set<String> hitWords = new HashSet<>(asList);
        StringBuilder bd = new StringBuilder();
        for (String token : tokens) {
            if (!hitWords.contains(token)) {
                bd.append(token).append(" ");
            }
        }
        return bd.toString().trim();
    }

    private static boolean checkWeather(String query) {
        return containsChecker(query, Arrays.asList("weather", "rain", "temperature", "wind"));
    }

    private static String weatherProcess(String query) {
        return containsProcess(query, Arrays.asList("weather", "rain", "temperature", "wind"));
    }

    private static boolean checkWalmart(String query) {
        return containsChecker(query, Arrays.asList("buy", "seller", "purchase", "shop"));
    }

    private static String walmartProcess(String query) {
        return containsProcess(query, Arrays.asList("buy", "seller", "purchase", "shop"));
    }

    private static String noMatchJSON() {
        JSONObject jo = new JSONObject();
        jo.put("service", false);
        return jo.toString();
    }

    public static String getWeather(String query) {
        System.out.println(query);
        URL url;
        try {
            url = new URL("http://api.openweathermap.org/data/2.5/weather?appid=REMOVED" + query);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
        try {
            URLConnection con = url.openConnection();
            InputStream in = con.getInputStream();
            String encoding = con.getContentEncoding();
            encoding = encoding == null ? "UTF-8" : encoding;
            String body = IOUtils.toString(in, encoding);
            JSONObject jo = new JSONObject(body);
            JSONObject data = new JSONObject();
            JSONObject ret = new JSONObject();

            data.put("service", true);
            data.put("service_type", "weather");
            data.put("data", ret);
            ret.put("city", jo.getString("name"));
            ret.put("dt", formatTime(jo.getLong("dt")));
            JSONObject firstWeather = jo.getJSONArray("weather").getJSONObject(0);
            ret.put("weather", firstWeather.getString("main"));
            ret.put("icon_id", firstWeather.getInt("id"));
            JSONObject mainTemps = jo.getJSONObject("main");

            ret.put("temp_min", mainTemps.getDouble("temp_min"));
            ret.put("temp_max", mainTemps.getDouble("temp_max"));
            ret.put("temp", mainTemps.getDouble("temp"));
            ret.put("humidity", mainTemps.getInt("humidity"));
            ret.put("wind_kmh", jo.getJSONObject("wind").getDouble("speed") * 3.6);
            ret.put("wind_mph", jo.getJSONObject("wind").getDouble("speed") * 2.23694);
            return data.toString();
        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getWalmart(String query) {
        System.out.println(query);
        try {
            URL url = new URL("http://api.walmartlabs.com/v1/search?apiKey=**Removed**" + query);
            URLConnection con = url.openConnection();
            InputStream in = con.getInputStream();
            String encoding = con.getContentEncoding();
            encoding = encoding == null ? "UTF-8" : encoding;
            String body = null;
            body = IOUtils.toString(in, encoding);
            JSONObject jo = new JSONObject(body);
            if (jo.getInt("numItems") == 0 || jo.optString("message").equals("") && jo.optString("message").contains("not found")) {
                return null;
            }
            JSONObject ret = new JSONObject();
            ret.put("service", true);
            ret.put("service_type", "walmart");
            JSONArray data = new JSONArray();
            JSONArray src = jo.getJSONArray("items");
            for (int i = 0; i < jo.getInt("numItems"); i++) {
                JSONObject item = new JSONObject();
                JSONObject src_item = src.getJSONObject(i);
                item.put("seller", src_item.optString("sellerInfo"));
                item.put("productUrl", src_item.optString("productURL"));
                item.put("title", src_item.getString("name"));
                item.put("price", src_item.getDouble("salePrice"));
                item.put("thumbnailUrl", src_item.getString("thumbnailImage"));
                data.put(item);
            }
            ret.put("data", data);
            return ret.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String formatTime(long unixSecs) {
        Date date = new java.util.Date(unixSecs * 1000L);
        SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(java.util.TimeZone.getDefault());
        return sdf.format(date);
    }
}