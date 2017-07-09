import akka.util.ByteString
import com.amazonaws.services.rekognition.AmazonRekognitionAsyncClient
import com.amazonaws.services.rekognition.model.*
import com.amazonaws.util.IOUtils
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.fail


object CallModeratorSpec : Spek({
    given("A moderator client with some response setup") {
        val roseBytes = ModeratorUtil.imageBytes("roses.jpg")
        val verseBytes = ModeratorUtil.imageBytes("verse.jpg")
        val orchidBytes = ModeratorUtil.imageBytes("orchid1.jpg")
        val mock = mock<AmazonRekognitionAsyncClient> {
            on { detectModerationLabelsAsync(DetectModerationLabelsRequest().withImage(Image().withBytes(roseBytes.asByteBuffer()))) } doReturn (
                    CompletableFuture.completedFuture(DetectModerationLabelsResult()
                            .withModerationLabels(emptyList()))
                    )
            on { detectModerationLabelsAsync(DetectModerationLabelsRequest().withImage(Image().withBytes(verseBytes.asByteBuffer()))) } doReturn (
                    CompletableFuture.completedFuture(DetectModerationLabelsResult()
                            .withModerationLabels(ModerationLabel().withName("Sexual Activity").withConfidence(0.6f)))
                    )
            on { detectModerationLabelsAsync(DetectModerationLabelsRequest().withImage(Image().withBytes(orchidBytes.asByteBuffer()))) } doReturn (
                    CompletableFuture.supplyAsync {
                        throw InvalidParameterException(
                                "Request has Invalid Parameters (Service: AmazonRekognition; Status Code: 400; Error Code: InvalidParameterException; " +
                                        "Request ID: 41c02b76-6386-11e7-bf09-8ddb6bd345ba")
                    }
                            as Future<DetectModerationLabelsResult>
                    )

        }
        val moderatorClient = ModerationClient(mock)
        on("test one call") {

            it("simulated response with labels") {
                val callModerationService = moderatorClient.callModerationService(verseBytes)
                val moderationLabels = callModerationService.toCompletableFuture().get(5, TimeUnit.SECONDS)
                assertEquals(1, moderationLabels.moderationLabels.size)
                val label = moderationLabels.moderationLabels[0]
                assertEquals("Sexual Activity", label.name)
                assertEquals(0.6f, label.confidence)
            }
            it("simulated good response") {
                val callModerationService = moderatorClient.callModerationService(roseBytes)
                val moderationLabels = callModerationService.toCompletableFuture().get(5, TimeUnit.SECONDS)
                assertEquals(0, moderationLabels.moderationLabels.size)

            }
            it("simulated error response") {
                val callModerationService = moderatorClient.callModerationService(orchidBytes)
                assertFailsWith<ExecutionException> {
                    callModerationService.toCompletableFuture().get(5, TimeUnit.SECONDS)
                }
            }
        }
    }
})

object ModeratorUtil {
    fun imageBytes(image: String): ByteString {
        val imagePath = javaClass.getResource(image).file
        val byteBuffer = FileInputStream(File(imagePath)).use { inputStream -> ByteBuffer.wrap(IOUtils.toByteArray(inputStream)) }
        return ByteString.ByteStrings.fromByteBuffer(byteBuffer)
    }

}