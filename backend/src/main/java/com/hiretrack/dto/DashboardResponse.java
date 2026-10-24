package com.hiretrack.dto;

import com.hiretrack.entity.ApplicationStatus;

import java.util.Map;

// Example: { "total": 5, "countsByStatus": { "APPLIED": 3, "INTERVIEW": 1, "OFFER": 1, "REJECTED": 0 } }
public record DashboardResponse(
        long total,
        Map<ApplicationStatus, Long> countsByStatus
) {
}
