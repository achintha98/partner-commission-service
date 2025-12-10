package com.pm.billingservice.model;

import com.pm.billingservice.model.enums.InvoiceStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @author Achintha Kalunayaka
 * @since 10/9/2025
 */

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @NotNull
    @Column(unique = true)
    private String invoiceNumber;  // e.g. INV-2025-10-0001 (UUID or formatted)

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_id")
    private Partner partner;

    @NotNull
    private LocalDate invoiceDate;

    @NotNull
    private LocalDate startPeriod;  // e.g. 2025-09-01

    @NotNull
    private LocalDate endPeriod;    // e.g. 2025-09-30

    @NotNull
    private BigDecimal totalSales;  // total sales for that month

    @NotNull
    private BigDecimal commission;  // calculated commission amount

    private String currency; // e.g. "USD" or "LKR"

    @Enumerated(EnumType.STRING)
    private InvoiceStatus status; // e.g. GENERATED, SENT, PAID, FAILED

    private String pdfUrl; // optional - S3 file location

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;


}
