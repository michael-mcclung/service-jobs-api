package com.macmcclung.jobs.job;

public class JobNotFoundException extends RuntimeException {
    public JobNotFoundException(Long id) {
        super("No job with id " + id);
    }
}
