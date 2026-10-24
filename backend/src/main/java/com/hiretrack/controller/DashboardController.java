package com.hiretrack.controller;

import com.hiretrack.config.DataSeeder;
import com.hiretrack.dto.DashboardResponse;
import com.hiretrack.service.JobApplicationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    // TODO (Step 3): replace with the logged-in user's email from the JWT
    private static final String CURRENT_USER = DataSeeder.DEMO_USER_EMAIL;

    private final JobApplicationService service;

    public DashboardController(JobApplicationService service) {
        this.service = service;
    }

    @GetMapping
    public DashboardResponse getDashboard() {
        return service.getDashboard(CURRENT_USER);
    }
}
