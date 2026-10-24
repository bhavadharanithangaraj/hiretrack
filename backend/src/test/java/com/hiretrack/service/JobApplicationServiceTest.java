package com.hiretrack.service;

import com.hiretrack.dto.DashboardResponse;
import com.hiretrack.dto.JobApplicationRequest;
import com.hiretrack.dto.JobApplicationResponse;
import com.hiretrack.entity.ApplicationStatus;
import com.hiretrack.entity.JobApplication;
import com.hiretrack.entity.Role;
import com.hiretrack.entity.User;
import com.hiretrack.exception.ResourceNotFoundException;
import com.hiretrack.repository.JobApplicationRepository;
import com.hiretrack.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// Unit tests: no Spring, no database. The repositories are fake (Mockito mocks),
// so we only test the logic inside JobApplicationService.
@ExtendWith(MockitoExtension.class)
class JobApplicationServiceTest {

    private static final String EMAIL = "alice@test.com";

    @Mock
    private JobApplicationRepository applicationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private JobApplicationService service;

    private User alice;

    @BeforeEach
    void setUp() {
        alice = new User("Alice", EMAIL, "hashed", Role.ROLE_USER);
    }

    @Test
    void create_savesApplicationForCurrentUser() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(alice));
        when(applicationRepository.save(any(JobApplication.class))).thenAnswer(inv -> inv.getArgument(0));

        JobApplicationResponse response = service.create(EMAIL, request("  Google  ", ApplicationStatus.APPLIED));

        // Capture what was passed to save() and check it
        ArgumentCaptor<JobApplication> captor = ArgumentCaptor.forClass(JobApplication.class);
        verify(applicationRepository).save(captor.capture());
        JobApplication saved = captor.getValue();

        assertThat(saved.getUser()).isSameAs(alice);
        assertThat(saved.getCompany()).isEqualTo("Google"); // trimmed
        assertThat(saved.getStatus()).isEqualTo(ApplicationStatus.APPLIED);
        assertThat(response.company()).isEqualTo("Google");
    }

    @Test
    void getById_throwsNotFound_whenApplicationBelongsToAnotherUser() {
        // The query filters by id AND email, so someone else's application comes back empty
        when(applicationRepository.findByIdAndUserEmail(42L, EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(EMAIL, 42L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("42");
    }

    @Test
    void getAll_withStatus_usesFilteredQuery() {
        JobApplication interview = existingApplication("Microsoft", ApplicationStatus.INTERVIEW);
        when(applicationRepository.findByUserEmailAndStatusOrderByAppliedDateDesc(EMAIL, ApplicationStatus.INTERVIEW))
                .thenReturn(List.of(interview));

        List<JobApplicationResponse> result = service.getAll(EMAIL, ApplicationStatus.INTERVIEW);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).company()).isEqualTo("Microsoft");
        verify(applicationRepository, never()).findByUserEmailOrderByAppliedDateDesc(any());
    }

    @Test
    void update_changesFieldsOfOwnedApplication() {
        JobApplication existing = existingApplication("Google", ApplicationStatus.APPLIED);
        when(applicationRepository.findByIdAndUserEmail(1L, EMAIL)).thenReturn(Optional.of(existing));
        when(applicationRepository.saveAndFlush(existing)).thenReturn(existing);

        JobApplicationResponse response = service.update(EMAIL, 1L, request("Google", ApplicationStatus.OFFER));

        assertThat(response.status()).isEqualTo(ApplicationStatus.OFFER);
        assertThat(existing.getStatus()).isEqualTo(ApplicationStatus.OFFER);
    }

    @Test
    void delete_throwsNotFound_andDeletesNothing_whenNotOwned() {
        when(applicationRepository.findByIdAndUserEmail(7L, EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(EMAIL, 7L)).isInstanceOf(ResourceNotFoundException.class);
        verify(applicationRepository, never()).delete(any());
    }

    @Test
    void getDashboard_countsEveryStatusAndTotal() {
        when(applicationRepository.countByUserEmailAndStatus(EMAIL, ApplicationStatus.APPLIED)).thenReturn(3L);
        when(applicationRepository.countByUserEmailAndStatus(EMAIL, ApplicationStatus.INTERVIEW)).thenReturn(2L);
        when(applicationRepository.countByUserEmailAndStatus(EMAIL, ApplicationStatus.OFFER)).thenReturn(1L);
        // REJECTED is not stubbed, so Mockito returns 0

        DashboardResponse dashboard = service.getDashboard(EMAIL);

        assertThat(dashboard.total()).isEqualTo(6);
        assertThat(dashboard.countsByStatus())
                .containsEntry(ApplicationStatus.APPLIED, 3L)
                .containsEntry(ApplicationStatus.INTERVIEW, 2L)
                .containsEntry(ApplicationStatus.OFFER, 1L)
                .containsEntry(ApplicationStatus.REJECTED, 0L);
    }

    private JobApplicationRequest request(String company, ApplicationStatus status) {
        return new JobApplicationRequest(company, "Software Engineer", "Remote", status,
                LocalDate.of(2026, 10, 1), "notes");
    }

    private JobApplication existingApplication(String company, ApplicationStatus status) {
        JobApplication app = new JobApplication(alice);
        app.setCompany(company);
        app.setRole("Software Engineer");
        app.setStatus(status);
        app.setAppliedDate(LocalDate.of(2026, 10, 1));
        return app;
    }
}
