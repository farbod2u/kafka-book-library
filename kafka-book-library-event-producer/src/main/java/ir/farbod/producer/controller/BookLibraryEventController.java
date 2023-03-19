package ir.farbod.producer.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import ir.farbod.producer.entity.Book;
import ir.farbod.producer.entity.BookLibraryEvent;
import ir.farbod.producer.service.BookLibraryEventProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.support.SendResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/v1/book-lib-event")
@RequiredArgsConstructor
@Slf4j
public class BookLibraryEventController {

    private final BookLibraryEventProducerService bookLibraryEventProducerService;

    @PostMapping("/async")
    public ResponseEntity<BookLibraryEvent> save_async(@RequestBody Book entity) throws JsonProcessingException {

        var event = new BookLibraryEvent(1, entity);
        log.info("before send");
        bookLibraryEventProducerService.sendBookEvent_Async(event);
        log.info("after sent");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(event);
    }

    @PostMapping("/sync")
    public ResponseEntity<BookLibraryEvent> save_sync(@RequestBody Book entity) throws JsonProcessingException, ExecutionException, InterruptedException {

        var event = new BookLibraryEvent(1, entity);
        log.info("before send");
        SendResult<Integer, String> result = bookLibraryEventProducerService.sendBookEvent_Sync(event);
        log.info("after sent ==> {}", result.toString());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(event);
    }

}
