package unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.farbod.producer.KafkaBookLibraryEventProducerApplication;
import ir.farbod.producer.controller.BookLibraryEventController;
import ir.farbod.producer.entity.Book;
import ir.farbod.producer.entity.BookLibraryEvent;
import ir.farbod.producer.producer.BookLibraryEventProducer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookLibraryEventController.class)
@AutoConfigureMockMvc
@ContextConfiguration(classes = {KafkaBookLibraryEventProducerApplication.class})
class BookLibraryEventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookLibraryEventProducer bookLibraryEventProducer;

    @Test
    void save_async() throws Exception {
        //given
        String URL = "/v1/book-lib-event/async";

        Book book = Book.builder()
                .id(123L)
                .author("Saeed")
                .name("book name").build();
        BookLibraryEvent bookLibraryEvent = BookLibraryEvent.builder()
                .eventId(null)
                .book(book)
                .build();

        String body = objectMapper.writeValueAsString(bookLibraryEvent);

        when(bookLibraryEventProducer.sendBookEvent_Async(isA(BookLibraryEvent.class))).thenReturn(null);

        //when
        //then
        mockMvc.perform(post(URL)
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    void save_async_error_4xx() throws Exception {
        //given
        String URL = "/v1/book-lib-event/async";

        BookLibraryEvent bookLibraryEvent = BookLibraryEvent.builder()
                .eventId(null)
                .book(new Book())
                .build();

        String body = objectMapper.writeValueAsString(bookLibraryEvent);

//        doNothing().when(bookLibraryEventProducerService).sendBookEvent_Async(isA(BookLibraryEvent.class));
        when(bookLibraryEventProducer.sendBookEvent_Async(isA(BookLibraryEvent.class))).thenReturn(null);

        //when
        //then
        mockMvc.perform(post(URL)
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string(containsString("must not be")));
    }


    @Test
    void update_async() throws Exception {
        //given
        String URL = "/v1/book-lib-event/async";

        Book book = Book.builder()
                .id(123L)
                .author("Saeed")
                .name("book name").build();
        BookLibraryEvent bookLibraryEvent = BookLibraryEvent.builder()
                .eventId(1)
                .book(book)
                .build();

        String body = objectMapper.writeValueAsString(bookLibraryEvent);

        when(bookLibraryEventProducer.sendBookEvent_Async(isA(BookLibraryEvent.class))).thenReturn(null);

        //when
        //then
        mockMvc.perform(put(URL)
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
    @Test
    void update_async_error_4xx() throws Exception {
        //given
        String URL = "/v1/book-lib-event/async";

        Book book = Book.builder()
                .id(123L)
                .author("Saeed")
                .name("book name").build();
        BookLibraryEvent bookLibraryEvent = BookLibraryEvent.builder()
                .eventId(null)
                .book(book)
                .build();

        String body = objectMapper.writeValueAsString(bookLibraryEvent);

        when(bookLibraryEventProducer.sendBookEvent_Async(isA(BookLibraryEvent.class))).thenReturn(null);

        //when
        //then
        mockMvc.perform(put(URL)
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string(containsString("Please set eventId to valid value.")));
    }



    @Test
    void save_sync() {
    }
}