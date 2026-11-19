package com.macmcclung.jobs.job;

public class InvalidStatusChangeException extends RuntimeException {
    public InvalidStatusChangeException(JobStatus from, JobStatus to) {
        super("Cannot move a job from " + from + " to " + to + ". Allowed: " + from.allowedNext());
    }
}
