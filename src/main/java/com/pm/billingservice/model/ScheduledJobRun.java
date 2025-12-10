package com.pm.billingservice.model;

import com.pm.billingservice.model.enums.JobStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @author Achintha Kalunayaka
 * @since 10/7/2025
 */

@Entity
@Builder
@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class ScheduledJobRun {
    @Id
    private UUID jobId;

    @NotNull
    private String jobName;

    @NotNull
    private LocalDateTime lastRun;

    @Enumerated(EnumType.STRING)
    @NotNull
    private JobStatus jobStatus;


}
