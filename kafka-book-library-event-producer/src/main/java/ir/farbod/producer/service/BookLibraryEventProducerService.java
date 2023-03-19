package ir.farbod.producer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.farbod.producer.entity.BookLibraryEvent;
import ir.farbod.producer.exception.RequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookLibraryEventProducerService {

    private final KafkaTemplate<Integer, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private final String TOPIC = "book-lib-event";

    public void sendBookEvent_Async(BookLibraryEvent event) throws JsonProcessingException {
        var key = event.getEventId();
        var value = objectMapper.writeValueAsString(event.getBook());

        kafkaTemplate.sendDefault(key, value)
                .whenComplete((result, throwable) -> {
                    if (throwable == null)
                        handleSuccess(result);
                    else
                        handleException(throwable);
                });
    }

    public SendResult<Integer, String> sendBookEvent_Sync(BookLibraryEvent event) {
        SendResult<Integer, String> result = null;
        try {
            var key = event.getEventId();
            var value = objectMapper.writeValueAsString(event.getBook());

//            result = kafkaTemplate.sendDefault(key, value).get(1, TimeUnit.SECONDS);
//            or

//            result = kafkaTemplate.send(TOPIC, key, value).get();
//            or

            result = kafkaTemplate.send(buildProducerRecord(TOPIC, key, value)).get();

        } catch (Exception e) {
            handleException(e);
        } finally {
            return result;
        }
    }

    private ProducerRecord<Integer, String> buildProducerRecord(String topic, Integer key, String value) {
        return new ProducerRecord<>(topic, null, key, value, null);
    }

    private void handleException(Throwable throwable) {
        log.error("Error on send with exception {}", throwable);
    }

    private void handleSuccess(SendResult<Integer, String> result) {
        log.info("Sent event successfully with Key {} and Value {}, Data {}", result.getProducerRecord().key(), result.getProducerRecord().value(), result.getProducerRecord());
    }

}
