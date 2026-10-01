package com.samvaya.repository;

import com.samvaya.model.AdminActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AdminActivityLogRepository extends JpaRepository<AdminActivityLog, Long> {
    List<AdminActivityLog> findAllByOrderByCreatedAtDesc();
    List<AdminActivityLog> findByModuleOrderByCreatedAtDesc(String module);
}
