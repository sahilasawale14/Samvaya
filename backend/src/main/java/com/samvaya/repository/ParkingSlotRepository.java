package com.samvaya.repository;

import com.samvaya.model.ParkingSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParkingSlotRepository extends JpaRepository<ParkingSlot, Long> {

    Optional<ParkingSlot> findBySlotNumber(String slotNumber);

    List<ParkingSlot> findByStatus(String status);

    List<ParkingSlot> findByFlatId(Long flatId);

    List<ParkingSlot> findByAssignedFlatId(Long assignedFlatId);

    Long countByStatus(String status);

    // Required query methods
    Long countByIsOccupiedFalse();

    Long countByIsOccupiedTrue();

    Long countBySlotTypeAndIsOccupiedFalse(String slotType);

    Long countBySlotTypeAndIsOccupiedTrue(String slotType);

    List<ParkingSlot> findByIsOccupiedFalse();

    @Query("SELECT p FROM ParkingSlot p WHERE p.isOccupied = false")
    List<ParkingSlot> listAvailableSlots();

    @Query("SELECT COUNT(p) FROM ParkingSlot p WHERE (p.slotType = :type1 OR p.slotType = :type2) AND p.isOccupied = false")
    Long countBySlotTypesAndIsOccupiedFalse(@Param("type1") String type1, @Param("type2") String type2);

    @Query("SELECT COUNT(p) FROM ParkingSlot p WHERE (p.slotType = :type1 OR p.slotType = :type2) AND p.isOccupied = true")
    Long countBySlotTypesAndIsOccupiedTrue(@Param("type1") String type1, @Param("type2") String type2);
}
