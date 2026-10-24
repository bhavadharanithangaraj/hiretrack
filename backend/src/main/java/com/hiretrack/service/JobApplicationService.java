package com.hiretrack.service;

import com.hiretrack.dto.DashboardResponse;
import com.hiretrack.dto.JobApplicationRequest;
import com.hiretrack.dto.JobApplicationResponse;
import com.hiretrack.entity.ApplicationStatus;
import com.hiretrack.entity.JobApplication;
import com.hiretrack.entity.User;
import com.hiretrack.exception.ResourceNotFoundException;
import com.hiretrack.repository.JobApplicationRepository;
import com.hiretrack.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Business logic for job applications. Every method takes the current user's email,
// so a user can only ever read or change their own applications.
@Service
public class JobApplicationService {

    private final JobApplicationRepository applicationRepository;
    private final UserRepository userRepository;

    // Constructor injection: Spring passes in the repositories for us
    public JobApplicationService(JobApplicationRepository applicationRepository, UserRepository userRepository) {
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<JobApplicationResponse> getAll(String userEmail, ApplicationStatus status) {
        List<JobApplication> apps = (status == null)
                ? applicationRepository.findByUserEmailOrderByAppliedDateDesc(userEmail)
                : applicationRepository.findByUserEmailAndStatusOrderByAppliedDateDesc(userEmail, status);
        return apps.stream().map(JobApplicationResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public JobApplicationResponse getById(String userEmail, Long id) {
        return JobApplicationResponse.from(findOwned(userEmail, id));
    }

    @Transactional
    public JobApplicationResponse create(String userEmail, JobApplicationRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        JobApplication app = new JobApplication(user);
        copyFields(request, app);
        return JobApplicationResponse.from(applicationRepository.save(app));
    }

    @Transactional
    public JobApplicationResponse update(String userEmail, Long id, JobApplicationRequest request) {
        JobApplication app = findOwned(userEmail, id);
        copyFields(request, app);
        // flush = write to the DB now, so @UpdateTimestamp fills in updatedAt before we build the response
        return JobApplicationResponse.from(applicationRepository.saveAndFlush(app));
    }

    @Transactional
    public void delete(String userEmail, Long id) {
        applicationRepository.delete(findOwned(userEmail, id));
    }

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(String userEmail) {
        // LinkedHashMap keeps the enum order: APPLIED, INTERVIEW, OFFER, REJECTED
        Map<ApplicationStatus, Long> counts = new LinkedHashMap<>();
        long total = 0;
        for (ApplicationStatus status : ApplicationStatus.values()) {
            long count = applicationRepository.countByUserEmailAndStatus(userEmail, status);
            counts.put(status, count);
            total += count;
        }
        return new DashboardResponse(total, counts);
    }

    // Someone else's application gives the same 404 as a missing one,
    // so users can't discover which IDs exist.
    private JobApplication findOwned(String userEmail, Long id) {
        return applicationRepository.findByIdAndUserEmail(id, userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Application " + id + " not found"));
    }

    private void copyFields(JobApplicationRequest request, JobApplication app) {
        app.setCompany(request.company().trim());
        app.setRole(request.role().trim());
        app.setLocation(request.location());
        app.setStatus(request.status());
        app.setAppliedDate(request.appliedDate());
        app.setNotes(request.notes());
    }
}
