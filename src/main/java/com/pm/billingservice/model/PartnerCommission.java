package com.pm.billingservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author Achintha Kalunayaka
 * @since 9/18/2025
 */

@Entity
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartnerCommission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long partnerId;

    @Column(nullable = false)
    private Long transactionId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal saleAmount;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal commissionAmount;

    @Column(length = 255)
    private String appliedRuleIds; // e.g., "1,2,10"

    @Column(nullable = false)
    private LocalDateTime saleDate;

    @Column(nullable = false)
    private LocalDateTime calculatedAt = LocalDateTime.now();



}
