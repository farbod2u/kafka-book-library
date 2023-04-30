package ir.farbod.producer.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import ir.farbod.producer.entity.BookLibraryEvent;
import ir.farbod.producer.entity.LibraryEventType;
import ir.farbod.producer.service.BookLibraryEventProducerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.support.SendResult;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/v1/book-lib-event")
@RequiredArgsConstructor
@Slf4j
public class BookLibraryEventController {

    private final BookLibraryEventProducerService bookLibraryEventProducerService;

    @PostMapping("/async")
    public ResponseEntity<BookLibraryEvent> save_async(@Valid @RequestBody BookLibraryEvent bookLibraryEvent) throws JsonProcessingException {

        bookLibraryEvent.setLibraryEventType(LibraryEventType.NEW);
        log.info("before send");
        bookLibraryEventProducerService.sendBookEvent_Async(bookLibraryEvent);
        log.info("after sent");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bookLibraryEvent);
    }

    @PutMapping("/async")
    public ResponseEntity<?> update_async(@Valid @RequestBody BookLibraryEvent bookLibraryEvent) throws JsonProcessingException {

        if (bookLibraryEvent.getEventId() == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Please set eventId to valid value.");

        bookLibraryEvent.setLibraryEventType(LibraryEventType.UPDATE);
        bookLibraryEventProducerService.sendBookEvent_Async(bookLibraryEvent);
        return ResponseEntity.status(HttpStatus.OK)
                .body(bookLibraryEvent);
    }

    @PostMapping("/sync")
    public ResponseEntity<BookLibraryEvent> save_sync(@RequestBody BookLibraryEvent bookLibraryEvent) throws JsonProcessingException, ExecutionException, InterruptedException {

        bookLibraryEvent.setLibraryEventType(LibraryEventType.NEW);
        log.info("before send");
        SendResult<Integer, String> result = bookLibraryEventProducerService.sendBookEvent_Sync(bookLibraryEvent);
        log.info("after sent ==> {}", result.toString());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bookLibraryEvent);
    }

}
