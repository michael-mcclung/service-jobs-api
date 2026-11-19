package com.macmcclung.jobs.job;

import java.util.EnumSet;
import java.util.Set;

/** Lifecycle of a service job. Only the listed transitions are allowed. */
public enum JobStatus {
    SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED;

    public Set<JobStatus> allowedNext() {
        return switch (this) {
            case SCHEDULED -> EnumSet.of(IN_PROGRESS, CANCELLED);
            case IN_PROGRESS -> EnumSet.of(COMPLETED, CANCELLED);
            case COMPLETED, CANCELLED -> EnumSet.noneOf(JobStatus.class);
        };
    }

    public boolean canMoveTo(JobStatus next) {
        return allowedNext().contains(next);
    }
}
