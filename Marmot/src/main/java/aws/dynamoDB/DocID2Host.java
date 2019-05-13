package aws.dynamoDB;

import java.io.*;

public class DocID2Host {
  public static void main(String[] args) throws IOException {
    String[] fileList = {"out_doc_0", "out_doc_1", "out_doc_2", "out_doc_3", "out_doc_4"};
    int index = 0;
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
        String newLine = DynamoDBService.getInstance().get(splitArr[0]).get("host").s() + "\t" + DynamoDBService.getInstance().get(splitArr[1]).get("host").s() + "\n";
        fw.write(newLine);
      }
      fr.close();
      fw.close();
    }
  }
}
