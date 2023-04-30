package ir.farbod.consumer.consumer;

import ir.farbod.consumer.service.BookLibraryEventService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.listener.AcknowledgingMessageListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class BookLibraryConsumerManualOffsetService implements AcknowledgingMessageListener<Integer, String> {

    private final BookLibraryEventService bookLibraryEventService;

    @SneakyThrows
    @Override
    @KafkaListener(topics = {"${spring.kafka.template.default-topic}"})
    public void onMessage(ConsumerRecord<Integer, String> consumerRecord, Acknowledgment acknowledgment) {
        log.info("Manual ConsumerRecord: ====> {} ", consumerRecord);

        bookLibraryEventService.processEvent(consumerRecord);

        acknowledgment.acknowledge();
    }
}
