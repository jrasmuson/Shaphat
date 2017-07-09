import com.amazonaws.AmazonClientException
import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.rekognition.AmazonRekognitionAsync
import com.amazonaws.services.rekognition.AmazonRekognitionAsyncClient
import com.amazonaws.services.rekognition.model.DetectModerationLabelsRequest
import com.amazonaws.services.rekognition.model.Image
import com.amazonaws.util.IOUtils
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import java.util.concurrent.Future


object DetectLabelsExample {

    @JvmStatic fun main(args: Array<String>) {
        val photo = pathOfImage("orchid1.jpg")

        val asyncClient = createAsyncClient()
        try {
            val request = createModerationRequest(photo)
            val resultFuture = asyncClient.detectModerationLabelsAsync(request)

            val completableFutureResult = makeCompletableFuture(resultFuture)
            completableFutureResult
                    .whenComplete { result, throwable ->
                        println("result $result ")
                        if(result!=null) {
                            val labels = result.moderationLabels

                            println("Detected labels for " + photo)
                            if (labels != null) {
                                for (label in labels) {
                                    println(label.name + ": " + label.confidence)
                                }
                            }
                        }else{
                            throwable.printStackTrace()
                        }
                    }.thenRun { asyncClient.shutdown() }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun createAsyncClient(): AmazonRekognitionAsync {
        val credentials: AWSCredentials
        try {
            credentials = ProfileCredentialsProvider("shaphat_test").credentials
        } catch (e: Exception) {
            throw AmazonClientException("Cannot load the credentials from the credential profiles file. "
                    + "Please make sure that your credentials file is at the correct "
                    + "location (/Users/userid/.aws/credentials), and is in a valid format.", e)
        }
        val rekognitionClient = AmazonRekognitionAsyncClient
                .asyncBuilder()
                .withRegion(Regions.US_WEST_2)
                .withCredentials(AWSStaticCredentialsProvider(credentials))
                .build()
        return rekognitionClient
    }

    private fun pathOfImage(image: String) = javaClass.getResource(image).file

    private fun createModerationRequest(photo: String?): DetectModerationLabelsRequest {
        val imageBytes: ByteBuffer =
                FileInputStream(File(photo)).use { inputStream -> ByteBuffer.wrap(IOUtils.toByteArray(inputStream)) }


        val request = DetectModerationLabelsRequest()
                .withImage(Image().withBytes(imageBytes))
        return request
    }

    fun <T> makeCompletableFuture(future: Future<T>): CompletableFuture<T> {
        return CompletableFuture.supplyAsync<T> {
            try {
                return@supplyAsync future.get()
            } catch (e: InterruptedException) {
                throw RuntimeException(e)
            } catch (e: ExecutionException) {
                throw RuntimeException(e)
            }
        }
    }
}

