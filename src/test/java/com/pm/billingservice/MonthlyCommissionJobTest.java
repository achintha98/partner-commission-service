package com.pm.billingservice;

import com.pm.billingservice.service.MonthlyCommissionJob;
import com.pm.billingservice.service.ScheduledJobService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.test.context.SpringBootTest;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Achintha Kalunayaka
 * @since 11/8/2025
 */

@SpringBootTest
public class MonthlyCommissionJobTest {

    @InjectMocks
    private MonthlyCommissionJob monthlyCommissionJob;

    @Mock
    private ScheduledJobService scheduledJobService;

    @Mock
    private JobLauncher jobLauncher;

    @Mock
    private Job monthlyInvoiceJob;

    @Test
    public void testMonthlyJobRunSuccessfully() {
        when(scheduledJobService.findJobStatus("MONTHLY_INVOICE")).thenReturn("INCOMPLETE");

        monthlyCommissionJob.generateMonthlyInvoice();

        verify(scheduledJobService).updateLastRun("MONTHLY_INVOICE");

    }

}
