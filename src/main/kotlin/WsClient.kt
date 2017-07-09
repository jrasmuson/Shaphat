import WsClientModule.Companion.imageTimeoutLabel
import org.apache.log4j.Logger
import play.libs.ws.DefaultBodyReadables
import play.libs.ws.StandaloneWSResponse
import play.libs.ws.ahc.StandaloneAhcWSClient
import java.time.Duration
import java.util.concurrent.CompletionStage
import javax.inject.Inject
import javax.inject.Named

class WsClient @Inject constructor(private val client: StandaloneAhcWSClient, @Named(imageTimeoutLabel) private val timeout: Long) : DefaultBodyReadables {
    private val logger = Logger.getLogger(WsClient::class.java)
    fun retrieveImage(url: String): CompletionStage<out StandaloneWSResponse> {
        val startTime = System.currentTimeMillis()
        return client
                .url(url)
                .setRequestTimeout(Duration.ofSeconds(timeout))
                .get()
                .thenApply {
                    logger.info("Time to retrieve url=$url was duration=${System.currentTimeMillis() - startTime}")
                    it
                }
    }
}