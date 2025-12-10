package com.pm.billingservice.repository;

import com.pm.billingservice.model.ScheduledJobRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author Achintha Kalunayaka
 * @since 10/8/2025
 */

@Repository
public interface ScheduledJobRepository extends JpaRepository<ScheduledJobRun, String> {

    @Query("select sj.jobStatus from ScheduledJobRun sj where sj.jobName =: jobName")
    String findJobStatus(String jobName);

    Optional<ScheduledJobRun> findByJobName(String jobName);
}
