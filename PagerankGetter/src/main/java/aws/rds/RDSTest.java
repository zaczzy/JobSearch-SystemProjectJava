package aws.rds;

import Models.Keyword;
import Models.Word;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.DB;
import org.javalite.activejdbc.ModelListener;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

public class RDSTest {

    public static void main(String[] args) {
        DB local = new DB("keywords").open(Credentials.Local.jdbcDriver, Credentials.Local.dbUrl, Credentials.Local.dbUser, Credentials.Local.dbUserPW);
        if (true ) {
            List<Keyword> entries = new ArrayList<>();
            entries.add(new Keyword("word", "docid", "url", "hits", 3, 0.5, 23.53, 9.9));
            entries.add(new Keyword("wowerd", "docbeid", "uwegerl", "hisdvets", 4, 0.9, 23.953, 9.2));
            if (entries != null && entries.size() > 0) {
                try {
                    local.openTransaction();
                    PreparedStatement ps = local.startBatch(
                            "INSERT INTO keywords(word, docid, hits, tf, pagerank, wtf, normalizer, url) " +
                                    "VALUES(?, ?, ?, ?, ?, ?, ?, ?)");
                    for (Keyword w : entries) {
                        local.addBatch(ps, w.word, w.docId, w.hits,
                                w.tf, w.pagerank, w.wtf, w.normalizer, w.url);
                    }
                    local.executeBatch(ps);
                    ps.close();
                    local.commitTransaction();
                    System.out.println("[ 💾 Saved: ] Saved ");
                } catch (Exception e) {
                    local.rollbackTransaction();
                    System.out.println("[ ⛔️ Failed Transaction: ] Rolled back");
                    e.printStackTrace();
                }
            }
        } else {
            Word.createIt("word", "testWord",
                    "docId", "fucking",
                    "hits", "[0]",
                    "tf", 20,
                    "pagerank", 1000);
        }
        local.close();
    }
}
