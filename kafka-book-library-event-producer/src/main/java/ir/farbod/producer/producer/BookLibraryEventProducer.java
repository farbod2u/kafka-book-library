package ir.farbod.producer.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.farbod.producer.entity.BookLibraryEvent;
import ir.farbod.producer.service.FailureRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
@Profile("dev")
public class BookLibraryEventProducer {

    public static final String RETRY = "RETRY";
    public static final String DEAD = "DEAD";
    public static final String SUCCESS = "SUCCESS";

    private final KafkaTemplate<Integer, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final FailureRecordService failureRecordService;

    @Value("${spring.kafka.template.default-topic}")
    private String TOPIC;

    public CompletableFuture<SendResult<Integer, String>> sendBookEvent_Async(BookLibraryEvent event) throws JsonProcessingException {
        var key = event.getEventId();
        var value = objectMapper.writeValueAsString(event);

        ProducerRecord<Integer, String> record = buildProducerRecord(TOPIC, key, value);
        CompletableFuture<SendResult<Integer, String>> sendResultCompletableFuture = kafkaTemplate.send(record);
        sendResultCompletableFuture
                .whenCompleteAsync((result, throwable) -> {
                    if (throwable == null)
                        handleSuccess(result);
                    else
                        handleException(throwable, record, event.getGuid());
                });

//                .whenComplete((result, throwable) -> {
//                    if (throwable == null)
//                        handleSuccess(result);
//                    else
//                        handleException(throwable);
//                });
        return sendResultCompletableFuture;
    }

    public SendResult<Integer, String> sendBookEvent_Sync(BookLibraryEvent event) {
        SendResult<Integer, String> result = null;

        try {
            var key = event.getEventId();
            var value = objectMapper.writeValueAsString(event);
            ProducerRecord<Integer, String> record = buildProducerRecord(TOPIC, key, value);

            try {
//            result = kafkaTemplate.sendDefault(key, value).get(1, TimeUnit.SECONDS);
//            or
//            result = kafkaTemplate.send(TOPIC, key, value).get();
//            or

                result = kafkaTemplate.send(record).get();
            } catch (Exception e) {
                handleException(e, record, event.getGuid());
            } finally {
                return result;
            }
        } catch (Exception e) {
            log.error("Exception in sendBookEvent_Sync() before send event : {}", e);
        } finally {
            return result;
        }
    }

    private ProducerRecord<Integer, String> buildProducerRecord(String topic, Integer key, String value) {
        List<Header> headers = List.of(new RecordHeader("event-source", "scanner".getBytes()));
        return new ProducerRecord<>(topic, null, key, value, headers);
    }

    private void handleException(Throwable throwable, ProducerRecord<Integer, String> producerRecord, String guid) {
        log.error("Error on send with exception ", throwable);
        failureRecordService.save(producerRecord, throwable, guid, RETRY);
    }

    private void handleSuccess(SendResult<Integer, String> result) {
        log.info("Sent event successfully with Key {} and Value {}, Data {}", result.getProducerRecord().key(), result.getProducerRecord().value(), result.getProducerRecord());
    }

}
