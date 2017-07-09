import akka.util.ByteString
import com.amazonaws.services.rekognition.AmazonRekognitionAsyncClient
import com.amazonaws.services.rekognition.model.DetectModerationLabelsRequest
import com.amazonaws.services.rekognition.model.DetectModerationLabelsResult
import com.amazonaws.services.rekognition.model.Image
import org.apache.log4j.Logger
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage
import java.util.concurrent.ExecutionException
import java.util.concurrent.Future
import javax.inject.Inject

class ModerationClient @Inject constructor(val asyncClient: AmazonRekognitionAsyncClient) {

    private val logger = Logger.getLogger(ModerationClient::class.java)

    fun callModerationService(bytes: ByteString): CompletionStage<DetectModerationLabelsResult> {
        val moderationRequest = DetectModerationLabelsRequest()
                .withImage(Image().withBytes(bytes.asByteBuffer()))
        val resultFuture = asyncClient.detectModerationLabelsAsync(moderationRequest)

        val completableFutureResult = makeCompletableFuture(resultFuture)

        return completableFutureResult
    }

    //From  https://stackoverflow.com/questions/23301598/transform-java-future-into-a-completablefuture
    fun <T> makeCompletableFuture(future: Future<T>): CompletableFuture<T> {
        return CompletableFuture.supplyAsync<T> {
            try {
                val startTime = System.currentTimeMillis()
                return@supplyAsync future.get()
                        .apply { logger.info("Time to call moderation service:${System.currentTimeMillis() - startTime}") }
            } catch (e: InterruptedException) {
                throw RuntimeException(e)
            } catch (e: ExecutionException) {
                throw RuntimeException(e)
            }
        }
    }

}