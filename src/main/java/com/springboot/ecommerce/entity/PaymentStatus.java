package com.springboot.ecommerce.entity;

public enum PaymentStatus {
    INITIATED, PENDING, SUCCESS, FAILED,
    REFUNDED, PARTIALLY_REFUNDED, CANCELLED
}
