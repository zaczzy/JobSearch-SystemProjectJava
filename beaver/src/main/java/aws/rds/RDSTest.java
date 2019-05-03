package aws.rds;

import model.Word;
import org.javalite.activejdbc.Base;

public class RDSTest {

    public static void main(String[] args) {
        Base.open(Credentials.jdbcDriver, Credentials.dbUrl, Credentials.dbUser, Credentials.dbUserPW);
        Word.createIt("word", "testWord",
                "docId", "testDoc",
                "hits", "[0]",
                "tf", 20);
        Base.close();
    }
}
