package com.samvaya.service;

import com.samvaya.dto.BillDTO;
import com.samvaya.model.Flat;
import com.samvaya.model.MaintenanceBill;
import com.samvaya.model.Resident;
import com.samvaya.model.User;
import com.samvaya.repository.FlatRepository;
import com.samvaya.repository.MaintenanceBillRepository;
import com.samvaya.repository.PaymentRepository;
import com.samvaya.repository.ResidentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BillingEngineTest {

    @Mock
    private MaintenanceBillRepository maintenanceBillRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private ResidentRepository residentRepository;

    @Mock
    private FlatRepository flatRepository;

    @InjectMocks
    private PaymentService paymentService;

    private Flat flat1Bhk;
    private Flat flat2Bhk;
    private Resident resident1;
    private Resident resident2;

    @BeforeEach
    void setUp() {
        User u1 = User.builder().id(1L).fullName("Rahul 1BHK Resident").build();
        User u2 = User.builder().id(2L).fullName("Priya 2BHK Resident").build();

        flat1Bhk = Flat.builder()
                .id(1L)
                .wing("A")
                .flatNumber("101")
                .flatType("1BHK")
                .bhkType("1BHK")
                .carpetAreaSqFt(550.0)
                .squareFeet(550.0)
                .residentId(1L)
                .status("OCCUPIED")
                .build();

        flat2Bhk = Flat.builder()
                .id(2L)
                .wing("A")
                .flatNumber("102")
                .flatType("2BHK")
                .bhkType("2BHK")
                .carpetAreaSqFt(900.0)
                .squareFeet(900.0)
                .residentId(2L)
                .status("OCCUPIED")
                .build();

        resident1 = Resident.builder().id(1L).user(u1).flat(flat1Bhk).build();
        resident2 = Resident.builder().id(2L).user(u2).flat(flat2Bhk).build();
    }

    @Test
    @DisplayName("Verify 1 BHK vs 2 BHK: Identical Fixed Charges, Variable Area Charges Parity")
    void testStandardizedBillingCalculation() {
        when(flatRepository.findAll()).thenReturn(List.of(flat1Bhk, flat2Bhk));
        when(residentRepository.findById(1L)).thenReturn(Optional.of(resident1));
        when(residentRepository.findById(2L)).thenReturn(Optional.of(resident2));
        when(maintenanceBillRepository.findByFlatIdAndBillMonth(any(), any())).thenReturn(Optional.empty());
        when(maintenanceBillRepository.save(any(MaintenanceBill.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BigDecimal ratePerSqFt = BigDecimal.valueOf(3.50);
        List<BillDTO> bills = paymentService.generateMonthlyBills("2026-10", ratePerSqFt);

        assertEquals(2, bills.size());

        BillDTO bill1Bhk = bills.stream().filter(b -> "1BHK".equals(b.getFlatType())).findFirst().orElseThrow();
        BillDTO bill2Bhk = bills.stream().filter(b -> "2BHK".equals(b.getFlatType())).findFirst().orElseThrow();

        // 1. Both flats must have identical fixed charges: Security (1000) + Lift/Elec (800) + Sinking (500) + Admin (200) = 2500
        assertEquals(0, BigDecimal.valueOf(1000.00).compareTo(bill1Bhk.getSecurityCharge()));
        assertEquals(0, BigDecimal.valueOf(1000.00).compareTo(bill2Bhk.getSecurityCharge()));
        assertEquals(0, BigDecimal.valueOf(800.00).compareTo(bill1Bhk.getLiftElectricityCharge()));
        assertEquals(0, BigDecimal.valueOf(800.00).compareTo(bill2Bhk.getLiftElectricityCharge()));
        assertEquals(0, BigDecimal.valueOf(500.00).compareTo(bill1Bhk.getSinkingFund()));
        assertEquals(0, BigDecimal.valueOf(500.00).compareTo(bill2Bhk.getSinkingFund()));
        assertEquals(0, BigDecimal.valueOf(200.00).compareTo(bill1Bhk.getAdministrativeFee()));
        assertEquals(0, BigDecimal.valueOf(200.00).compareTo(bill2Bhk.getAdministrativeFee()));
        assertEquals(0, BigDecimal.valueOf(2500.00).compareTo(bill1Bhk.getTotalFixedCharges()));
        assertEquals(0, BigDecimal.valueOf(2500.00).compareTo(bill2Bhk.getTotalFixedCharges()));

        // 2. Variable Area Charges calculation:
        // 1 BHK: 3.50 * 550 = 1925.00
        assertEquals(0, BigDecimal.valueOf(1925.00).compareTo(bill1Bhk.getVariableAreaCharge()));
        // 2 BHK: 3.50 * 900 = 3150.00
        assertEquals(0, BigDecimal.valueOf(3150.00).compareTo(bill2Bhk.getVariableAreaCharge()));

        // 3. Total Bill = Variable + Total Fixed Charges:
        // 1 BHK: 1925 + 2500 = 4425.00
        assertEquals(0, BigDecimal.valueOf(4425.00).compareTo(bill1Bhk.getTotalAmount()));
        // 2 BHK: 3150 + 2500 = 5650.00
        assertEquals(0, BigDecimal.valueOf(5650.00).compareTo(bill2Bhk.getTotalAmount()));
    }
}
