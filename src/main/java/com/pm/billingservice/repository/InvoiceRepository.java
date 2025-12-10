package com.pm.billingservice.repository;

import com.pm.billingservice.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * @author Achintha Kalunayaka
 * @since 10/9/2025
 */

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {
}
