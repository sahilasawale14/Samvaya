package com.samvaya.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDTO {
    private Long id;
    private Long billId;
    private String billMonth;
    private Long residentId;
    private String residentName;
    private String flatNumber;
    private BigDecimal amountPaid;
    private String paymentMode;
    private String transactionReference;
    private LocalDateTime paymentDate;
    private String status;
}
