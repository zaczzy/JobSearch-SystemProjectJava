package spout;

import aws.s3.Credentials;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.paginators.ListObjectsV2Iterable;

import java.lang.Thread;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class Loader extends Thread {

    private Queue<String> Q;
    private String folderName;
    private S3Client client;
    private String bucketName;

    public Loader(String folderName, Queue Q) {
        this.folderName = folderName;
        this.Q = Q;
        AwsBasicCredentials awsCreds = AwsBasicCredentials.create(Credentials.ACCESS_KEY, Credentials.SECRET_KEY);
        AwsCredentialsProvider provider = StaticCredentialsProvider.create(awsCreds);
        this.client = S3Client.builder().credentialsProvider(provider).region(Credentials.REGION).build();
        this.bucketName = Credentials.BUCKET_NAME;
    }

    public void run() {
        /* Request */
        ListObjectsV2Request listReq = ListObjectsV2Request.builder()
                .bucket(bucketName).prefix(folderName)
                .maxKeys(1)
                .build();
        /* Paginated Response as Iterable */
        ListObjectsV2Iterable listRes = client.listObjectsV2Paginator(listReq);
        /* Retrieve Results*/
        listRes.contents().stream().forEach(content -> {
            if (content.size() > 0) { Q.add(content.key()); }
        });
    }
}
