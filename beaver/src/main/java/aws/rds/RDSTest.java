package aws.rds;

import model.Word;
import model.WordEntry;
import org.javalite.activejdbc.Base;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

public class RDSTest {

    public static void main(String[] args) {
        Base.open(Credentials.jdbcDriver, Credentials.dbUrl, Credentials.dbUser, Credentials.dbUserPW);
        if (true ) {
            List<WordEntry> entries = new ArrayList<>();
            entries.add(new WordEntry("tester", "test doc id", "[0,9,35]", 0, 0, 2.3, 3.3));
            entries.add(new WordEntry("teste43", "test doc idid", "[0,2,35]", 0, 0, 2.9, 3.02));
            if (entries != null && entries.size() > 0) {
                try {
                    Base.openTransaction();
                    PreparedStatement ps = Base.startBatch(
                            "INSERT INTO words(word, docid, hits, tf, pagerank, wtf, normalizer) " +
                                    "VALUES(?, ?, ?, ?, ?, ?, ?)");
                    for (WordEntry w : entries) {
                        Base.addBatch(ps, w.getWord(), w.getDocid(), w.getHits(),
                                w.getTf(), w.getPagerank(), w.getWtf(), w.getNormalizer());
                    }
                    Base.executeBatch(ps);
                    ps.close();
                    Base.commitTransaction();
                    System.out.println("[ 💾 Saved: ] Saved for document " + entries.get(0).getDocid());
                } catch (Exception e) {
                    Base.rollbackTransaction();
                    System.out.println("[ ⛔️ Failed Transaction: ] Rolled back for " + entries.get(0).getDocid());
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
        Base.close();
    }
}
