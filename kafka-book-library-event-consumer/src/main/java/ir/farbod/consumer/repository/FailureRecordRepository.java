package ir.farbod.consumer.repository;

import ir.farbod.consumer.entity.FailureRecord;
import org.springframework.data.jpa.repository.JpaRepository;

/***
 * @author Saeed Safaeian
 * Date : 13/05/2023
 */
public interface FailureRecordRepository extends JpaRepository<FailureRecord, Integer> {
}
