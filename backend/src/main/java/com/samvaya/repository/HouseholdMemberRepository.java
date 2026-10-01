package com.samvaya.repository;

import com.samvaya.model.HouseholdMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface HouseholdMemberRepository extends JpaRepository<HouseholdMember, Long> {
    List<HouseholdMember> findByResidentId(Long residentId);
}
