import com.amazonaws.services.rekognition.model.DetectModerationLabelsResult
import models.ModerationRequest
import models.ModerationResult
import java.util.concurrent.CompletableFuture
import java.util.stream.Collectors

fun convertLabelResult(request: ModerationRequest, result: DetectModerationLabelsResult): ModerationResult {
    if (result.moderationLabels.isEmpty()) return ModerationResult(request.hostname, request.imageUrl, hasLabels = 0)
    else return result.moderationLabels.fold(ModerationResult(request.hostname, request.imageUrl, hasLabels = 1)) {
        acc, label ->
        when (label.name) {
            "Explicit Nudity" -> acc.copy(explicitNudity = label.confidence)
            "Graphic Male Nudity" -> acc.copy(graphicMaleNudity = label.confidence)
            "Graphic Female Nudity" -> acc.copy(graphicFemaleNudity = label.confidence)
            "Sexual Activity" -> acc.copy(sexualActivity = label.confidence)
            "Partial Nudity" -> acc.copy(partialNudity = label.confidence)
            "Suggestive" -> acc.copy(suggestive = label.confidence)
            "Female Swimwear Or Underwear" -> acc.copy(femaleSwimwearOrUnderwear = label.confidence)
            "Male Swimwear Or Underwear" -> acc.copy(maleSwimwearOrUnderwear = label.confidence)
            "Revealing Clothes" -> acc.copy(revealingClothes = label.confidence)
            else -> acc.copy(hasError = 1, errorMessage = "Couldn't find label name ${label.name} in $label " + acc.errorMessage)
        }
    }
}

fun createErrorResult(request: ModerationRequest, throwable: Throwable?): ModerationResult {
    return ModerationResult(request.hostname, request.imageUrl, hasError = 1, errorMessage = throwable?.localizedMessage)
}

//From https://stackoverflow.com/questions/30025428/listfuture-to-futurelist-sequence
fun <T> sequence(com: List<CompletableFuture<T>>): CompletableFuture<List<T>> {
    return CompletableFuture.allOf(*com.toTypedArray())
            .thenApply { _ ->
                com.stream()
                        .map<T> { it.join() }
                        .collect(Collectors.toList())
            }
}

