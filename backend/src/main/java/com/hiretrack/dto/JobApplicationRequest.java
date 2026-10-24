package com.hiretrack.dto;

import com.hiretrack.entity.ApplicationStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

// The JSON body the client sends to create or update an application.
public record JobApplicationRequest(
        @NotBlank(message = "Company is required")
        @Size(max = 100, message = "Company must be at most 100 characters")
        String company,

        @NotBlank(message = "Role is required")
        @Size(max = 100, message = "Role must be at most 100 characters")
        String role,

        @Size(max = 100, message = "Location must be at most 100 characters")
        String location,

        @NotNull(message = "Status is required")
        ApplicationStatus status,

        @NotNull(message = "Applied date is required")
        @PastOrPresent(message = "Applied date cannot be in the future")
        LocalDate appliedDate,

        @Size(max = 2000, message = "Notes must be at most 2000 characters")
        String notes
) {
}
