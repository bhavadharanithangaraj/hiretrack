package com.hiretrack.repository;

import com.hiretrack.entity.ApplicationStatus;
import com.hiretrack.entity.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

// Spring Data generates the SQL from the method names.
// "UserEmail" means: follow the "user" field, then compare its "email".
public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {

    List<JobApplication> findByUserEmailOrderByAppliedDateDesc(String email);

    List<JobApplication> findByUserEmailAndStatusOrderByAppliedDateDesc(String email, ApplicationStatus status);

    // Only finds the application if it belongs to this user
    Optional<JobApplication> findByIdAndUserEmail(Long id, String email);

    long countByUserEmailAndStatus(String email, ApplicationStatus status);
}
