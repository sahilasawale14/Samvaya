package com.samvaya.repository;

import com.samvaya.model.StaffMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StaffMemberRepository extends JpaRepository<StaffMember, Long> {
    List<StaffMember> findByStatus(String status);
    List<StaffMember> findByDesignation(String designation);
    Long countByStatus(String status);
}
