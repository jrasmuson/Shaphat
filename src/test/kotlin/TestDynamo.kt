import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.local.embedded.DynamoDBEmbedded
import org.junit.BeforeClass
import org.junit.Test
import kotlin.test.assertEquals
import DynamoUtils.createNewTable
import DynamoUtils.tableName
import models.AlreadyFinishedRequest
import models.ModerationResult
import org.junit.AfterClass

/**
 * Spek can't give jvm arguments to test so have to use junit
 * need to run with jvm arguments -Dsqlite4java.library.path=dynamoDrivers
 *
 */
class TestDynamo {
    @Test
    fun loadingToAnEmptyTable() {
        val batchWrite = dynamoClient?.batchWrite(emptyList<ModerationResult>())
        assertEquals(0, batchWrite?.size)
    }

    @Test
    fun gettingBackResult() {
        val request = AlreadyFinishedRequest("test.com", "http://test.com/insertedImage.jpg")
        dynamoClient?.batchWrite(listOf(request))
        val batchWrite = dynamoClient?.batchGet(listOf(request))
        assertEquals(1, batchWrite?.size)
    }

    companion object {
        var amazonDynamoDB: AmazonDynamoDB? = null
        var dynamoClient: DynamoClient? = null
        @BeforeClass
        @JvmStatic internal fun setup() {
            amazonDynamoDB = DynamoDBEmbedded.create().amazonDynamoDB()
            createNewTable(amazonDynamoDB)
            dynamoClient = DynamoClient(DynamoDBMapper(amazonDynamoDB), tableName)
        }

        @AfterClass
        @JvmStatic fun cleanup() {
            amazonDynamoDB?.shutdown()
        }
    }
}