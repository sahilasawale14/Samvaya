package com.samvaya.repository;

import com.samvaya.model.Flat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface FlatRepository extends JpaRepository<Flat, Long> {
    Optional<Flat> findByWingAndFlatNumber(String wing, String flatNumber);
    Optional<Flat> findByFlatNumber(String flatNumber);
    List<Flat> findByWing(String wing);
    List<Flat> findByStatus(String status);
    Long countByStatus(String status);
}
