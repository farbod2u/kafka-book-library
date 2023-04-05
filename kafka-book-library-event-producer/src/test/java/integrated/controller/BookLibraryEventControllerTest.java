package integrated.controller;

import ir.farbod.producer.KafkaBookLibraryEventProducerApplication;
import ir.farbod.producer.config.AutoConfigTopic;
import ir.farbod.producer.entity.Book;
import ir.farbod.producer.entity.BookLibraryEvent;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {KafkaBookLibraryEventProducerApplication.class, AutoConfigTopic.class})
@EmbeddedKafka(topics = "book-lib-event", partitions = 3)
@TestPropertySource(properties = {
        "spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.admin.properties.bootstrap.servers=${spring.embedded.kafka.brokers}"})
class BookLibraryEventControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private EmbeddedKafkaBroker kafkaBroker;

    private Consumer<Integer, String> consumer;

    @BeforeEach
    void setUp() {
        Map<String, Object> configs = new HashMap<>(KafkaTestUtils.consumerProps("group1", "true", kafkaBroker));

        consumer = new DefaultKafkaConsumerFactory<>(configs, new IntegerDeserializer(), new StringDeserializer()).createConsumer();
        kafkaBroker.consumeFromAllEmbeddedTopics(consumer);
    }

    @AfterEach
    void tearDown() {
        consumer.close();
    }

    @Test
    @Timeout(3)
    void save_async() throws InterruptedException {
        //given
        String URL = "/v1/book-lib-event/async";

        Book book = Book.builder()
                .id(123L)
                .author("Saeed")
                .name("book name").build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Book> requestBody = new HttpEntity<>(book, headers);

        String expectedConsumerRecord = "{\"id\":123,\"name\":\"book name\",\"author\":\"Saeed\"}";

        //when
        ResponseEntity<BookLibraryEvent> response = restTemplate.exchange(URL, HttpMethod.POST, requestBody, BookLibraryEvent.class);

        ConsumerRecord<Integer, String> consumerRecord = KafkaTestUtils.getSingleRecord(consumer, "book-lib-event");
        //Thread.sleep(3000);

        //then
        assertEquals(response.getStatusCode(), HttpStatus.CREATED);

        assertEquals(consumerRecord.value(), expectedConsumerRecord);
    }

    @Test
    void save_sync() {
    }
}