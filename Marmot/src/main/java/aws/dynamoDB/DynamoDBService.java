package aws.dynamoDB;


import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.HashMap;
import java.util.Map;


public class DynamoDBService {
  private static DynamoDBService instance;
  private DynamoDbClient dbClient;



  public static DynamoDBService getInstance() {
    if (instance == null) {
      instance = new DynamoDBService();
    }
    return instance;
  }

  private DynamoDBService() {
    AwsBasicCredentials awsCreds = AwsBasicCredentials.create(Credentials.ACCESS_KEY, Credentials.SECRET_KEY);
    AwsCredentialsProvider provider = StaticCredentialsProvider.create(awsCreds);
    this.dbClient = DynamoDbClient.builder().credentialsProvider(provider).region(Credentials.REGION).build();

  }

  /*
    Put an item to Document database, with primary key, and a two-dimension string arr fields.
   */
  public void put(String id, String[][] fields) {
      HashMap<String,AttributeValue> item_values = new HashMap<>();
      item_values.put("ID", AttributeValue.builder().s(id).build());
      for (String[] field : fields) {
        item_values.put(field[0], AttributeValue.builder().s(field[1]).build());
      }
      PutItemRequest putRequest = PutItemRequest.builder().tableName(Credentials.TABLE_NAME).item(item_values).build();
      dbClient.putItem(putRequest);
  }

  public Map<String, AttributeValue> get(String id) {
    HashMap<String,AttributeValue> key_to_get =
            new HashMap<>();
    key_to_get.put("ID", AttributeValue.builder()
            .s(id).build());
    GetItemRequest request = null;

    request = GetItemRequest.builder()
            .key(key_to_get)
            .tableName(Credentials.TABLE_NAME)
            .build();
    return dbClient.getItem(request).item();
  }

  public void update(String id, String[][] fields) {
    HashMap<String,AttributeValue> item_key =
            new HashMap<>();
    item_key.put("ID", AttributeValue.builder()
            .s(id).build());

    HashMap<String, AttributeValueUpdate> item_values = new HashMap<>();
    for (String[] field : fields) {
      item_values.put(field[0], AttributeValueUpdate.builder()
              .value(AttributeValue.builder().s(field[1]).build())
              .action(AttributeAction.PUT)
              .build());
    }
    UpdateItemRequest updateRequest = UpdateItemRequest.builder().tableName(Credentials.TABLE_NAME).key(item_key).attributeUpdates(item_values).build();
    dbClient.updateItem(updateRequest);
  }
}
