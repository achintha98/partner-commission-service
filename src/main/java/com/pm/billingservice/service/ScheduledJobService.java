package com.pm.billingservice.service;

import com.pm.billingservice.model.ScheduledJobRun;
import com.pm.billingservice.model.enums.JobStatus;
import com.pm.billingservice.repository.ScheduledJobRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

/**
 * @author Achintha Kalunayaka
 * @since 10/7/2025
 */

@Service
@RequiredArgsConstructor
public class ScheduledJobService {

    @PersistenceContext
    private final EntityManager em;

    private final ScheduledJobRepository scheduledJobRepository;

    public boolean hasRunThisMonth(String jobName, LocalDateTime now) {
        return scheduledJobRepository.findByJobName(jobName).map(jobRun ->
                {
                    LocalDateTime lastRunDate = jobRun.getLastRun();
                    LocalDateTime firstDayOfMonth = now.withDayOfMonth(1);
                    return !lastRunDate.isBefore(firstDayOfMonth);
                }
                ).orElse( false);
    }

    public String findJobStatus(String jobName) {
        return scheduledJobRepository.findJobStatus(jobName);
    }

    @Transactional
    public void updateLastRun(String jobName) {

        ScheduledJobRun jobRun = em.find(ScheduledJobRun.class, jobName);
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        if (jobRun == null) {
            em.persist(new ScheduledJobRun(UUID.randomUUID(),jobName, now, JobStatus.SUCCESS));
        } else {
            jobRun.setLastRun(now);
            em.merge(jobRun);
        }
    }

}
