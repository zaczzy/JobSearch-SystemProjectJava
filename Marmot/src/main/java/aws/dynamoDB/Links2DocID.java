package aws.dynamoDB;

import java.io.*;
import java.util.Map;

public class Links2DocID {
  public static void main(String[] args) throws IOException {
    Map<String, String> ret = DynamoDBService.getInstance().getAllURLS("test2");
    String[] fileList = {"out_links_0", "out_links_1", "out_links_2", "out_links_3", "out_links_4"};
    int index = 0;
    for (String fileName : fileList) {
      System.out.println(index);
      String outputFileName = "out_doc_" + index++;
      File file = new File("./links/" + outputFileName);
      FileWriter fw = new FileWriter(file, true);
      FileReader fr = new FileReader(new File("./links/" + fileName));
      BufferedReader bufferedReader = new BufferedReader(fr);
      String line;
      while ((line = bufferedReader.readLine()) != null) {
        String[] splitArr = new String[2];
        if (line.contains("\t")) {
          splitArr = line.split("\t");
        } else if (line.contains(" ") && line.indexOf(" ") != line.length() - 1) {
          splitArr = line.split(" ");
        }
        if (splitArr.length == 2 && ret.containsKey(splitArr[0]) && ret.containsKey(splitArr[1])) {
          String newLine = ret.get(splitArr[0]) + "\t" + ret.get(splitArr[1]) + "\n";
          fw.write(newLine);
        }
      }
      fr.close();
      fw.close();
    }
  }
}
