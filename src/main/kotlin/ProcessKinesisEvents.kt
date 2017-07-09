import RecordDeaggregator.deaggregate
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper.FailedBatch
import com.amazonaws.services.kinesis.clientlibrary.types.UserRecord
import com.amazonaws.services.lambda.runtime.events.KinesisEvent
import models.AlreadyFinishedRequest
import models.ModerationRequest
import org.apache.log4j.Logger
import java.io.IOException
import java.util.concurrent.CompletableFuture
import javax.inject.Inject

class ProcessKinesisEvents @Inject constructor(val dynamo: DynamoClient, val moderationClient: ModerationClient, val wsClient: WsClient) {

    private val logger = Logger.getLogger(ProcessKinesisEvents::class.java)


    fun recordHandler(event: KinesisEvent): CompletableFuture<List<FailedBatch>> {
        //When pushing to kinesis with the producer library you can aggregate records into one record.
        //This will separate those into individual records
        val records = deaggregate(event.records)
        logger.info("Number of records=${records.size}")

        //If the json fails to parse it will be null. We only want to use the service for jsons that can be parsed
        val allRequests = records.mapNotNull(this::convertToRequest)
        logger.info("Number parsed=${allRequests.size}")


        //Want to only request images that haven't been requested before.
        val requestsToDo = requestsNotAlreadyDone(allRequests)
        logger.info("Number of new requests=${requestsToDo.size}")


        // The flow is ModerationRequest -> Web Service call to get image -> If succeed call moderation service -> convert successful calls into Moderation responses that can be stored
        // Turn exceptions into error Results that can be stored in DynamoDb
        val moderationResults = requestsToDo.map { request ->
            wsClient.retrieveImage(request.imageUrl)
                    .thenCompose { wsResult ->
                        if (wsResult.status == 200) {
                            moderationClient.callModerationService(
                                    wsResult.bodyAsBytes)
                        } else {
                            throw RuntimeException("Could not retrieve image. Status code=${wsResult.status}, for url=${request.imageUrl}")
                        }
                    }.handle { moderation, throwable ->
                if (moderation != null)
                    convertLabelResult(request, moderation)
                else
                    createErrorResult(request, throwable)
            }.toCompletableFuture()
        }


        //Want to wait for all the requests to come back. This returns at the speed of the slowest request
        val resultOfAllRequests = sequence(moderationResults)

        // Once all the calls have been turned into
        return resultOfAllRequests.thenApply { dynamo.batchWrite(it) }
    }

    private fun convertToRequest(record: UserRecord): ModerationRequest? {
        val data = record.data
        val json = String(data.array())
        return try {
            val moderationRequest = JsonMapper.parseModerationRequest(json)
            moderationRequest
        } catch (e: IOException) {
            logger.error("Failed to parse: hashKey=${record.explicitHashKey}, json=$json", e)
            null
        }
    }

    //Dynamo cannot use the default data class that only use the constructor to add everything
    //It wants to call the setters after calling the no arg constructor so have to use an intermediate class called AlreadyFinishedRequest
    //This class has setters and the fields can be null be they won't be null because if it had passed the previous json stage we know it will have these fields
    private fun requestsNotAlreadyDone(allRequests: List<ModerationRequest>): List<ModerationRequest> {

        val asIfAlreadyFinished = allRequests.map { AlreadyFinishedRequest(it.hostname, it.imageUrl) }
        val alreadyChecked = dynamo.batchGet(asIfAlreadyFinished)
        return if (alreadyChecked != null && !alreadyChecked.isEmpty()) {
            (asIfAlreadyFinished.toSet() - alreadyChecked)
                    .map { ModerationRequest(it.hostname!!, it.imageUrl!!) }
                    .toList()
        } else {
            allRequests
        }
    }


}