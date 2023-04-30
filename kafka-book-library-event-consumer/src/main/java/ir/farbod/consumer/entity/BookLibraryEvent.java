package ir.farbod.consumer.entity;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
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

}
