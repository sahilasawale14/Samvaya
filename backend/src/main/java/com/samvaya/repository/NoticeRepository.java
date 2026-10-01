package com.samvaya.repository;

import com.samvaya.model.Notice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {
    List<Notice> findByIsActiveTrueOrderByPublishedDateDesc();
    List<Notice> findByCategoryAndIsActiveTrue(String category);
}
