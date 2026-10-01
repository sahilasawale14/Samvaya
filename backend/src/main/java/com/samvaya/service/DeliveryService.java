package com.samvaya.service;

import com.samvaya.dto.DeliveryDTO;
import com.samvaya.exception.ResourceNotFoundException;
import com.samvaya.model.Delivery;
import com.samvaya.model.Flat;
import com.samvaya.model.Resident;
import com.samvaya.repository.DeliveryRepository;
import com.samvaya.repository.FlatRepository;
import com.samvaya.repository.ResidentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final ResidentRepository residentRepository;
    private final FlatRepository flatRepository;

    @Transactional(readOnly = true)
    public List<DeliveryDTO> getAllDeliveries() {
        return deliveryRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DeliveryDTO> getDeliveriesForResident(Long residentId) {
        return deliveryRepository.findByResidentId(residentId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public DeliveryDTO createExpectedDelivery(DeliveryDTO dto) {
        Resident resident = null;
        if (dto.getResidentId() != null) {
            resident = residentRepository.findById(dto.getResidentId()).orElse(null);
        }
        if (resident == null) {
            resident = residentRepository.findAll().stream().findFirst().orElse(null);
        }
        if (resident == null) {
            throw new ResourceNotFoundException("Resident not found");
        }

        Flat flat = resident.getFlat();

        Delivery delivery = Delivery.builder()
                .company(dto.getCompany())
                .deliveryPersonName(dto.getDeliveryPersonName())
                .phone(dto.getPhone())
                .flat(flat)
                .resident(resident)
                .referenceNumber(dto.getReferenceNumber())
                .vehicleNumber(dto.getVehicleNumber())
                .isExpected(true)
                .status("EXPECTED")
                .approvalStatus("APPROVED")
                .notes(dto.getNotes())
                .build();

        return mapToDTO(deliveryRepository.save(delivery));
    }

    @Transactional
    public DeliveryDTO recordArrival(Long deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found"));

        delivery.setStatus("ARRIVED");
        delivery.setArrivedAt(LocalDateTime.now());
        return mapToDTO(deliveryRepository.save(delivery));
    }

    @Transactional
    public DeliveryDTO completeDelivery(Long deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found"));

        delivery.setStatus("COMPLETED");
        delivery.setCompletedAt(LocalDateTime.now());
        return mapToDTO(deliveryRepository.save(delivery));
    }

    private DeliveryDTO mapToDTO(Delivery d) {
        return DeliveryDTO.builder()
                .id(d.getId())
                .company(d.getCompany())
                .deliveryPersonName(d.getDeliveryPersonName())
                .phone(d.getPhone())
                .flatId(d.getFlat() != null ? d.getFlat().getId() : null)
                .wing(d.getFlat() != null ? d.getFlat().getWing() : "")
                .flatNumber(d.getFlat() != null ? d.getFlat().getFlatNumber() : "")
                .residentId(d.getResident() != null ? d.getResident().getId() : null)
                .residentName(d.getResident() != null && d.getResident().getUser() != null ? d.getResident().getUser().getFullName() : "N/A")
                .referenceNumber(d.getReferenceNumber())
                .vehicleNumber(d.getVehicleNumber())
                .isExpected(d.getIsExpected())
                .status(d.getStatus())
                .approvalStatus(d.getApprovalStatus())
                .arrivedAt(d.getArrivedAt() != null ? d.getArrivedAt().format(DateTimeFormatter.ofPattern("dd MMM, hh:mm a")) : null)
                .completedAt(d.getCompletedAt() != null ? d.getCompletedAt().format(DateTimeFormatter.ofPattern("dd MMM, hh:mm a")) : null)
                .notes(d.getNotes())
                .build();
    }
}
