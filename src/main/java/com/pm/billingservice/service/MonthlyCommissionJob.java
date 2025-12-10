package com.pm.billingservice.service;

import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.*;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * @author Achintha Kalunayaka
 * @since 10/7/2025
 */

@Service
@RequiredArgsConstructor
//@Profile("master")
public class MonthlyCommissionJob {

    private static final Logger logger = LoggerFactory.getLogger(MonthlyCommissionJob.class);

    private final ScheduledJobService scheduledJobService;

    private final JobLauncher jobLauncher;

    private final JobOperator jobOperator;

    private final JobExplorer jobExplorer;

    private final Job monthlyInvoiceJob;

     @SchedulerLock(name = "aggregationJob_1", lockAtMostFor = "10m")
public void generateMonthlyInvoice() {
        String jobName = "MONTHLY_INVOICE";
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        // check if already run this month


        try {
            if (scheduledJobService.hasRunThisMonth(jobName, now)) {
                logger.info("Skipping job {}, already executed for month {}", jobName, now.getMonth());
                return;
            }
//            if (scheduledJobService.findJobStatus(jobName).equals("RUNNING")) {
//                logger.info("Skipping job {}, job is already running {}", jobName, now.getMonth());
//                return;
//            }
            logger.info("Running monthly invoice for {}", now.getMonth());

            // your main logic here
            JobParameters params = new JobParametersBuilder()
                    .addString("time", now.toString())
                    .toJobParameters();

//            jobOperator.restart(1);

                jobLauncher.run(monthlyInvoiceJob, params);

                // mark job as completed
//            scheduledJobService.updateLastRun(jobName);
                logger.info("Monthly invoice completed successfully");




        } catch (Exception ex) {
            logger.error("Error during monthly invoice job: {}", ex.getMessage(), ex);
            // optionally send alert or retry logic
        }
    }

    public void recoverAndRestart(JobInstance jobInstance) throws Exception {
        JobExecution lastExecution = jobExplorer.getLastJobExecution(jobInstance);
        if (lastExecution.getStatus().equals(BatchStatus.STARTED)) {
            // mark it failed manually
            lastExecution.setStatus(BatchStatus.FAILED);
            lastExecution.setExitStatus(ExitStatus.FAILED);
        }
        // now restart it
        jobOperator.restart(lastExecution.getId());
    }

}
