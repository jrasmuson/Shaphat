import akka.actor.ActorSystem
import akka.http.javadsl.ConnectHttp
import akka.http.javadsl.Http
import akka.stream.ActorMaterializer
import com.google.inject.Guice
import com.google.inject.Injector
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals

object WebClientIntegrationSpec : Spek({

    describe("wsclient") {
        val injector: Injector = Guice.createInjector(WsClientModule())
        val wsClient = injector.getInstance(WsClient::class.java)
        val system = injector.getInstance(ActorSystem::class.java)
        beforeGroup {
            val materializer = ActorMaterializer.create(system)
            val app = MyTestImageService()
            val host = ConnectHttp.toHost("127.0.0.1", 9000)
            Http.get(system).bindAndHandle(app.createRoute().flow(system, materializer), host, materializer)
        }

        on("good urls") {
            it("returns 200") {
                val imageRequest = wsClient.retrieveImage("http://localhost:9000/images/roses.jpg")
                val get = imageRequest.toCompletableFuture().get(5, TimeUnit.SECONDS)
                assertEquals(get.status, 200)
            }
        }
        on("bad url") {
            it("returns 404") {
                val imageRequest = wsClient.retrieveImage("http://localhost:9000/images/badurl.jpg")
                val get = imageRequest.toCompletableFuture().get(5, TimeUnit.SECONDS)
                assertEquals(get.status, 404)
            }
        }
        afterGroup {
            system.terminate()
        }
    }
})