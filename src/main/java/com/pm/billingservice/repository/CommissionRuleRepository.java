package com.pm.billingservice.repository;

import com.pm.billingservice.model.CommissionRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Achintha Kalunayaka
 * @since 9/18/2025
 */

@Repository
public interface CommissionRuleRepository extends JpaRepository<CommissionRule, Long> {

    @Query("""
        SELECT r FROM CommissionRule r
        WHERE (r.partnerTier IS NULL OR r.partnerTier = :partnerTier)
          AND (r.category IS NULL OR r.category = :category)
          AND (:saleDate BETWEEN r.validFrom AND r.validTo)
          AND (r.minAmount IS NULL OR :amount >= r.minAmount)
          AND (r.maxAmount IS NULL OR :amount <= r.maxAmount)
        ORDER BY r.priority ASC
    """)
    List<CommissionRule> findApplicableRules(
            @Param("partnerTier") String partnerTier,
            @Param("category") String category,
            @Param("amount") BigDecimal amount,
            @Param("saleDate") LocalDateTime saleDate
    );
}
