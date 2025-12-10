package com.pm.billingservice.controller;

import com.pm.billingservice.model.PartnerCommission;
import com.pm.billingservice.repository.PartnerCommissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


/**
 * @author Achintha Kalunayaka
 * @since 10/22/2025
 */

@RestController
@RequiredArgsConstructor
public class JobController {

    private final PartnerCommissionRepository partnerCommissionRepository;



    @PostMapping("/start-invoice-job")
    public String startJob() throws Exception {

        return "Job started";
    }

    @GetMapping("/pageable/{minId}/{maxId}")
    public List<PartnerCommission> getCommissionsByRange(
            @PathVariable Long minId,
            @PathVariable Long maxId,
            // Use PageableDefault to provide sensible defaults for testing
            @PageableDefault(size = 10, page = 0, sort = "id") Pageable pageable) {

        // Log the received parameters to confirm the request is hitting the controller
        System.out.println("TEST CONTROLLER: Received request for range: [" + minId + " - " + maxId + "]");
        System.out.println("TEST CONTROLLER: Pagination parameters: " + pageable.toString());

        // This is the call that mirrors the ItemReader's underlying call structure
        Page<PartnerCommission> result = partnerCommissionRepository.findByTransactionIdBetween(minId, maxId, pageable);

        ;
        System.out.println("TEST CONTROLLER: Repository returned Page with " + result.getNumberOfElements() +
                " elements. Total: " + result.getContent());

        return result.getContent();    }

}
