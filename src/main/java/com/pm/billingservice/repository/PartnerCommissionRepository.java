package com.pm.billingservice.repository;

import com.pm.billingservice.dto.PartnerCount;
import com.pm.billingservice.model.PartnerCommission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Achintha Kalunayaka
 * @since 9/19/2025
 */

@Repository
public interface PartnerCommissionRepository extends JpaRepository<PartnerCommission, Long> {

    List<Integer> countByPartnerIdAndSaleDateBefore(Long partnerId, LocalDateTime saleDate);

    @Query(
            "SELECT new com.pm.billingservice.dto.PartnerCount(" + // Use the fully qualified class name
                    "p.partnerId, " +                               // 1. partnerId (Entity field)
                    "COUNT(p.transactionId), " +                               // 2. Count
                    "MIN(p.transactionId), " +                       // 3. Min Transaction ID (Entity field)
                    "MAX(p.transactionId)) " +                       // 4. Max Transaction ID (Entity field)
                    "FROM PartnerCommission p " +
                    "GROUP BY p.partnerId"
    )
    List<PartnerCount> getPartitionInfoForAllPartners();

    @Query("SELECT MIN(pc.id) FROM PartnerCommission pc")
    Long findMin();
    @Query("SELECT MAX(pc.id) FROM PartnerCommission pc")
    Long findMax();

    Page<PartnerCommission> findByTransactionIdBetween(Long minId, Long maxId, Pageable pageable);

}


