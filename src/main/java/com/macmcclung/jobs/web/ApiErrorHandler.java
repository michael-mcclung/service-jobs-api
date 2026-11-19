package com.macmcclung.jobs.web;

import com.macmcclung.jobs.job.InvalidStatusChangeException;
import com.macmcclung.jobs.job.JobNotFoundException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/** Returns RFC 9457 problem details so clients get consistent, readable errors. */
@RestControllerAdvice
public class ApiErrorHandler {

    @ExceptionHandler(JobNotFoundException.class)
    ProblemDetail notFound(JobNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "Job not found", ex.getMessage());
    }

    @ExceptionHandler({InvalidStatusChangeException.class, IllegalStateException.class})
    ProblemDetail conflict(RuntimeException ex) {
        return problem(HttpStatus.CONFLICT, "Not allowed in the job's current status", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail invalid(MethodArgumentNotValidException ex) {
        ProblemDetail pd = problem(HttpStatus.BAD_REQUEST, "Validation failed", "One or more fields are invalid");
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(e -> errors.putIfAbsent(e.getField(), e.getDefaultMessage()));
        pd.setProperty("errors", errors);
        return pd;
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    ProblemDetail unreadable(Exception ex) {
        return problem(HttpStatus.BAD_REQUEST, "Malformed request", "The request body or a parameter could not be read");
    }

    private static ProblemDetail problem(HttpStatus status, String title, String detail) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setTitle(title);
        return pd;
    }
}
