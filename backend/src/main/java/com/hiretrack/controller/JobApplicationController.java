package com.hiretrack.controller;

import com.hiretrack.dto.JobApplicationRequest;
import com.hiretrack.dto.JobApplicationResponse;
import com.hiretrack.entity.ApplicationStatus;
import com.hiretrack.service.JobApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Thin layer: reads the HTTP request, calls the service, returns the result.
// Spring injects "Authentication" (set by JwtAuthenticationFilter); getName() is the user's email.
@RestController
@RequestMapping("/api/applications")
public class JobApplicationController {

    private final JobApplicationService service;

    public JobApplicationController(JobApplicationService service) {
        this.service = service;
    }

    // GET /api/applications or /api/applications?status=INTERVIEW
    @GetMapping
    public List<JobApplicationResponse> getAll(Authentication auth,
                                               @RequestParam(required = false) ApplicationStatus status) {
        return service.getAll(auth.getName(), status);
    }

    @GetMapping("/{id}")
    public JobApplicationResponse getById(Authentication auth, @PathVariable Long id) {
        return service.getById(auth.getName(), id);
    }

    // @Valid runs the Bean Validation rules in JobApplicationRequest
    @PostMapping
    public ResponseEntity<JobApplicationResponse> create(Authentication auth,
                                                         @Valid @RequestBody JobApplicationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(auth.getName(), request));
    }

    @PutMapping("/{id}")
    public JobApplicationResponse update(Authentication auth, @PathVariable Long id,
                                         @Valid @RequestBody JobApplicationRequest request) {
        return service.update(auth.getName(), id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Authentication auth, @PathVariable Long id) {
        service.delete(auth.getName(), id);
        return ResponseEntity.noContent().build();
    }
}
