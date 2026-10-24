package com.hiretrack.dto;

import com.hiretrack.entity.ApplicationStatus;
import com.hiretrack.entity.JobApplication;

import java.time.Instant;
import java.time.LocalDate;

// The JSON we send back. We never return the entity directly,
// so internal fields (like the owning user) don't leak out.
public record JobApplicationResponse(
        Long id,
        String company,
        String role,
        String location,
        ApplicationStatus status,
        LocalDate appliedDate,
        String notes,
        Instant createdAt,
        Instant updatedAt
) {
    public static JobApplicationResponse from(JobApplication app) {
        return new JobApplicationResponse(
                app.getId(),
                app.getCompany(),
                app.getRole(),
                app.getLocation(),
                app.getStatus(),
                app.getAppliedDate(),
                app.getNotes(),
                app.getCreatedAt(),
                app.getUpdatedAt()
        );
    }
}
