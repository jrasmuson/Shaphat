import akka.actor.ActorSystem
import akka.http.javadsl.ConnectHttp
import akka.http.javadsl.Http
import akka.http.javadsl.server.AllDirectives
import akka.http.javadsl.server.PathMatchers
import akka.http.javadsl.server.PathMatchers.segment
import akka.http.javadsl.server.Route
import akka.stream.ActorMaterializer
import java.io.IOException


class MyTestImageService : AllDirectives() {

    //used to serve image from the test resources to test the webclient
    fun createRoute(): Route {
        return path(PathMatchers.segment("images").slash(segment())
        ) { name -> getFromResource(name) }
    }

    companion object {
        //Used for testing out the image server
        @Throws(IOException::class)
        @JvmStatic fun main(args: Array<String>) {
            val system = ActorSystem.create()
            val materializer = ActorMaterializer.create(system)

            val app = MyTestImageService()

            val host = ConnectHttp.toHost("127.0.0.1")
            println("port ${host.port()}")

            Http.get(system).bindAndHandle(app.createRoute().flow(system, materializer), host, materializer)

            println("Type RETURN to exit...")
            readLine()
            system.terminate()
        }
    }
}
