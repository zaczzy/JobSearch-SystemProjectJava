package aws.rds;

public class Credentials {

    public static class Remote {
        public static final String jdbcDriver = "org.postgresql.Driver";
        public static final String dbUrl = "jdbc:postgresql://beaver-prod.cqd97et2fvfy.us-east-1.rds.amazonaws.com/BeaverProduction";
        public static final String dbUser = "BeaverProd";
        public static final String dbUserPW = "UPenn2020";
    }

    public static class Local {
        public static final String jdbcDriver = "org.postgresql.Driver";
        public static final String dbUrl = "jdbc:postgresql://127.0.0.1:5432/beaver";
        public static final String dbUser = "zhileiz";
        public static final String dbUserPW = "penTom32";
    }

}
