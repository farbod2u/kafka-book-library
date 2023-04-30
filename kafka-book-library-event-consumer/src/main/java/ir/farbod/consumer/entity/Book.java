package ir.farbod.consumer.entity;

import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Entity
public class Book {

    @Id
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String author;

    @OneToOne
    @JoinColumn(name = "eventId")
    @ToString.Exclude
    private BookLibraryEvent bookLibraryEvent;

}
