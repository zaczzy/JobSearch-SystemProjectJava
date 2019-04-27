package aws.s3;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.paginators.ListObjectsV2Iterable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class S3Service {

    /************************************
     *  Singleton Instance of S3Service *
     */
    private static S3Service instance;

    public static S3Service getInstance() {
        if (instance == null) {
            instance = new S3Service();
        }
        return instance;
    }

    /**
     * Private Fields
     */
    private S3Client client;
    private String bucketName;

    /**
     *  Configure Instance
     */
    private S3Service() {
        AwsBasicCredentials awsCreds = AwsBasicCredentials.create(Credentials.ACCESS_KEY, Credentials.SECRET_KEY);
        AwsCredentialsProvider provider = StaticCredentialsProvider.create(awsCreds);
        this.client = S3Client.builder().credentialsProvider(provider).region(Credentials.REGION).build();
        this.bucketName = Credentials.BUCKET_NAME;
    }

    /******************
     * PUBLIC METHODS *
     */

    /**
     * @param folderName -- name of the s3 directory
     * @return a list of non-empty files in the folder
     */
    public List<String> listAllFiles(String folderName) {
        List<String> collection = new ArrayList<String>();
        /* Request */
        ListObjectsV2Request listReq = ListObjectsV2Request.builder()
                .bucket(bucketName).prefix(folderName)
                .maxKeys(1)
                .build();
        /* Paginated Response as Iterable */
        ListObjectsV2Iterable listRes = client.listObjectsV2Paginator(listReq);
        /* Retrieve Results*/
        listRes.contents().stream().forEach(content -> {
            if (content.size() > 0) { collection.add(content.key()); }
        });
        return collection;
    }

    /**
     *
     * @param fileName -- name of the file
     * @return a string of the file content.
     * @throws IOException -- If the file does not exist.
     */
    public String getFileAsString(String fileName) throws IOException {
        GetObjectRequest getReq = GetObjectRequest.builder().bucket(bucketName).key(fileName).build();
        InputStream response = client.getObject(getReq);
        return getTextFromStream(response);
    }

    /*******************
     * PRIVATE METHODS *
     */

    private String getTextFromStream(InputStream input) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line + "\n");
        }
        input.close();
        return sb.toString();
    }



}
