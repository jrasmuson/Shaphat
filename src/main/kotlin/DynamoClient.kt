import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.google.inject.Inject
import com.google.inject.name.Named
import models.AlreadyFinishedRequest
import org.apache.log4j.Logger
import java.util.ArrayList

class DynamoClient @Inject constructor(val dynamoDBMapper: DynamoDBMapper, @Named("table_name") val tableName: String) {

    private val logger = Logger.getLogger(DynamoClient::class.java)

    private val tableOverride = DynamoDBMapperConfig.builder()
            .withTableNameOverride(DynamoDBMapperConfig.TableNameOverride(tableName))
            .build()

    fun batchGet(objects: List<*>): List<AlreadyFinishedRequest>? {
        //batchLoad could be for multiple tables so it comes back as a Map
        //but since we are asking for only one table get that table directly
        return try {
            dynamoDBMapper.batchLoad(objects, tableOverride)[tableName]?.map { it as AlreadyFinishedRequest }
        } catch (e: Exception) {
            logger.error("Error reading from dynamo table", e)
            null
        }
    }

    fun batchWrite(objects: List<*>): List<DynamoDBMapper.FailedBatch> {
        return try {
            return dynamoDBMapper.batchWrite(objects,
                    ArrayList<Any>(),
                    tableOverride
            )
        } catch (e: Exception) {
            logger.error("Error writing to dynamo table", e)
            emptyList<DynamoDBMapper.FailedBatch>()
        }

    }
}