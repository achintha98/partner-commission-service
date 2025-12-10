package com.pm.billingservice.dto;

import lombok.*;
import org.springframework.batch.core.BatchStatus;
import java.io.Serializable;

/**
 * @author Achintha Kalunayaka
 * @since 10/23/2025
 */

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class StepExecutionReplyDto implements Serializable {
    private Long id;
    private String stepName;
    private Long jobExecutionId;
    private BatchStatus status;
    private String exitStatus;
}
