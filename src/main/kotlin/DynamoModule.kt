import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.google.inject.AbstractModule
import com.google.inject.name.Names
import org.apache.log4j.Logger
import kotlin.system.measureTimeMillis

class DynamoModule : AbstractModule() {
    private val logger = Logger.getLogger(DynamoModule::class.java)

    override fun configure() {
        val tableName = System.getenv(Companion.tableNameLabel) ?: throw IllegalArgumentException("Need ${Companion.tableNameLabel} env variable")
        bindConstant().annotatedWith(Names.named(tableNameLabel)).to(tableName)

        //This one could be the slowest it can take 2 seconds
        logger.debug("Time to bind dynamo: duration=" + measureTimeMillis {
            bind(DynamoDBMapper::class.java).toInstance(
                    DynamoDBMapper(AmazonDynamoDBClientBuilder.standard().build()))
        })
    }

    companion object {
        val tableNameLabel = "table_name"
    }
}