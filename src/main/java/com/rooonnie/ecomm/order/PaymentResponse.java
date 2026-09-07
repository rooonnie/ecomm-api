package com.rooonnie.ecomm.order;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentResponse(Long id, String provider, BigDecimal amount, PaymentStatus status, Instant paidAt) {
}
