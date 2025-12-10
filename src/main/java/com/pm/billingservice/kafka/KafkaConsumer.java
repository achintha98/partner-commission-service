package com.pm.billingservice.kafka;

import com.pm.billingservice.model.PartnerCommission;
import com.pm.billingservice.model.Transaction;
import com.pm.billingservice.service.PartnerCommissionService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import transactionDB.public$.transaction_out_box.Value;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.TimeZone;

/**
 * @author Achintha Kalunayaka
 * @since 4/25/2025
 */

@Component
@RequiredArgsConstructor
public class KafkaConsumer {

//    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumer.class);
//
//    @Autowired
//    private PartnerCommissionService partnerCommissionService;
//
//    @KafkaListener(topics = "transactionDB.public.transaction_out_box", groupId = "analytics-service")
//    public void consumeEvents(Value value) {
//        try {
//            Transaction transaction = Transaction.builder().transactionId(value.getTransactionId()).amount(BigDecimal.valueOf(value.getAmount())).
//                    currency(value.getCurrency().toString()).partnerId(value.getPartnerId()).saleDate(LocalDateTime.ofInstant(Instant.ofEpochMilli(value.getSaleDate()),
//                            TimeZone.getDefault().toZoneId())).build();
//            PartnerCommission partnerCommission = partnerCommissionService.calculatePartnerCommission(transaction);
//            logger.info("Received transaction event: [PartnerId={}, Amount={}, Currency={}]",
//                    value.getPartnerId(), value.getAmount(), value.getCurrency());
//        } catch (RuntimeException e) {
//            logger.error("Error deserializing event: {}", e.getMessage());
//        }
//    }

    }


