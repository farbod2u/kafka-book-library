package unit.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.farbod.producer.entity.Book;
import ir.farbod.producer.entity.BookLibraryEvent;
import ir.farbod.producer.producer.BookLibraryEventProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookLibraryEventProducerTest {

    @Mock
    private KafkaTemplate<Integer, String> kafkaTemplate;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private BookLibraryEventProducer bookLibraryEventProducer;

    private String TOPIC = "book-lib-event";

    @Test
    void sendBookEvent_Async_with_exception() throws JsonProcessingException {
        //given
        Book book = Book.builder()
                .id(123L)
                .author("Saeed")
                .name("book name").build();
        BookLibraryEvent bookLibraryEvent = BookLibraryEvent.builder()
                .eventId(null)
                .book(book)
                .build();

        ReflectionTestUtils.setField(bookLibraryEventProducer, "TOPIC", TOPIC);

        CompletableFuture<SendResult<Integer, String>> result = CompletableFuture.failedFuture(new RuntimeException("fail on kafka"));
        when(kafkaTemplate.send(isA(ProducerRecord.class))).thenReturn(result);

        //when
        CompletableFuture<SendResult<Integer, String>> actualResult = bookLibraryEventProducer.sendBookEvent_Async(bookLibraryEvent);

        //then
        assertThrows(Exception.class, () -> actualResult.get());

    }

    @Test
    void sendBookEvent_Async_Success() throws JsonProcessingException, ExecutionException, InterruptedException {
        //given
        Book book = Book.builder()
                .id(123L)
                .author("Saeed")
                .name("book name").build();
        BookLibraryEvent bookLibraryEvent = BookLibraryEvent.builder()
                .eventId(null)
                .book(book)
                .build();

        ReflectionTestUtils.setField(bookLibraryEventProducer, "TOPIC", TOPIC);

        String eventString = objectMapper.writeValueAsString(bookLibraryEvent);
        ProducerRecord<Integer, String> producerRecord = new ProducerRecord<>(TOPIC, bookLibraryEvent.getEventId(), eventString);
        int partition = 1;
        RecordMetadata recordMetadata = new RecordMetadata(new TopicPartition(TOPIC, partition),
                1, 1, 34, System.currentTimeMillis(), 1,2 );
        SendResult<Integer, String> sendResult = new SendResult<>(producerRecord, recordMetadata);
        CompletableFuture<SendResult<Integer, String>> result = CompletableFuture.completedFuture(sendResult);
        when(kafkaTemplate.send(isA(ProducerRecord.class))).thenReturn(result);

        //when
        SendResult<Integer, String> actualResult = bookLibraryEventProducer.sendBookEvent_Async(bookLibraryEvent).get();

        //then
        assertEquals(eventString, actualResult.getProducerRecord().value());
        assertEquals(partition, actualResult.getRecordMetadata().partition());

    }

    @Test
    void sendBookEvent_Sync() {
    }
}