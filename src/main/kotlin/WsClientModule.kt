import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.ActorMaterializerSettings
import com.google.inject.AbstractModule
import com.google.inject.name.Names
import com.typesafe.config.ConfigFactory
import org.apache.log4j.Logger
import play.libs.ws.ahc.AhcWSClientConfigFactory
import play.libs.ws.ahc.StandaloneAhcWSClient
import kotlin.system.measureTimeMillis


class WsClientModule : AbstractModule() {

    private val logger = Logger.getLogger(WsClientModule::class.java)

    override fun configure() {
        val start=System.currentTimeMillis()
        val imageTimeout = (System.getenv(imageTimeoutLabel) ?: defaultImageTimeout).toLong()
        bindConstant().annotatedWith(Names.named(imageTimeoutLabel)).to(imageTimeout)

        val name = "wsclient"
        val system = ActorSystem.create(name)
        logger.debug("Time to start actor system duration=${System.currentTimeMillis()-start}")

        val settings = ActorMaterializerSettings.create(system)
        val materializer = ActorMaterializer.create(settings, system, name)


        logger.debug("time to create and bind Ws client duration="+ measureTimeMillis {
            // Create the WS client from the `application.conf` file, the current classloader and materializer.
            val client = StandaloneAhcWSClient.create(
                    AhcWSClientConfigFactory.forConfig(ConfigFactory.load(), system.javaClass.classLoader),
                    materializer
            )
            bind(ActorSystem::class.java).toInstance(system)
            bind(StandaloneAhcWSClient::class.java).toInstance(client)
        })
    }


    companion object {
        const val imageTimeoutLabel = "image_timeout"
        const val defaultCompleteTimeoutLabel ="complete_timeout"
        //default to 10 seconds
        const val defaultImageTimeout = "10"
        const val defaultCompleteTimeout = "30"
    }

}
