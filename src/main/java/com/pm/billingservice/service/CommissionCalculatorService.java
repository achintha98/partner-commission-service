package com.pm.billingservice.service;

import com.pm.billingservice.model.CommissionRule;
import com.pm.billingservice.model.Partner;
import com.pm.billingservice.model.Transaction;
import com.pm.billingservice.repository.CommissionRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Achintha Kalunayaka
 * @since 9/18/2025
 */

@Service
@RequiredArgsConstructor
public class CommissionCalculatorService {

    @Autowired
    private final CommissionRuleRepository ruleRepository;

    public BigDecimal calculateCommission(Transaction transaction, Partner partner) {
        List<CommissionRule> rules = ruleRepository.findApplicableRules(partner.getPartnerTier(), transaction.getCategory(),
                transaction.getAmount(), transaction.getSaleDate());

        return rules.stream()
                .map(rule -> {
                    BigDecimal percentagePart = rule.getPercentage() != null
                            ? transaction.getAmount().multiply(rule.getPercentage().divide(BigDecimal.valueOf(100)))
                            : BigDecimal.ZERO;

                    BigDecimal bonusPart = rule.getFixedBonus() != null ? rule.getFixedBonus() : BigDecimal.ZERO;

                    return percentagePart.add(bonusPart);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
