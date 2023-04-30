package ir.farbod.consumer.repository;

import ir.farbod.consumer.entity.BookLibraryEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookLibraryEventRepository  extends JpaRepository<BookLibraryEvent, Integer> {
}
