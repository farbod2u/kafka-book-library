package ir.farbod.consumer.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

//@Component
@Slf4j
public class BookLibraryConsumerAutoOffsetService {

    @Value("${spring.kafka.template.default-topic}")
    private String TOPIC;

    @KafkaListener(topics = {"${spring.kafka.template.default-topic}"})
    public void onMessage(ConsumerRecord<Integer, String> consumerRecord){
        log.info("ConsumerRecord: ====> {} ", consumerRecord);

    }

}
