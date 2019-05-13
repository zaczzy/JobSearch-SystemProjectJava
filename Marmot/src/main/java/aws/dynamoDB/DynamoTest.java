package aws.dynamoDB;

import java.io.*;
import java.util.Map;


public class DynamoTest {

    public static void main(String[] args) throws IOException {
//      String[][] fields = new String[2][2];
//      fields[0][0] = "url";
//      fields[0][1] = "https://www.google.com";
//      fields[1][0] = "last crawled time";
//      fields[1][1] = "yesterday";
//      DynamoDBService.getInstance().put("testID", fields);
//      DynamoDBService.getInstance().get("testID");
//      fields = new String[1][2];
//      fields[0][0] = "url";
//      fields[0][1] = "https://www.yahoo.com";
//      DynamoDBService.getInstance().update("testID", fields);
//      DynamoDBService.getInstance().get("testID");

      Map<String, String> map = DynamoDBService.getInstance().getID2HostMap();
      String[] fileList = {"out_doc_5"};
      int index = 5;
      for (String fileName : fileList) {
        System.out.println(index);
        String outputFileName = "out_host_" + index++;
        File file = new File("./links/" + outputFileName);
        FileWriter fw = new FileWriter(file, true);
        FileReader fr = new FileReader(new File("./links/" + fileName));
        BufferedReader bufferedReader = new BufferedReader(fr);
        String line;
        while ((line = bufferedReader.readLine()) != null) {
          String[] splitArr = new String[2];
          if (line.contains("\t")) {
            splitArr = line.split("\t");
          }
          String newLine = map.get(splitArr[0]) + "\t" + map.get(splitArr[1]) + "\n";
          fw.write(newLine);
        }
        fr.close();
        fw.close();
      ` `
    }
}
