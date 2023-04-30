package ir.farbod.consumer.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.listener.AcknowledgingMessageListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class BookLibraryConsumerManualOffsetService implements AcknowledgingMessageListener<Integer, String> {

    @Override
    @KafkaListener(topics = {"${spring.kafka.template.default-topic}"})
    public void onMessage(ConsumerRecord<Integer, String> consumerRecord, Acknowledgment acknowledgment) {
        log.info("Manual ConsumerRecord: ====> {} ", consumerRecord);

        acknowledgment.acknowledge();
    }
}
