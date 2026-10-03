package com.samvaya.repository;

import com.samvaya.model.VisitorEntryExit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface VisitorEntryExitRepository extends JpaRepository<VisitorEntryExit, Long> {
    Optional<VisitorEntryExit> findTopByVisitorIdOrderByEntryTimeDesc(Long visitorId);
    List<VisitorEntryExit> findByVisitorId(Long visitorId);
    void deleteByVisitorId(Long visitorId);
    List<VisitorEntryExit> findByExitTimeIsNull();
}
