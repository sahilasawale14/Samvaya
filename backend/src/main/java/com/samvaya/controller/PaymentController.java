package com.samvaya.controller;

import com.samvaya.dto.ApiResponse;
import com.samvaya.dto.BillDTO;
import com.samvaya.dto.PaymentDTO;
import com.samvaya.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/bills")
    public ResponseEntity<ApiResponse<List<BillDTO>>> getBills(@RequestParam(required = false) Long residentId) {
        List<BillDTO> bills = (residentId != null)
                ? paymentService.getBillsForResident(residentId)
                : paymentService.getAllBills();
        return ResponseEntity.ok(ApiResponse.success(bills));
    }

    @PostMapping("/bills/generate-monthly")
    public ResponseEntity<ApiResponse<List<BillDTO>>> generateMonthlyBills(
            @RequestParam(required = false) String month,
            @RequestParam(required = false, defaultValue = "3.50") BigDecimal ratePerSqFt) {
        List<BillDTO> generated = paymentService.generateMonthlyBills(month, ratePerSqFt);
        return ResponseEntity.ok(ApiResponse.success("Monthly maintenance bills generated successfully", generated));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PaymentDTO>>> getPayments(@RequestParam(required = false) Long residentId) {
        List<PaymentDTO> payments = (residentId != null)
                ? paymentService.getPaymentsForResident(residentId)
                : paymentService.getAllPayments();
        return ResponseEntity.ok(ApiResponse.success(payments));
    }

    @PostMapping("/pay")
    public ResponseEntity<ApiResponse<PaymentDTO>> payBill(@RequestParam Long billId,
                                                           @RequestParam Long residentId,
                                                           @RequestParam(required = false, defaultValue = "UPI") String paymentMode) {
        PaymentDTO payment = paymentService.payBill(billId, residentId, paymentMode);
        return ResponseEntity.ok(ApiResponse.success("Payment processed successfully", payment));
    }
}
