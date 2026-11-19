package com.macmcclung.jobs.job;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class JobRequests {
    private JobRequests() {}

    public record Create(
            @NotBlank @Size(max = 120) String customerName,
            @NotBlank @Size(max = 200) String description,
            @Size(max = 80) String technician,
            @NotNull @FutureOrPresent LocalDateTime scheduledFor,
            @PositiveOrZero @Digits(integer = 8, fraction = 2) BigDecimal quotedPrice) {}

    public record StatusChange(@NotNull JobStatus status) {}

    public record Reschedule(
            @NotNull @FutureOrPresent LocalDateTime scheduledFor,
            @Size(max = 80) String technician) {}
}
