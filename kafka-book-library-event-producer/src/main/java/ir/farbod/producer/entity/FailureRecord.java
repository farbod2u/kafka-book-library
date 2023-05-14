package ir.farbod.producer.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/***
 * @author Saeed Safaeian
 * Date : 14/05/2023
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Entity
public class FailureRecord {

    @Id
    @Column(name = "guid_value")
    private String guid;

    @Column(nullable = false, name = "topic_value")
    private String topic;

    @Column(nullable = false)
    private String bookLibraryEvent;

    @Column(nullable = false)
    private String exception_message;

    @Column(nullable = false, name = "status_value")
    private String status;
}
