package com.samvaya.repository;

import com.samvaya.model.TemporaryWorker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TemporaryWorkerRepository extends JpaRepository<TemporaryWorker, Long> {
    List<TemporaryWorker> findByStatus(String status);
    Long countByStatus(String status);
}
