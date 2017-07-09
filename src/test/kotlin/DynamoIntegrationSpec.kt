import DynamoUtils.createNewTable
import DynamoUtils.tableName
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.local.embedded.DynamoDBEmbedded
import models.AlreadyFinishedRequest
import models.ModerationResult
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import kotlin.test.assertEquals

// Spek can't give jvm arguments to test so have to use junit
//Also Spek is having problems stopping the database so the test keeps running
//object DynamoIntegrationSpec : Spek({
//    var amazonDynamoDB:AmazonDynamoDB? = DynamoDBEmbedded.create().amazonDynamoDB()
//    createNewTable(amazonDynamoDB)
//    val dynamoClient = DynamoClient(DynamoDBMapper(amazonDynamoDB), tableName)
//    given("a dynamo connection") {
//        //        val endpoint = AwsClientBuilder.EndpointConfiguration("http://localhost:8000", "")
////        val amazonDynamoDB = AmazonDynamoDBClientBuilder.standard().withEndpointConfiguration(endpoint).build()
//
//
////        on("creating a table") {
////            it("returns success") {
////                val createNewTable = createNewTable(amazonDynamoDB)
////                assertEquals(tableName, createNewTable.tableName)
////            }
////        }
//
//        on("loading an empty list") {
//            it("doesn't have any failed batches") {
//                val batchWrite = dynamoClient.batchWrite(emptyList<ModerationResult>())
//                assertEquals(0, batchWrite.size)
//            }
//        }
//
//        on("loading one item") {
//            val result = ModerationResult("test.com", "http://test.com/image1")
//            it("load successfully") {
//                val batchWrite = dynamoClient.batchWrite(listOf(result))
//                assertEquals(0, batchWrite.size)
//            }
//        }
//        on("get back the request") {
//            val request = AlreadyFinishedRequest("test.com", "http://test.com/insertedImage.jpg")
//            dynamoClient.batchWrite(listOf(request))
//            it("get the one result") {
//                val batchWrite = dynamoClient.batchGet(listOf(request))
//                assertEquals(1, batchWrite?.size)
//            }
//        }
//
//    }
//    afterGroup{
//        println("before shut")
//        amazonDynamoDB?.shutdown()
//        amazonDynamoDB=null
//        println("after shut")
//    }
//})