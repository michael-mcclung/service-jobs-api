package com.macmcclung.jobs.job;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "service_job")
public class Job {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String customerName;

    @Column(nullable = false, length = 200)
    private String description;

    @Column(length = 80)
    private String technician;

    @Column(nullable = false)
    private LocalDateTime scheduledFor;

    @Column(precision = 10, scale = 2)
    private BigDecimal quotedPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private JobStatus status = JobStatus.SCHEDULED;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    private Instant updatedAt = Instant.now();

    protected Job() {}

    public Job(String customerName, String description, String technician,
               LocalDateTime scheduledFor, BigDecimal quotedPrice) {
        this.customerName = customerName;
        this.description = description;
        this.technician = technician;
        this.scheduledFor = scheduledFor;
        this.quotedPrice = quotedPrice;
    }

    public void moveTo(JobStatus next) {
        if (!status.canMoveTo(next)) {
            throw new InvalidStatusChangeException(status, next);
        }
        status = next;
        updatedAt = Instant.now();
    }

    public void reschedule(LocalDateTime when, String technician) {
        if (status != JobStatus.SCHEDULED) {
            throw new IllegalStateException("Only scheduled jobs can be rescheduled");
        }
        this.scheduledFor = when;
        this.technician = technician;
        updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getCustomerName() { return customerName; }
    public String getDescription() { return description; }
    public String getTechnician() { return technician; }
    public LocalDateTime getScheduledFor() { return scheduledFor; }
    public BigDecimal getQuotedPrice() { return quotedPrice; }
    public JobStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
