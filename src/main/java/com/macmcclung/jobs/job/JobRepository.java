package com.macmcclung.jobs.job;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobRepository extends JpaRepository<Job, Long> {
    List<Job> findByStatusOrderByScheduledForAsc(JobStatus status);
    List<Job> findByTechnicianIgnoreCaseOrderByScheduledForAsc(String technician);
    List<Job> findByScheduledForBetweenOrderByScheduledForAsc(LocalDateTime from, LocalDateTime to);
}
