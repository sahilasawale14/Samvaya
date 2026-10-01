package com.samvaya.service;

import com.samvaya.dto.BillDTO;
import com.samvaya.dto.PaymentDTO;
import com.samvaya.exception.BadRequestException;
import com.samvaya.exception.ResourceNotFoundException;
import com.samvaya.model.Flat;
import com.samvaya.model.MaintenanceBill;
import com.samvaya.model.Payment;
import com.samvaya.model.Resident;
import com.samvaya.repository.FlatRepository;
import com.samvaya.repository.MaintenanceBillRepository;
import com.samvaya.repository.PaymentRepository;
import com.samvaya.repository.ResidentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {

    public static final BigDecimal FIXED_SECURITY_CHARGE = BigDecimal.valueOf(1000.00);
    public static final BigDecimal FIXED_LIFT_ELECTRICITY_CHARGE = BigDecimal.valueOf(800.00);
    public static final BigDecimal FIXED_SINKING_FUND = BigDecimal.valueOf(500.00);
    public static final BigDecimal FIXED_ADMINISTRATIVE_FEE = BigDecimal.valueOf(200.00);
    public static final BigDecimal TOTAL_FIXED_CHARGES = FIXED_SECURITY_CHARGE
            .add(FIXED_LIFT_ELECTRICITY_CHARGE)
            .add(FIXED_SINKING_FUND)
            .add(FIXED_ADMINISTRATIVE_FEE); // Total = ₹2,500.00

    private final MaintenanceBillRepository maintenanceBillRepository;
    private final PaymentRepository paymentRepository;
    private final ResidentRepository residentRepository;
    private final FlatRepository flatRepository;

    @Transactional(readOnly = true)
    public List<BillDTO> getAllBills() {
        return maintenanceBillRepository.findAll().stream()
                .map(this::mapToBillDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BillDTO> getBillsForResident(Long residentId) {
        return maintenanceBillRepository.findByResidentId(residentId).stream()
                .map(this::mapToBillDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BillDTO> getBillsForFlat(Long flatId) {
        return maintenanceBillRepository.findByFlatId(flatId).stream()
                .map(this::mapToBillDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BillDTO> getMyBills(Long flatId, Long residentId, Long userId) {
        if (flatId != null) {
            return getBillsForFlat(flatId);
        }
        if (residentId != null) {
            return getBillsForResident(residentId);
        }
        if (userId != null) {
            Resident resident = residentRepository.findByUserId(userId).orElse(null);
            if (resident != null) {
                if (resident.getFlat() != null) {
                    return getBillsForFlat(resident.getFlat().getId());
                }
                return getBillsForResident(resident.getId());
            }
        }
        return getAllBills();
    }

    @Transactional(readOnly = true)
    public List<PaymentDTO> getAllPayments() {
        return paymentRepository.findAll().stream()
                .map(this::mapToPaymentDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PaymentDTO> getPaymentsForResident(Long residentId) {
        return paymentRepository.findByResidentId(residentId).stream()
                .map(this::mapToPaymentDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<BillDTO> generateMonthlyBills(String month, BigDecimal ratePerSqFt) {
        String billingMonth = (month != null && !month.trim().isEmpty()) 
                ? month.trim() 
                : YearMonth.now().toString(); // e.g. "2026-10"

        BigDecimal rate = (ratePerSqFt != null && ratePerSqFt.compareTo(BigDecimal.ZERO) > 0)
                ? ratePerSqFt
                : BigDecimal.valueOf(3.50);

        LocalDate dueDate;
        try {
            YearMonth ym = YearMonth.parse(billingMonth.length() == 7 ? billingMonth : billingMonth.substring(0, 7));
            dueDate = ym.atDay(25);
        } catch (Exception e) {
            dueDate = LocalDate.now().plusDays(20);
        }

        List<Flat> flats = flatRepository.findAll();
        List<MaintenanceBill> generatedBills = new ArrayList<>();

        for (Flat flat : flats) {
            Resident resident = null;
            if (flat.getResidentId() != null) {
                resident = residentRepository.findById(flat.getResidentId()).orElse(null);
            }
            if (resident == null) {
                List<Resident> activeResidents = residentRepository.findByFlatIdAndStatus(flat.getId(), "ACTIVE");
                if (!activeResidents.isEmpty()) {
                    resident = activeResidents.get(0);
                }
            }

            // Only generate bill for occupied units with active residents
            if (resident == null) {
                continue;
            }

            Double carpetArea = (flat.getCarpetAreaSqFt() != null && flat.getCarpetAreaSqFt() > 0)
                    ? flat.getCarpetAreaSqFt()
                    : (flat.getSquareFeet() != null ? flat.getSquareFeet() : 900.0);

            String flatType = (flat.getFlatType() != null && !flat.getFlatType().isEmpty())
                    ? flat.getFlatType()
                    : (flat.getBhkType() != null ? flat.getBhkType() : "2BHK");

            BigDecimal variableAreaCharge = rate.multiply(BigDecimal.valueOf(carpetArea)).setScale(2, RoundingMode.HALF_UP);
            BigDecimal totalAmount = variableAreaCharge.add(TOTAL_FIXED_CHARGES);

            // Check if bill for this flat and month already exists
            Optional<MaintenanceBill> existing = maintenanceBillRepository.findByFlatIdAndBillMonth(flat.getId(), billingMonth);
            MaintenanceBill bill;

            if (existing.isPresent()) {
                bill = existing.get();
                if ("PAID".equalsIgnoreCase(bill.getStatus())) {
                    generatedBills.add(bill);
                    continue; // Do not overwrite already paid bill
                }
                bill.setResident(resident);
                bill.setFlatType(flatType);
                bill.setCarpetAreaSqFt(carpetArea);
                bill.setRatePerSqFt(rate);
                bill.setVariableAreaCharge(variableAreaCharge);
                bill.setSecurityCharge(FIXED_SECURITY_CHARGE);
                bill.setLiftElectricityCharge(FIXED_LIFT_ELECTRICITY_CHARGE);
                bill.setSinkingFund(FIXED_SINKING_FUND);
                bill.setAdministrativeFee(FIXED_ADMINISTRATIVE_FEE);
                bill.setTotalFixedCharges(TOTAL_FIXED_CHARGES);
                bill.setMaintenanceCharge(variableAreaCharge);
                bill.setTotalAmount(totalAmount);
                bill.setDueDate(dueDate);
            } else {
                bill = MaintenanceBill.builder()
                        .flat(flat)
                        .resident(resident)
                        .billMonth(billingMonth)
                        .flatType(flatType)
                        .carpetAreaSqFt(carpetArea)
                        .ratePerSqFt(rate)
                        .variableAreaCharge(variableAreaCharge)
                        .securityCharge(FIXED_SECURITY_CHARGE)
                        .liftElectricityCharge(FIXED_LIFT_ELECTRICITY_CHARGE)
                        .sinkingFund(FIXED_SINKING_FUND)
                        .administrativeFee(FIXED_ADMINISTRATIVE_FEE)
                        .totalFixedCharges(TOTAL_FIXED_CHARGES)
                        .maintenanceCharge(variableAreaCharge)
                        .waterCharge(BigDecimal.ZERO)
                        .parkingCharge(BigDecimal.ZERO)
                        .penaltyCharge(BigDecimal.ZERO)
                        .totalAmount(totalAmount)
                        .dueDate(dueDate)
                        .status("PENDING")
                        .build();
            }

            MaintenanceBill saved = maintenanceBillRepository.save(bill);
            generatedBills.add(saved);
        }

        return generatedBills.stream()
                .map(this::mapToBillDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public PaymentDTO payBill(Long billId, Long residentId, String paymentMode) {
        MaintenanceBill bill = maintenanceBillRepository.findById(billId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found"));

        if ("PAID".equals(bill.getStatus())) {
            throw new BadRequestException("This bill has already been paid");
        }

        Resident resident = residentRepository.findById(residentId)
                .orElseThrow(() -> new ResourceNotFoundException("Resident not found"));

        String txnRef = "UPI/" + LocalDate.now().toString().replace("-", "") + "/SAMVAYA/" + (100000 + (int)(Math.random() * 900000));

        Payment payment = Payment.builder()
                .bill(bill)
                .resident(resident)
                .amountPaid(bill.getTotalAmount())
                .paymentMode(paymentMode != null ? paymentMode : "UPI")
                .transactionReference(txnRef)
                .paymentDate(LocalDateTime.now())
                .status("COMPLETED")
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        bill.setStatus("PAID");
        maintenanceBillRepository.save(bill);

        return mapToPaymentDTO(savedPayment);
    }

    public BillDTO mapToBillDTO(MaintenanceBill b) {
        String flatType = b.getFlatType();
        Double carpetArea = b.getCarpetAreaSqFt();

        if (flatType == null && b.getFlat() != null) {
            flatType = b.getFlat().getFlatType() != null ? b.getFlat().getFlatType() : b.getFlat().getBhkType();
        }
        if (carpetArea == null && b.getFlat() != null) {
            carpetArea = b.getFlat().getCarpetAreaSqFt() != null ? b.getFlat().getCarpetAreaSqFt() : b.getFlat().getSquareFeet();
        }
        if (carpetArea == null) carpetArea = 900.0;
        if (flatType == null) flatType = "2BHK";

        BigDecimal rate = b.getRatePerSqFt() != null ? b.getRatePerSqFt() : BigDecimal.valueOf(3.50);
        BigDecimal variable = b.getVariableAreaCharge() != null 
                ? b.getVariableAreaCharge() 
                : rate.multiply(BigDecimal.valueOf(carpetArea)).setScale(2, RoundingMode.HALF_UP);

        BigDecimal secCharge = b.getSecurityCharge() != null ? b.getSecurityCharge() : FIXED_SECURITY_CHARGE;
        BigDecimal liftCharge = b.getLiftElectricityCharge() != null ? b.getLiftElectricityCharge() : FIXED_LIFT_ELECTRICITY_CHARGE;
        BigDecimal sinkCharge = b.getSinkingFund() != null ? b.getSinkingFund() : FIXED_SINKING_FUND;
        BigDecimal adminCharge = b.getAdministrativeFee() != null ? b.getAdministrativeFee() : FIXED_ADMINISTRATIVE_FEE;
        BigDecimal totalFixed = b.getTotalFixedCharges() != null 
                ? b.getTotalFixedCharges() 
                : secCharge.add(liftCharge).add(sinkCharge).add(adminCharge);

        return BillDTO.builder()
                .id(b.getId())
                .flatId(b.getFlat() != null ? b.getFlat().getId() : null)
                .wing(b.getFlat() != null ? b.getFlat().getWing() : "")
                .flatNumber(b.getFlat() != null ? b.getFlat().getFlatNumber() : "")
                .flatType(flatType)
                .carpetAreaSqFt(carpetArea)
                .residentId(b.getResident() != null ? b.getResident().getId() : null)
                .residentName(b.getResident() != null && b.getResident().getUser() != null ? b.getResident().getUser().getFullName() : "N/A")
                .billMonth(b.getBillMonth())
                .ratePerSqFt(rate)
                .variableAreaCharge(variable)
                .securityCharge(secCharge)
                .liftElectricityCharge(liftCharge)
                .sinkingFund(sinkCharge)
                .administrativeFee(adminCharge)
                .totalFixedCharges(totalFixed)
                .maintenanceCharge(b.getMaintenanceCharge() != null ? b.getMaintenanceCharge() : variable)
                .waterCharge(b.getWaterCharge() != null ? b.getWaterCharge() : BigDecimal.ZERO)
                .parkingCharge(b.getParkingCharge() != null ? b.getParkingCharge() : BigDecimal.ZERO)
                .penaltyCharge(b.getPenaltyCharge() != null ? b.getPenaltyCharge() : BigDecimal.ZERO)
                .totalAmount(b.getTotalAmount())
                .dueDate(b.getDueDate())
                .status(b.getStatus())
                .build();
    }

    private PaymentDTO mapToPaymentDTO(Payment p) {
        return PaymentDTO.builder()
                .id(p.getId())
                .billId(p.getBill() != null ? p.getBill().getId() : null)
                .billMonth(p.getBill() != null ? p.getBill().getBillMonth() : "")
                .residentId(p.getResident() != null ? p.getResident().getId() : null)
                .residentName(p.getResident() != null && p.getResident().getUser() != null ? p.getResident().getUser().getFullName() : "N/A")
                .flatNumber(p.getResident() != null && p.getResident().getFlat() != null ? p.getResident().getFlat().getFlatNumber() : "")
                .amountPaid(p.getAmountPaid())
                .paymentMode(p.getPaymentMode())
                .transactionReference(p.getTransactionReference())
                .paymentDate(p.getPaymentDate())
                .status(p.getStatus())
                .build();
    }
}
