package ir.farbod.consumer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.farbod.consumer.entity.BookLibraryEvent;
import ir.farbod.consumer.repository.BookLibraryEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookLibraryEventService {

    private final BookLibraryEventRepository bookLibraryEventRepository;
    private final ObjectMapper objectMapper;

    public void processEvent(ConsumerRecord<Integer, String> consumerRecord) throws JsonProcessingException {
        BookLibraryEvent bookLibraryEvent = objectMapper.readValue(consumerRecord.value(), BookLibraryEvent.class);

        switch (bookLibraryEvent.getLibraryEventType()){
            case NEW -> {
                save(bookLibraryEvent);
                break;
            }
            case UPDATE -> {}
            default -> log.info("Invalid LibraryEventType ==> {}", bookLibraryEvent.getLibraryEventType());
        }

    }

    private void save(BookLibraryEvent bookLibraryEvent) {
        bookLibraryEvent.getBook().setBookLibraryEvent(bookLibraryEvent);
        bookLibraryEventRepository.save(bookLibraryEvent);

        log.info("Successfully persisted the Book Library Event {} ", bookLibraryEvent);
    }

}
