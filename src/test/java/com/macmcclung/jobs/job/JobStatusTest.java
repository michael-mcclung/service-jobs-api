package com.macmcclung.jobs.job;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class JobStatusTest {
    private Job newJob() {
        return new Job("Acme", "AC tune-up", "Sam", LocalDateTime.now().plusDays(1), new BigDecimal("129.00"));
    }

    @Test
    void followsTheAllowedLifecycle() {
        Job job = newJob();
        job.moveTo(JobStatus.IN_PROGRESS);
        job.moveTo(JobStatus.COMPLETED);
        assertEquals(JobStatus.COMPLETED, job.getStatus());
    }

    @Test
    void rejectsSkippingOrReopening() {
        Job job = newJob();
        assertThrows(InvalidStatusChangeException.class, () -> job.moveTo(JobStatus.COMPLETED));
        job.moveTo(JobStatus.CANCELLED);
        assertThrows(InvalidStatusChangeException.class, () -> job.moveTo(JobStatus.SCHEDULED));
    }

    @Test
    void onlyScheduledJobsCanBeRescheduled() {
        Job job = newJob();
        job.moveTo(JobStatus.IN_PROGRESS);
        assertThrows(IllegalStateException.class, () -> job.reschedule(LocalDateTime.now().plusDays(2), "Lee"));
    }
}
