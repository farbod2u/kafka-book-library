package ir.farbod.producer.entity;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class BookLibraryEvent {

    private Integer eventId;
    @NotNull
    @Valid
    private Book book;
    private LibraryEventType libraryEventType;
    private String guid;
}
