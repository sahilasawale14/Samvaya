package com.samvaya.repository;

import com.samvaya.model.AmenityBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface AmenityBookingRepository extends JpaRepository<AmenityBooking, Long> {
    List<AmenityBooking> findByResidentId(Long residentId);
    List<AmenityBooking> findByAmenityIdAndBookingDateAndStatusNot(Long amenityId, LocalDate bookingDate, String status);
    List<AmenityBooking> findByBookingDate(LocalDate bookingDate);
    
    @Query("SELECT b FROM AmenityBooking b WHERE b.amenity.id = :amenityId " +
           "AND b.bookingDate = :bookingDate " +
           "AND b.status <> 'CANCELLED' " +
           "AND ((b.startTime < :endTime AND b.endTime > :startTime))")
    List<AmenityBooking> findConflictingBookings(@Param("amenityId") Long amenityId,
                                                @Param("bookingDate") LocalDate bookingDate,
                                                @Param("startTime") LocalTime startTime,
                                                @Param("endTime") LocalTime endTime);
}
