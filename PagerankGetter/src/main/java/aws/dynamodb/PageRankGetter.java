package aws.dynamodb;

import java.util.HashMap;
import java.util.Map;

public class PageRankGetter {
    static Map<String, String> id2host;
    static Map<String, String> host2pr;
    static Map<String, String> id2url;
//    makes 2 DynamoDB requests, slower
    public static String get(String docID) {
        return PR_DBService.getInstance().get(Host_DBService.getInstance().get(docID).get("host").s()).get("PageRank").s();
    }

    public static Map<String, Double> getAll() throws InterruptedException {
        Map<String, Double> ret = new HashMap<>();
        Thread t1 = new Thread(() -> Host_DBService.getInstance().getID2HostMap());
        Thread t2 = new Thread(() -> PR_DBService.getInstance().getHost2PRMap());
        t1.start(); t2.start();
        t1.join(); t2.join();
        int i = 0;
        for (Map.Entry<String, String> ent: id2host.entrySet()) {
            try {
                ret.put(ent.getKey(), Double.parseDouble(host2pr.get(ent.getValue())));
            } catch (NullPointerException e) {
                i++;
                System.out.println(i + " ❌ Did not find ");
                System.out.println("docId:" + ent.getKey());
                System.out.println("docId:" + ent.getValue());
            }
        }
        System.out.println("[🌊: Total]" + ret.size());
        return ret;
    }

    public static Map<String, String> getUrl() {
        Map<String, String> ret = new HashMap<>();
        for (Map.Entry<String, String> ent : id2url.entrySet()) {
            ret.put(ent.getKey(), ent.getValue());
        }
        return ret;
    }

    public static void clean() {
        id2url = null;
        id2host = null;
        host2pr = null;
    }
}
