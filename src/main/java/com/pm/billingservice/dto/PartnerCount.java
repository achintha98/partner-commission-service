package com.pm.billingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @param transactionCount Maps to the 'transaction_count' (COUNT(*)) column
 * @author Achintha Kalunayaka
 * @since 11/9/2025
 */
@Data
@AllArgsConstructor
public class PartnerCount {

    private Long partnerId;
    private Long transactionCount;
    private Long minTransactionId;
    private Long maxTransactionId;

}
