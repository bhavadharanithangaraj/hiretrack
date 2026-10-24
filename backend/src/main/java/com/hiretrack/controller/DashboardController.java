package com.hiretrack.controller;

import com.hiretrack.dto.DashboardResponse;
import com.hiretrack.service.JobApplicationService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final JobApplicationService service;

    public DashboardController(JobApplicationService service) {
        this.service = service;
    }

    @GetMapping
    public DashboardResponse getDashboard(Authentication auth) {
        return service.getDashboard(auth.getName());
    }
}
