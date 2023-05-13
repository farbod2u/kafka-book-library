package ir.farbod.consumer.service;

import ir.farbod.consumer.entity.FailureRecord;
import ir.farbod.consumer.repository.FailureRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Service;

/***
 * @author Saeed Safaeian
 * Date : 13/05/2023
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FailureRecordService {

    private final FailureRecordRepository failureRecordRepository;


    public void save(ConsumerRecord<?, ?> consumerRecord, Exception e, String status) {
        var entity = FailureRecord.builder()
                .errorRecord_key((Integer) consumerRecord.key())
                .exception_message(e.getCause().getMessage())
                .errorRecord_value(consumerRecord.value().toString())
                .offset_value(consumerRecord.offset())
                .partion(consumerRecord.partition())
                .status(status)
                .topic(consumerRecord.topic())
                .build();

        failureRecordRepository.save(entity);
    }
}
