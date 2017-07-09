package models

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable
import java.time.LocalDateTime


@DynamoDBTable(tableName = "ModerationResult")
data class ModerationResult(
        @get:DynamoDBHashKey(attributeName = "hostname")
        val hostname: String,
        @get:DynamoDBRangeKey(attributeName = "imageUrl")
        val imageUrl: String,
        @get:DynamoDBAttribute
        val hasLabels: Int = 0,
        @get:DynamoDBAttribute
        val hasError: Int = 0,
        @get:DynamoDBAttribute
        val errorMessage: String? = null,
        @get:DynamoDBAttribute
        val createTime: String = LocalDateTime.now().toString(),
        @get:DynamoDBAttribute
        val explicitNudity: Float? = null,
        @get:DynamoDBAttribute
        val graphicMaleNudity: Float? = null,
        @get:DynamoDBAttribute
        val graphicFemaleNudity: Float? = null,
        @get:DynamoDBAttribute
        val sexualActivity: Float? = null,
        @get:DynamoDBAttribute
        val partialNudity: Float? = null,
        @get:DynamoDBAttribute
        val suggestive: Float? = null,
        @get:DynamoDBAttribute
        val maleSwimwearOrUnderwear: Float? = null,
        @get:DynamoDBAttribute
        val femaleSwimwearOrUnderwear: Float? = null,
        @get:DynamoDBAttribute
        val revealingClothes: Float? = null)
