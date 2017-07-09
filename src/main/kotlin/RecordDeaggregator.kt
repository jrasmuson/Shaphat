import com.amazonaws.services.kinesis.clientlibrary.types.UserRecord
import com.amazonaws.services.kinesis.model.Record
import com.amazonaws.services.lambda.runtime.events.KinesisEvent.KinesisEventRecord

import java.util.LinkedList
import java.util.function.Consumer
import java.util.stream.Stream

/**
 * A Kinesis deaggregator convenience class. This class contains
 * a number of static methods that provide different interfaces for
 * deaggregating user records from an existing aggregated Kinesis record. This class
 * is oriented towards deaggregating Kinesis records as provided by AWS Lambda
 * (for other applications, record deaggregation is handled transparently by the
 * Kinesis Consumer Library).

 * NOTE: Any non-aggregated records passed to any deaggregation methods will be
 * returned unchanged.

 */
object RecordDeaggregator {
    /**
     * Interface used by a calling method to call the process function

     */
    interface KinesisUserRecordProcessor {
        fun process(userRecords: List<UserRecord>): Void
    }

    /**
     * Method to process a set of Kinesis user records from a Stream of Kinesis
     * Event Records using the Java 8 Streams API

     * @param inputStream
     * *            The Kinesis Event Records provided by AWS Lambda
     * *
     * @param streamConsumer
     * *            Instance implementing the Consumer interface to process the
     * *            deaggregated UserRecords
     * *
     * @return Void
     */
    fun stream(inputStream: Stream<KinesisEventRecord>, streamConsumer: Consumer<UserRecord>): Void? {
        // convert the event input record set to a List of Record
        val rawRecords = LinkedList<Record>()
        inputStream.forEachOrdered { rec -> rawRecords.add(rec.kinesis) }

        // deaggregate UserRecords from the Kinesis Records
        val deaggregatedRecords = UserRecord.deaggregate(rawRecords)
        deaggregatedRecords.stream().forEachOrdered(streamConsumer)

        return null
    }

    /**
     * Method to process a set of Kinesis user records from a list of Kinesis
     * Event Records using pre-Streams style API

     * @param inputRecords
     * *            The Kinesis Event Records provided by AWS Lambda
     * *
     * @param processor
     * *            Instance implementing KinesisUserRecordProcessor
     * *
     * @return Void
     */
    fun processRecords(inputRecords: List<KinesisEventRecord>, processor: KinesisUserRecordProcessor): Void {
        // extract raw Kinesis Records from input event records
        val rawRecords = LinkedList<Record>()
        for (rec in inputRecords) {
            rawRecords.add(rec.kinesis)
        }

        // invoke provided processor
        return processor.process(UserRecord.deaggregate(rawRecords))
    }

    /**
     * Method to bulk deaggregate a set of Kinesis user records from a list of
     * Kinesis Event Records.

     * @param inputRecords
     * *            The Kinesis Event Records provided by AWS Lambda
     * *
     * @return A list of Kinesis UserRecord objects obtained by deaggregating
     * *         the input list of KinesisEventRecords
     */
    fun deaggregate(inputRecords: List<KinesisEventRecord>): List<UserRecord> {
        val outputRecords = LinkedList<UserRecord>()
        for (inputRecord in inputRecords) {
            outputRecords.addAll(deaggregate(inputRecord))
        }
        return outputRecords
    }

    /**
     * Method to deaggregate a single Kinesis record into one or more
     * Kinesis user records.

     * @param inputRecord
     * *            The single KinesisEventRecord to deaggregate
     * *
     * @return A list of Kinesis UserRecord objects obtained by deaggregating
     * *         the input KinesisEventRecord
     */
    fun deaggregate(inputRecord: KinesisEventRecord): List<UserRecord> {
        return UserRecord.deaggregate(listOf(inputRecord.kinesis))
    }
}
