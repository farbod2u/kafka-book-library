package ir.farbod.consumer.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import ir.farbod.consumer.service.BookLibraryEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/***
 * @author Saeed Safaeian
 * Date : 08/05/2023
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class BookLibreryEventRetryConsumer {

    private final BookLibraryEventService bookLibraryEventService;

    @KafkaListener(topics = {"${topics.retry}"}, groupId = "${groups.retry}")
    public void onMessage(ConsumerRecord<Integer, String> consumerRecord, Acknowledgment acknowledgment) throws JsonProcessingException {
        log.info("Retry ConsumerRecord: ====> {} ", consumerRecord);
        consumerRecord.headers().forEach(header -> log.info("Header Key : {} , Value : {}", header.key(), new String(header.value())));

        bookLibraryEventService.processEvent(consumerRecord);

        acknowledgment.acknowledge();
    }

    /*

// https://docs.spring.io/spring-kafka/reference/html/#dead-letters

The record sent to the dead-letter topic is enhanced with the following headers:

    KafkaHeaders.DLT_EXCEPTION_FQCN: The Exception class name (generally a ListenerExecutionFailedException, but can be others).
    KafkaHeaders.DLT_EXCEPTION_CAUSE_FQCN: The Exception cause class name, if present (since version 2.8).
    KafkaHeaders.DLT_EXCEPTION_STACKTRACE: The Exception stack trace.
    KafkaHeaders.DLT_EXCEPTION_MESSAGE: The Exception message.
    KafkaHeaders.DLT_KEY_EXCEPTION_FQCN: The Exception class name (key deserialization errors only).
    KafkaHeaders.DLT_KEY_EXCEPTION_STACKTRACE: The Exception stack trace (key deserialization errors only).
    KafkaHeaders.DLT_KEY_EXCEPTION_MESSAGE: The Exception message (key deserialization errors only).
    KafkaHeaders.DLT_ORIGINAL_TOPIC: The original topic.
    KafkaHeaders.DLT_ORIGINAL_PARTITION: The original partition.
    KafkaHeaders.DLT_ORIGINAL_OFFSET: The original offset.
    KafkaHeaders.DLT_ORIGINAL_TIMESTAMP: The original timestamp.
    KafkaHeaders.DLT_ORIGINAL_TIMESTAMP_TYPE: The original timestamp type.
    KafkaHeaders.DLT_ORIGINAL_CONSUMER_GROUP: The original consumer group that failed to process the record (since version 2.8).

     */

}
