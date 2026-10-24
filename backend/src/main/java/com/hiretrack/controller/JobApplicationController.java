package com.hiretrack.controller;

import com.hiretrack.config.DataSeeder;
import com.hiretrack.dto.JobApplicationRequest;
import com.hiretrack.dto.JobApplicationResponse;
import com.hiretrack.entity.ApplicationStatus;
import com.hiretrack.service.JobApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Thin layer: reads the HTTP request, calls the service, returns the result.
@RestController
@RequestMapping("/api/applications")
public class JobApplicationController {

    // TODO (Step 3): replace with the logged-in user's email from the JWT
    private static final String CURRENT_USER = DataSeeder.DEMO_USER_EMAIL;

    private final JobApplicationService service;

    public JobApplicationController(JobApplicationService service) {
        this.service = service;
    }

    // GET /api/applications or /api/applications?status=INTERVIEW
    @GetMapping
    public List<JobApplicationResponse> getAll(@RequestParam(required = false) ApplicationStatus status) {
        return service.getAll(CURRENT_USER, status);
    }

    @GetMapping("/{id}")
    public JobApplicationResponse getById(@PathVariable Long id) {
        return service.getById(CURRENT_USER, id);
    }

    // @Valid runs the Bean Validation rules in JobApplicationRequest
    @PostMapping
    public ResponseEntity<JobApplicationResponse> create(@Valid @RequestBody JobApplicationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(CURRENT_USER, request));
    }

    @PutMapping("/{id}")
    public JobApplicationResponse update(@PathVariable Long id, @Valid @RequestBody JobApplicationRequest request) {
        return service.update(CURRENT_USER, id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(CURRENT_USER, id);
        return ResponseEntity.noContent().build();
    }
}
