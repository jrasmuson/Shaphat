package models

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable

//Even though this is just the request it has the same keys as the Result so we can use it to check dynamo for everything not already loaded
@DynamoDBTable(tableName = "ModerationResult")
data class AlreadyFinishedRequest(
        @get:DynamoDBHashKey(attributeName = "hostname")
        var hostname: String? = null,
        @get:DynamoDBRangeKey(attributeName = "imageUrl")
        var imageUrl: String? = null)