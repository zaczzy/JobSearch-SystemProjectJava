package dynamodb;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import software.amazon.awssdk.services.dynamodb.paginators.ScanIterable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class Host_DBService {
    private static Host_DBService instance;
    GetItemRequest getRequest;
    protected DynamoDbClient dbClient;



    public static Host_DBService getInstance() {
        if (instance == null) {
            instance = new Host_DBService();
        }
        return instance;
    }

    private Host_DBService() {
        AwsBasicCredentials awsCreds = AwsBasicCredentials.create(Host_Credentials.ACCESS_KEY, Host_Credentials.SECRET_KEY);
        AwsCredentialsProvider provider = StaticCredentialsProvider.create(awsCreds);
        this.dbClient = DynamoDbClient.builder().credentialsProvider(provider).region(Host_Credentials.REGION).build();
    }


    public Map<String, AttributeValue> get(String id) {
        HashMap<String,AttributeValue> key_to_get =
                new HashMap<>();
        key_to_get.put("ID", AttributeValue.builder()
                .s(id).build());
        GetItemRequest request = null;

        request = GetItemRequest.builder()
                .key(key_to_get)
                .tableName(Host_Credentials.TABLE_NAME)
                .build();

        return getStringAttributeValueMap(request, dbClient);
    }

    static Map<String, AttributeValue> getStringAttributeValueMap(GetItemRequest request, DynamoDbClient dbClient) {
        return dbClient.getItem(request).item();
    }

    public void getID2HostMap() {
        ScanRequest request =
                ScanRequest.builder()
                        .tableName(Host_Credentials.TABLE_NAME)
                        .build();
        ScanIterable response = dbClient.scanPaginator(request);
        Map<String, String> ret = new HashMap<>();
        Map<String, String> url = new HashMap<>();
        for (ScanResponse page : response) {
            for (Map<String, AttributeValue> item : page.items()) {
                if (item.get("isNew").s().equals("true")) {
                    ret.put(item.get("ID").s(), item.get("host").s());
                    url.put(item.get("ID").s(), item.get("url").s());
                }
            }
        }
        PageRankGetter.id2host = ret;
        PageRankGetter.id2url = url;
    }
}