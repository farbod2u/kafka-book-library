package ir.farbod.producer.repository;

import ir.farbod.producer.entity.FailureRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/***
 * @author Saeed Safaeian
 * Date : 13/05/2023
 */
public interface FailureRecordRepository extends JpaRepository<FailureRecord, Integer> {
    List<FailureRecord> findAllByStatus(String status);

    Optional<FailureRecord> findByGuid(String guid);
}
