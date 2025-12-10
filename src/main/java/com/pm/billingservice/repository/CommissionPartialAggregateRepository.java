package com.pm.billingservice.repository;

import com.pm.billingservice.model.CommissionPartialAggregate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Achintha Kalunayaka
 * @since 11/9/2025
 */

@Repository
public interface CommissionPartialAggregateRepository extends JpaRepository<CommissionPartialAggregate, Long> {
}
