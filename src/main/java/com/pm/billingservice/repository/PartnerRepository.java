package com.pm.billingservice.repository;

import com.pm.billingservice.model.Partner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * @author Achintha Kalunayaka
 * @since 9/19/2025
 */

@Repository
public interface PartnerRepository extends JpaRepository<Partner, Long> {
    @Query("SELECT MIN(p.id) FROM Partner p")
    Long findMin();
    @Query("SELECT MAX(p.id) FROM Partner p")
    Long findMax();

    Page<Partner> findByIdBetween(Long minId, Long maxId, Pageable pageable);
}
