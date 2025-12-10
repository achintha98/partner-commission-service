package com.pm.billingservice.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @author Achintha Kalunayaka
 * @since 9/18/2025
 */

@Entity
public class PartnerBillingSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long partnerId;

    @Column(nullable = false)
    private LocalDate billingMonth; // YYYY-MM (stored as first day of month)

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalSales = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalCommission = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private InvoiceStatus invoiceStatus = InvoiceStatus.PENDING;

    @Column(nullable = false)
    private LocalDate generatedAt = LocalDate.now();

    // Enum for invoice status
    public enum InvoiceStatus {
        PENDING, INVOICED, PAID
    }

}
