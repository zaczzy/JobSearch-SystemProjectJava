package aws.s3;

import java.io.IOException;
import java.util.List;

public class S3Test {

    public static void main(String[] args) throws IOException {
//        List<String> list  = S3Service.getInstance().listAllFiles("documents_small_batch/1/");
//        for (String s : list) {
//            String content = S3Service.getInstance().getFileAsString(s);
//            System.out.println("===== File of length " + content.length() + "=====");
//            System.out.println(content.substring(0, 300));
//        }
        String file = S3Service.getInstance().getFileAsString("documents_small_batch/0/0002ce21-65d4-4811-8957-7cecf3e46f4f.html");
        System.out.println("===== File of length " + file.length() + "=====");
    }

}