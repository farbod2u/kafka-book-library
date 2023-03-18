package ir.farbod.producer.controller;

import ir.farbod.producer.entity.Book;
import ir.farbod.producer.entity.BookLibraryEvent;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/book-lib-event")
public class BookLibraryEventController {

    @PostMapping("/async")
    public ResponseEntity<BookLibraryEvent> save_async(@RequestBody Book entity){

        var res = new BookLibraryEvent(null, entity);

        return  ResponseEntity.status(HttpStatus.CREATED)
                .body(res);
    }
}
