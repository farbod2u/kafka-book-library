package ir.farbod.consumer.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Entity
public class BookLibraryEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer eventId;

    @OneToOne(mappedBy = "bookLibraryEvent", cascade = CascadeType.ALL)
    private Book book;

    @Enumerated(EnumType.STRING)
    private LibraryEventType libraryEventType;

}
