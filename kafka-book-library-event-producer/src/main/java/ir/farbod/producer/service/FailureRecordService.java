package ir.farbod.producer.service;

import ir.farbod.producer.entity.FailureRecord;
import ir.farbod.producer.repository.FailureRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Service;

import java.util.List;

/***
 * @author Saeed Safaeian
 * Date : 13/05/2023
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FailureRecordService {

    private final FailureRecordRepository failureRecordRepository;


    public void save(ProducerRecord<Integer, String> producerRecord, Throwable e, String guid, String status) {
        if (failureRecordRepository.findByGuid(guid).isEmpty()) {
            var entity = FailureRecord.builder()
                    .exception_message(e.getCause().getMessage())
                    .bookLibraryEvent(producerRecord.value())
                    .status(status)
                    .topic(producerRecord.topic())
                    .guid(guid)
                    .build();

            failureRecordRepository.save(entity);
        }
    }

    public List<FailureRecord> findAllByStatus(String status) {
        return failureRecordRepository.findAllByStatus(status);
    }

    public void update(FailureRecord entity) {
        failureRecordRepository.save(entity);
    }
}
