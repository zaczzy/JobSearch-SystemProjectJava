package aws.dynamoDB;

import java.io.IOException;


public class DynamoTest {

    public static void main(String[] args) throws IOException {
      String[][] fields = new String[2][2];
      fields[0][0] = "url";
      fields[0][1] = "https://www.google.com";
      fields[1][0] = "last crawled time";
      fields[1][1] = "yesterday";
      DynamoDBService.getInstance().put("testID", fields);
      DynamoDBService.getInstance().get("testID");
    }

}
