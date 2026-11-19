package com.macmcclung.jobs.job;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class JobService {
    private final JobRepository repo;

    public JobService(JobRepository repo) {
        this.repo = repo;
    }

    public Job create(JobRequests.Create req) {
        return repo.save(new Job(req.customerName().trim(), req.description().trim(),
                blankToNull(req.technician()), req.scheduledFor(), req.quotedPrice()));
    }

    @Transactional(readOnly = true)
    public Job get(Long id) {
        return repo.findById(id).orElseThrow(() -> new JobNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<Job> list(JobStatus status, String technician, LocalDate day) {
        if (status != null) return repo.findByStatusOrderByScheduledForAsc(status);
        if (technician != null && !technician.isBlank()) return repo.findByTechnicianIgnoreCaseOrderByScheduledForAsc(technician.trim());
        if (day != null) return repo.findByScheduledForBetweenOrderByScheduledForAsc(day.atStartOfDay(), day.plusDays(1).atStartOfDay().minusNanos(1));
        return repo.findAll(org.springframework.data.domain.Sort.by("scheduledFor"));
    }

    public Job changeStatus(Long id, JobStatus next) {
        Job job = get(id);
        job.moveTo(next);
        return job;
    }

    public Job reschedule(Long id, JobRequests.Reschedule req) {
        Job job = get(id);
        job.reschedule(req.scheduledFor(), blankToNull(req.technician()));
        return job;
    }

    public void delete(Long id) {
        Job job = get(id);
        if (job.getStatus() != JobStatus.SCHEDULED && job.getStatus() != JobStatus.CANCELLED) {
            throw new IllegalStateException("Only scheduled or cancelled jobs can be deleted");
        }
        repo.delete(job);
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
