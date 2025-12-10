package com.pm.billingservice.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
public class CommissionRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String ruleName;
    private String partnerTier;
    private String category;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private BigDecimal percentage;
    private BigDecimal fixedBonus;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    private Integer priority;
}
