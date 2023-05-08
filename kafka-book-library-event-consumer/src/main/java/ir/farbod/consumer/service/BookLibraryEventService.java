package ir.farbod.consumer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.farbod.consumer.entity.BookLibraryEvent;
import ir.farbod.consumer.repository.BookLibraryEventRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookLibraryEventService {

    private final BookLibraryEventRepository bookLibraryEventRepository;
    private final ObjectMapper objectMapper;

    public void processEvent(ConsumerRecord<Integer, String> consumerRecord) throws JsonProcessingException {
        BookLibraryEvent bookLibraryEvent = objectMapper.readValue(consumerRecord.value(), BookLibraryEvent.class);

        if (consumerRecord.key() == 999)
            throw new RecoverableDataAccessException("Network error");

        switch (bookLibraryEvent.getLibraryEventType()) {
            case NEW -> {
                save(bookLibraryEvent);
                break;
            }
            case UPDATE -> {
                validate(bookLibraryEvent);
                save(bookLibraryEvent);
            }
            default -> log.info("Invalid LibraryEventType ==> {}", bookLibraryEvent.getLibraryEventType());
        }

    }

    private void validate(BookLibraryEvent bookLibraryEvent) {
        if (bookLibraryEvent.getEventId() == null)
            throw new IllegalArgumentException("eventId is missing");

        BookLibraryEvent entity = null;
        try {
            entity = getByEventId(bookLibraryEvent.getEventId());
        } catch (Exception e) {
            throw new IllegalArgumentException("Not a valid eventId");
        }

        log.info("Validation is successfull for eventId : {} ", entity.getEventId());
    }

    private void save(BookLibraryEvent bookLibraryEvent) {
        bookLibraryEvent.getBook().setBookLibraryEvent(bookLibraryEvent);
        bookLibraryEventRepository.save(bookLibraryEvent);

        log.info("Successfully persisted the Book Library Event {} ", bookLibraryEvent);
    }

    public BookLibraryEvent getByEventId(Integer eventId) {
        Optional<BookLibraryEvent> res = bookLibraryEventRepository.findById(eventId);
        if (!res.isPresent())
            throw new EntityNotFoundException("BookEventLibrary not found with eventId " + eventId);

        return res.get();
    }

}
