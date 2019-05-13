package aws.rds;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.ModelListener;

public class RDSTest {

    public static void main(String[] args) {
        Base.open(Credentials.jdbcDriver, Credentials.dbUrl, Credentials.dbUser, Credentials.dbUserPW);
        Keyword.find("word='uber'", new ModelListener<Keyword>() {
            @Override
            public void onModel(Keyword word) {
                System.out.println(word);
            }
        });
        Base.close();
    }
}
