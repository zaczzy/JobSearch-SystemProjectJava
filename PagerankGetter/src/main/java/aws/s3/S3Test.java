package aws.s3;

import java.io.IOException;
import java.util.List;

public class S3Test {

    public static void main(String[] args) throws IOException {
        List<String> list  = S3Service.getInstance().listAllFiles("documents/1/");
        for (String s : list) {
            String content = S3Service.getInstance().getFileAsString(s);
            System.out.println("===== File of length " + content.length() + "=====");
            System.out.println(content.substring(0, 300));
        }
    }

}