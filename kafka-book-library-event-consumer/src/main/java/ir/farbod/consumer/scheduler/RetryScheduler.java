package ir.farbod.consumer.scheduler;

import com.fasterxml.jackson.core.JsonProcessingException;
import ir.farbod.consumer.config.BookLibraryConsumerConfig;
import ir.farbod.consumer.entity.FailureRecord;
import ir.farbod.consumer.service.BookLibraryEventService;
import ir.farbod.consumer.service.FailureRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/***
 * @author Saeed Safaeian
 * Date : 13/05/2023
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class RetryScheduler {

    private final FailureRecordService failureRecordService;
    private final BookLibraryEventService bookLibraryEventService;

    @Scheduled(fixedRate = 10000)
    public void retryFailedRecordScheduler() {

        log.info("Retrying Failed Record Scheduler Start.");
        failureRecordService.findAllByStatus(BookLibraryConsumerConfig.RETRY)
                .forEach(failureRecord -> {
                    log.info("Failed Record Scheduled : {}", failureRecord);
                    try {
                        bookLibraryEventService.processEvent(buildCumsumerRecord(failureRecord));
                        failureRecord.setStatus(BookLibraryConsumerConfig.SUCCESS);
                        failureRecordService.update(failureRecord);
                        log.info("Failed Record Successfully Recovered.");
                    } catch (Exception e) {
                        log.error("Exception in retryFailedRecordScheduler() : {}", e);
                    }
                });
        log.info("Retrying Failed Record Scheduler Compelete.");
    }

    private ConsumerRecord<Integer, String> buildCumsumerRecord(FailureRecord failureRecord) {
        return new ConsumerRecord<>(
                failureRecord.getTopic(),
                failureRecord.getPartion(),
                failureRecord.getOffset_value(),
                failureRecord.getErrorRecord_key(),
                failureRecord.getErrorRecord_value().replace("999", "1")
        );
    }

}
