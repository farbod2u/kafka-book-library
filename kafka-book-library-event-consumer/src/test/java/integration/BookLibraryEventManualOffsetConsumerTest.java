package integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.farbod.consumer.KafkaBookLibraryEventConsumerApplication;
import ir.farbod.consumer.consumer.BookLibraryEventManualOffsetConsumer;
import ir.farbod.consumer.entity.Book;
import ir.farbod.consumer.entity.BookLibraryEvent;
import ir.farbod.consumer.entity.LibraryEventType;
import ir.farbod.consumer.repository.BookLibraryEventRepository;
import ir.farbod.consumer.service.BookLibraryEventService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {KafkaBookLibraryEventConsumerApplication.class})
@EmbeddedKafka(topics = "book-lib-event", partitions = 3)
@TestPropertySource(properties = {
        "spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.consumer.bootstrap-servers=${spring.embedded.kafka.brokers}"
})
public class BookLibraryEventManualOffsetConsumerTest {

    // https://www.testcontainers.org/

    @Autowired
    private EmbeddedKafkaBroker kafkaBroker;

    @Autowired
    private KafkaTemplate<Integer, String> kafkaTemplate;

    @Autowired
    private KafkaListenerEndpointRegistry listenerEndpointRegistry;

    @Autowired
    private ObjectMapper objectMapper;

    @SpyBean
    private BookLibraryEventManualOffsetConsumer bookLiraryEventConsumerSpy;

    @SpyBean
    private BookLibraryEventService bookLibraryEventServiceSpy;

    @Autowired
    private BookLibraryEventRepository bookLibraryEventRepository;

    @BeforeEach
    void setup() {
        listenerEndpointRegistry.getListenerContainers()
                .forEach(messageListenerContainer -> ContainerTestUtils.waitForAssignment(messageListenerContainer, kafkaBroker.getPartitionsPerTopic()));
    }

    @AfterEach
    void tearDown() {
        bookLibraryEventRepository.deleteAll();
    }

    @Test
    void publishNewBookLibraryEvent() throws JsonProcessingException, ExecutionException, InterruptedException {
        //given
        var book = Book.builder()
                .id(123L)
                .author("Saeed")
                .name("book name")
                .build();
        var bookEventLibrary = BookLibraryEvent.builder()
                .eventId(null)
                .book(book)
                .libraryEventType(LibraryEventType.NEW)
                .build();

        String eventJson = objectMapper.writeValueAsString(bookEventLibrary);

        //when
        kafkaTemplate.sendDefault(eventJson).get();

        CountDownLatch countDownLatch = new CountDownLatch(1);
        countDownLatch.await(3, TimeUnit.SECONDS);

        //then
        verify(bookLiraryEventConsumerSpy, times(1)).onMessage(isA(ConsumerRecord.class), isA(Acknowledgment.class));
        verify(bookLibraryEventServiceSpy, times(1)).processEvent(isA(ConsumerRecord.class));

        List<BookLibraryEvent> eventList = bookLibraryEventRepository.findAll();

        assertEquals(1, eventList.size());
        eventList.forEach(bookLibraryEvent -> {
            assertNotNull(bookLibraryEvent);
            assertEquals(book.getId(), bookLibraryEvent.getBook().getId());
        });
    }

    @Test
    void publishUpdateBookLibraryEvent() throws JsonProcessingException, ExecutionException, InterruptedException {
        //given
        var book = Book.builder()
                .id(123L)
                .author("Saeed")
                .name("book name")
                .build();
        var bookEventLibrary = BookLibraryEvent.builder()
                .eventId(null)
                .book(book)
                .libraryEventType(LibraryEventType.NEW)
                .build();

        BookLibraryEvent savedEvent = bookLibraryEventRepository.save(bookEventLibrary);
        savedEvent.setLibraryEventType(LibraryEventType.UPDATE);
        savedEvent.getBook().setName("Book name #2");

        String savedEventJson = objectMapper.writeValueAsString(savedEvent);

        //when
        kafkaTemplate.sendDefault(savedEvent.getEventId(), savedEventJson).get();

        CountDownLatch countDownLatch = new CountDownLatch(1);
        countDownLatch.await(3, TimeUnit.SECONDS);

        //then
        verify(bookLiraryEventConsumerSpy, times(1)).onMessage(isA(ConsumerRecord.class), isA(Acknowledgment.class));
        verify(bookLibraryEventServiceSpy, times(1)).processEvent(isA(ConsumerRecord.class));

        BookLibraryEvent actualEvent = bookLibraryEventRepository.findById(savedEvent.getEventId()).get();
        assertEquals(savedEvent.getBook().getName(), actualEvent.getBook().getName());
    }

    @Test
    void publishUpdateBookLibraryEvent_with_null_eventId() throws JsonProcessingException, ExecutionException, InterruptedException {
        //given
        var book = Book.builder()
                .id(123L)
                .author("Saeed")
                .name("book name")
                .build();
        var bookEventLibrary = BookLibraryEvent.builder()
                .eventId(null)
                .book(book)
                .libraryEventType(LibraryEventType.UPDATE)
                .build();

        String bookEventLibraryJson = objectMapper.writeValueAsString(bookEventLibrary);

        //when
        kafkaTemplate.sendDefault(bookEventLibrary.getEventId(), bookEventLibraryJson).get();

        CountDownLatch countDownLatch = new CountDownLatch(1);
        countDownLatch.await(5, TimeUnit.SECONDS);

        //then
        verify(bookLiraryEventConsumerSpy, times(10)).onMessage(isA(ConsumerRecord.class), isA(Acknowledgment.class));
        verify(bookLibraryEventServiceSpy, times(10)).processEvent(isA(ConsumerRecord.class));

       // assertThrows(IllegalArgumentException.class, () -> bookLibraryEventServiceSpy.processEvent(isA(ConsumerRecord.class)));

    }

    @Test
    void publishUpdateBookLibraryEvent_with_invalid_eventId() throws JsonProcessingException, ExecutionException, InterruptedException {
        //given
        var book = Book.builder()
                .id(123L)
                .author("Saeed")
                .name("book name")
                .build();
        var bookEventLibrary = BookLibraryEvent.builder()
                .eventId(1)
                .book(book)
                .libraryEventType(LibraryEventType.UPDATE)
                .build();

        String bookEventLibraryJson = objectMapper.writeValueAsString(bookEventLibrary);

        //when
        kafkaTemplate.sendDefault(bookEventLibrary.getEventId(), bookEventLibraryJson).get();

        CountDownLatch countDownLatch = new CountDownLatch(1);
        countDownLatch.await(5, TimeUnit.SECONDS);

        //then
        verify(bookLiraryEventConsumerSpy, times(10)).onMessage(isA(ConsumerRecord.class), isA(Acknowledgment.class));
        verify(bookLibraryEventServiceSpy, times(10)).processEvent(isA(ConsumerRecord.class));

    }


}