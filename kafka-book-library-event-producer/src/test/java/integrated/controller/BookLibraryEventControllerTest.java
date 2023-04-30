package integrated.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.farbod.producer.KafkaBookLibraryEventProducerApplication;
import ir.farbod.producer.config.AutoConfigTopic;
import ir.farbod.producer.entity.Book;
import ir.farbod.producer.entity.BookLibraryEvent;
import ir.farbod.producer.entity.LibraryEventType;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.TestPropertySource;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {KafkaBookLibraryEventProducerApplication.class, AutoConfigTopic.class})
@EmbeddedKafka(topics = "book-lib-event", partitions = 3)
@TestPropertySource(properties = {
        "spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.admin.properties.bootstrap.servers=${spring.embedded.kafka.brokers}"})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BookLibraryEventControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private EmbeddedKafkaBroker kafkaBroker;

    private ObjectMapper objectMapper;

    private Consumer<Integer, String> consumer;

    // @Value("${}")
    private String TOPIC;

    @BeforeEach
    void setUp() {
        Map<String, Object> configs = new HashMap<>(KafkaTestUtils.consumerProps("group1", "true", kafkaBroker));
        consumer = new DefaultKafkaConsumerFactory<>(configs, new IntegerDeserializer(), new StringDeserializer()).createConsumer();
        kafkaBroker.consumeFromAllEmbeddedTopics(consumer);

        objectMapper = new ObjectMapper();
    }

    @AfterEach
    void tearDown() {
        consumer.close();
    }

    @Test
    @Order(2)
    void save_async() {
        //given
        String URL = "/v1/book-lib-event/async";

        Book book = Book.builder()
                .id(123L)
                .author("Saeed")
                .name("book name")
                .build();
        var bookEventLibrary = BookLibraryEvent.builder()
                .eventId(null)
                .book(book)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<BookLibraryEvent> requestBody = new HttpEntity<>(bookEventLibrary, headers);

        //when
        ResponseEntity<BookLibraryEvent> response = restTemplate.exchange(URL, HttpMethod.POST, requestBody, BookLibraryEvent.class);

        //then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    @Order(3)
    void update_async() {
        //given
        String URL = "/v1/book-lib-event/async";

        Book book = Book.builder()
                .id(123L)
                .author("Saeed")
                .name("book name")
                .build();
        var bookEventLibrary = BookLibraryEvent.builder()
                .eventId(1)
                .book(book)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<BookLibraryEvent> requestBody = new HttpEntity<>(bookEventLibrary, headers);

        //when
        ResponseEntity<Object> response = restTemplate.exchange(URL, HttpMethod.PUT, requestBody, Object.class);

        //then
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @Order(4)
    void update_async_without_eventId() {
        //given
        String URL = "/v1/book-lib-event/async";

        Book book = Book.builder()
                .id(123L)
                .author("Saeed")
                .name("book name")
                .build();
        var bookEventLibrary = BookLibraryEvent.builder()
                .eventId(null)
                .book(book)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<BookLibraryEvent> requestBody = new HttpEntity<>(bookEventLibrary, headers);

        //when
        ResponseEntity<String> response = restTemplate.exchange(URL, HttpMethod.PUT, requestBody, String.class);

        //then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Please set eventId to valid value.", response.getBody());
    }

    @Timeout(3)
    @Test
    @Order(1)
    void save_sync() throws JsonProcessingException {
        //given
        String URL = "/v1/book-lib-event/sync";

        Book book = Book.builder()
                .id(1923L)
                .author("Saeed")
                .name("book name")
                .build();
        var bookEventLibrary = BookLibraryEvent.builder()
                .eventId(null)
                .book(book)
                .libraryEventType(LibraryEventType.NEW)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<BookLibraryEvent> requestBody = new HttpEntity<>(bookEventLibrary, headers);

        String expectedConsumerRecord = objectMapper.writeValueAsString(bookEventLibrary);

        //when
        ResponseEntity<BookLibraryEvent> response = restTemplate.exchange(URL, HttpMethod.POST, requestBody, BookLibraryEvent.class);

        ConsumerRecord<Integer, String> consumerRecord = KafkaTestUtils.getSingleRecord(consumer, "book-lib-event");
        //Thread.sleep(3000);

        //then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        assertEquals(expectedConsumerRecord, consumerRecord.value());
    }

}