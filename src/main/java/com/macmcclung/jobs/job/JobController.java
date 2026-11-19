package com.macmcclung.jobs.job;

import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/jobs")
public class JobController {
    private final JobService service;

    public JobController(JobService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Job> create(@Valid @RequestBody JobRequests.Create req) {
        Job job = service.create(req);
        return ResponseEntity.created(URI.create("/api/jobs/" + job.getId())).body(job);
    }

    @GetMapping
    public List<Job> list(@RequestParam(required = false) JobStatus status,
                          @RequestParam(required = false) String technician,
                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate day) {
        return service.list(status, technician, day);
    }

    @GetMapping("/{id}")
    public Job get(@PathVariable Long id) {
        return service.get(id);
    }

    @PatchMapping("/{id}/status")
    public Job changeStatus(@PathVariable Long id, @Valid @RequestBody JobRequests.StatusChange req) {
        return service.changeStatus(id, req.status());
    }

    @PatchMapping("/{id}/schedule")
    public Job reschedule(@PathVariable Long id, @Valid @RequestBody JobRequests.Reschedule req) {
        return service.reschedule(id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
