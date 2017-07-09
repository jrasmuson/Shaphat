import com.amazonaws.services.rekognition.model.DetectModerationLabelsResult
import com.amazonaws.services.rekognition.model.ModerationLabel
import models.ModerationRequest
import models.ModerationResult
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import kotlin.test.assertEquals

object ModerationLabelsToResultSpec : Spek({
    given("A moderation request") {
        val hostname = "test.com"
        val imageUrl = "http://test.com/image1"
        val request = ModerationRequest(hostname, imageUrl)
        on("empty moderation labels") {
            val result = DetectModerationLabelsResult().withModerationLabels()
            val moderationResult: ModerationResult = convertLabelResult(request, result)
            it("has no Labels") {
                assertEquals(ModerationResult(hostname, imageUrl, hasLabels = 0, createTime = moderationResult.createTime), moderationResult)
            }
        }
        on("moderation labels with one label") {
            val result = DetectModerationLabelsResult()
                    .withModerationLabels(ModerationLabel()
                            .withName("Sexual Activity")
                            .withConfidence(0.6f))
            val moderationResult: ModerationResult = convertLabelResult(request, result)
            it("has labels") {
                assertEquals(ModerationResult(hostname, imageUrl, hasLabels = 1, sexualActivity = 0.6f, createTime = moderationResult.createTime), moderationResult)
            }
        }
        on("one bad label") {
            val result = DetectModerationLabelsResult()
                    .withModerationLabels(ModerationLabel()
                            .withName("bad label")
                            .withConfidence(0.6f))
            val moderationResult: ModerationResult = convertLabelResult(request, result)
            it("has the error flag") {
                //just want to make sure the error flag is set. Can ignore the error message
                assertEquals(ModerationResult(hostname, imageUrl, hasLabels = 1, hasError = 1, errorMessage = moderationResult.errorMessage, createTime = moderationResult.createTime), moderationResult)
            }
        }
    }
})

