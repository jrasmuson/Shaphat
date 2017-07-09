import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.Table
import com.amazonaws.services.dynamodbv2.model.*

object DynamoUtils{
    val tableName = "ModerationResultTest"
    fun createNewTable(amazonDynamoDB: AmazonDynamoDB?): Table {
        val dynamoClient = DynamoDB(amazonDynamoDB)

        return dynamoClient.createTable(
                CreateTableRequest(tableName,
                        listOf(KeySchemaElement("hostname", KeyType.HASH),
                                KeySchemaElement("imageUrl", KeyType.RANGE)))
                        .withAttributeDefinitions(
                                listOf(AttributeDefinition("hostname", ScalarAttributeType.S),
                                        AttributeDefinition("imageUrl", ScalarAttributeType.S)))
                        .withProvisionedThroughput(ProvisionedThroughput(1L, 1L)))
    }
}