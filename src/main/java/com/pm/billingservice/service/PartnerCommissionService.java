package com.pm.billingservice.service;

import com.pm.billingservice.model.Partner;
import com.pm.billingservice.model.PartnerCommission;
import com.pm.billingservice.model.Transaction;
import com.pm.billingservice.repository.PartnerCommissionRepository;
import com.pm.billingservice.repository.PartnerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * @author Achintha Kalunayaka
 * @since 9/19/2025
 */

@Service
@RequiredArgsConstructor
public class PartnerCommissionService {

    private final CommissionCalculatorService commissionCalculatorService;

    private final PartnerRepository partnerRepository;

    private final PartnerCommissionRepository partnerCommissionRepository;

    public PartnerCommission calculatePartnerCommission(Transaction transaction) {
        Partner partner = partnerRepository.findById(transaction.getPartnerId()).orElseThrow(() -> new RuntimeException("Partner not found: " + transaction.getPartnerId()));
        BigDecimal commissionAmount = commissionCalculatorService.calculateCommission(transaction, partner);
        //TODO: implement a relationship between commissionPartner and commissionRules tables to store rule ids
        return partnerCommissionRepository.save(PartnerCommission.builder().partnerId(transaction.getPartnerId()).commissionAmount(commissionAmount).saleAmount(transaction.getAmount()).
                transactionId(transaction.getTransactionId()).saleDate(transaction.getSaleDate()).build());
    }

}
