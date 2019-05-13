package aws.s3;

import aws.dynamoDB.DynamoDBService;

import java.io.IOException;
import java.util.UUID;

public class S3Test {

    public static void main(String[] args) throws IOException {
        System.out.println("test");
        String content = "test string";
        String docID = UUID.randomUUID().toString();
        S3Service.getInstance().putFile("test2/" + docID + ".html", content);

        String[][] fields = new String[1][2];
        fields[0][0] = "url";
        fields[0][1] = "https://www.google.com";
        DynamoDBService.getInstance().put(docID, fields);


    }

}