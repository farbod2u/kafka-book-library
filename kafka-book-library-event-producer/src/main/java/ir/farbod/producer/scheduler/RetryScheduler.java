package ir.farbod.producer.scheduler;


import com.fasterxml.jackson.databind.ObjectMapper;
import ir.farbod.producer.entity.BookLibraryEvent;
import ir.farbod.producer.producer.BookLibraryEventProducer;
import ir.farbod.producer.service.FailureRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static ir.farbod.producer.producer.BookLibraryEventProducer.RETRY;
import static ir.farbod.producer.producer.BookLibraryEventProducer.SUCCESS;

/***
 * @author Saeed Safaeian
 * Date : 13/05/2023
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class RetryScheduler {

    private final FailureRecordService failureRecordService;
    private final BookLibraryEventProducer bookLibraryEventProducer;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedRate = 10000)
    public void retryFailedRecordScheduler() {

        log.info("Retrying Failed Record Scheduler Start.");
        failureRecordService.findAllByStatus(RETRY)
                .forEach(failureRecord -> {
                    log.info("Failed Record Scheduled : {}", failureRecord);
                    try {
                        var bookLibraryEvent = objectMapper.readValue(failureRecord.getBookLibraryEvent(), BookLibraryEvent.class);
                        bookLibraryEventProducer.sendBookEvent_Async(bookLibraryEvent)
                                .whenCompleteAsync((result, throwable) -> {
                                    if (throwable == null) {
                                        failureRecord.setStatus(SUCCESS);
                                        failureRecordService.update(failureRecord);
                                        log.info("Failed Record Successfully Recovered.");
                                    } else
                                        log.error("Exception in retryFailedRecordScheduler().whenCompleteAsync() : ", throwable);
                                });


                    } catch (Exception e) {
                        log.error("Exception in retryFailedRecordScheduler() : {}", e);
                    }
                });
        log.info("Retrying Failed Record Scheduler Compelete.");
    }

}
