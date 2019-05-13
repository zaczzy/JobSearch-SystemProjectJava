package aws.dynamodb;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import software.amazon.awssdk.services.dynamodb.paginators.ScanIterable;

import java.util.HashMap;
import java.util.Map;

import static aws.dynamodb.Host_DBService.getStringAttributeValueMap;


public class PR_DBService{
    private static PR_DBService instance;
    GetItemRequest getRequest;
    protected DynamoDbClient dbClient;



    public static PR_DBService getInstance() {
        if (instance == null) {
            instance = new PR_DBService();
        }
        return instance;
    }

    private PR_DBService() {
        AwsBasicCredentials awsCreds = AwsBasicCredentials.create(PR_Credentials.ACCESS_KEY, PR_Credentials.SECRET_KEY);
        AwsCredentialsProvider provider = StaticCredentialsProvider.create(awsCreds);
        this.dbClient = DynamoDbClient.builder().credentialsProvider(provider).region(PR_Credentials.REGION).build();
    }


    public Map<String, AttributeValue> get(String id) {
        HashMap<String,AttributeValue> key_to_get =
                new HashMap<>();
        key_to_get.put("ID", AttributeValue.builder()
                .s(id).build());
        GetItemRequest request = null;

        request = GetItemRequest.builder()
                .key(key_to_get)
                .tableName(PR_Credentials.TABLE_NAME)
                .build();

        return getStringAttributeValueMap(request, dbClient);
    }

    public void getHost2PRMap() {
        ScanRequest request =
                ScanRequest.builder()
                        .tableName(PR_Credentials.TABLE_NAME)
                        .build();
        ScanIterable response = dbClient.scanPaginator(request);
        Map<String, String> ret = new HashMap<>();
        for (ScanResponse page : response) {
            for (Map<String, AttributeValue> item : page.items()) {
                ret.put(item.get("ID").s(), item.get("PageRank").s());
            }
        }
        PageRankGetter.host2pr = ret;
    }

}