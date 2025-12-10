package com.pm.billingservice.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

/**
 * @author Achintha Kalunayaka
 * @since 11/9/2025
 */

@Entity
@Data
@RequiredArgsConstructor
public class CommissionPartialAggregate {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    // Use a simple data type for the foreign key reference
    @Column(nullable = false)
    private Long partnerId; // <--- Just the ID!

    @Column(nullable = false)
    private Long jobExecutionId;

    @Column(nullable = false)
    private Long stepExecutionId;

    @Column(nullable = false)
    private BigDecimal commissionAmount;
}
