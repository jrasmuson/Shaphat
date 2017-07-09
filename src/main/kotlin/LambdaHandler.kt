import com.amazonaws.services.lambda.runtime.events.KinesisEvent
import com.google.inject.Guice
import org.apache.log4j.Logger
import WsClientModule.Companion.defaultCompleteTimeout
import WsClientModule.Companion.defaultCompleteTimeoutLabel
import akka.actor.ActorSystem
import java.nio.ByteBuffer

import java.util.concurrent.TimeUnit

class LambdaHandler {
    private val logger = Logger.getLogger(LambdaHandler::class.java)

    fun recordHandler(event: KinesisEvent) {
        //The initialize time is about 3 seconds
        val initStartTime = System.currentTimeMillis()
        val injector = Guice.createInjector(DynamoModule(), WsClientModule())
        logger.debug("Initialize time. duration=${System.currentTimeMillis() - initStartTime}")
        try {
            val startTime = System.currentTimeMillis()
            val completeTimeout = (System.getenv(defaultCompleteTimeoutLabel) ?: defaultCompleteTimeout).toLong()


            val processKinesisEvents = injector.getInstance(ProcessKinesisEvents::class.java)
            val futureListOfErrors = processKinesisEvents.recordHandler(event)

            val listOfDynamoErrors = futureListOfErrors.get(completeTimeout, TimeUnit.SECONDS)
            logger.info("Finished all the requests in duration=${System.currentTimeMillis() - startTime}")

            if (!listOfDynamoErrors.isEmpty()) {
                listOfDynamoErrors.forEach { error -> logger.error("Had error: " + error, error.exception) }
            }
        } catch(e: Exception) {
            logger.error("Error with kinesis events", e)
        } finally {
            val system = injector.getInstance(ActorSystem::class.java)
            system.terminate()
        }
    }

    companion object {
        //Just for testing
        @JvmStatic fun main(args: Array<String>) {
            val lambdaHandler = LambdaHandler()
            val kinesisRecords = listOf(
                    KinesisEvent.KinesisEventRecord().apply {
                        kinesis = KinesisEvent.Record().apply {
                            data = ByteBuffer.wrap(json2.toByteArray())
                        }
                    })
            val kinesisEvent = KinesisEvent().apply {
                records = kinesisRecords
            }
            lambdaHandler.recordHandler(kinesisEvent)
        }

        const val json2 = """
{
        "hostname":"flowermeaning.com",
        "imageUrl":"http://farm3.static.flickr.com/2520/3804172425_6c506f5009_o.jpg"
}"""
    }
}