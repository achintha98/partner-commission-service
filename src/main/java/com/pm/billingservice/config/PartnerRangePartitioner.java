package com.pm.billingservice.config;

import com.pm.billingservice.dto.PartnerCount;
import com.pm.billingservice.repository.PartnerCommissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Achintha Kalunayaka
 * @since 10/20/2025
 */

@Component
@RequiredArgsConstructor
public class PartnerRangePartitioner implements Partitioner {

    private final PartnerCommissionRepository partnerCommissionRepository;

    // Target work size for a single partition (e.g., 100,000 transactions)
    private static final int TARGET_WORK_SIZE = 100000;

    // Threshold to consider a partner "fat" and require splitting
    private static final int FAT_PARTNER_THRESHOLD = 100000;

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        List<PartnerCount> allPartnerCounts = partnerCommissionRepository.getPartitionInfoForAllPartners();

        Map<String, ExecutionContext> partitions = new HashMap<>();


        int partitionIndex = 0;

        for (PartnerCount partnerCount : allPartnerCounts) {
            long count = partnerCount.getTransactionCount();
            Long partnerId = partnerCount.getPartnerId();
            Long minId = partnerCount.getMinTransactionId();
            Long maxId = partnerCount.getMaxTransactionId();

            if (count < TARGET_WORK_SIZE) {
                // Case 1: Small Partner (Assign a single partition)

                partitions.put("partition-" + partitionIndex++, createPartitionContext(partnerId, minId, maxId));

            } else {
                // Case 3: Fat Partner (Split into sub-partitions)
                int numSplits = (int) Math.ceil((double) count / TARGET_WORK_SIZE);

                // Determine splits based on an indexed column, e.g., Transaction ID (Assumes sequential IDs)
                long rangeSize = (maxId - minId) / numSplits;
                long startId = minId;

                for (int i = 0; i < numSplits; i++) {
                    long endId = (i == numSplits - 1) ? maxId : startId + rangeSize;

                    // Assign a range of Transaction IDs to the partition
                    partitions.put("partition-" + partitionIndex++, createPartitionContext(partnerId, startId, endId));
                    startId = endId + 1; // Start the next partition one ID higher
                }
            }
        }
        return partitions;
    }

    private ExecutionContext createPartitionContext(Long partnerId, Long startId, Long endId) {
        ExecutionContext context = new ExecutionContext();
        context.putLong("partnerId", partnerId);

        // These keys tell the ItemReader whether to split the work further
        if (startId != null && endId != null) {
            context.putLong("minTransactionId", startId);
            context.putLong("maxTransactionId", endId);
        }
        return context;
    }
}
