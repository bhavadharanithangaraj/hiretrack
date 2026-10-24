package com.hiretrack.controller;

import com.hiretrack.config.SecurityConfig;
import com.hiretrack.dto.JobApplicationResponse;
import com.hiretrack.entity.ApplicationStatus;
import com.hiretrack.exception.ResourceNotFoundException;
import com.hiretrack.security.JwtService;
import com.hiretrack.service.JobApplicationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// @WebMvcTest starts only the web layer (controller + security + exception handler), not the database.
// MockMvc sends fake HTTP requests, so we test URLs, status codes, JSON and validation for real.
@WebMvcTest(JobApplicationController.class)
@Import(SecurityConfig.class)
class JobApplicationControllerTest {

    private static final String EMAIL = "alice@test.com";

    @Autowired
    private MockMvc mockMvc;

    // @MockBean puts a Mockito mock into the Spring context instead of the real bean
    @MockBean
    private JobApplicationService service;

    // Needed by SecurityConfig
    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    @Test
    void getAll_withoutLogin_returns401() throws Exception {
        mockMvc.perform(get("/api/applications"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Please log in"));
    }

    // @WithMockUser pretends a user with this email is logged in
    @Test
    @WithMockUser(username = EMAIL)
    void getAll_returnsOnlyCurrentUsersApplications() throws Exception {
        when(service.getAll(EMAIL, ApplicationStatus.INTERVIEW)).thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/api/applications").param("status", "INTERVIEW"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].company").value("Google"))
                .andExpect(jsonPath("$[0].status").value("INTERVIEW"));
    }

    @Test
    @WithMockUser(username = EMAIL)
    void create_withValidBody_returns201() throws Exception {
        when(service.create(eq(EMAIL), any())).thenReturn(sampleResponse());

        String body = """
                {
                  "company": "Google",
                  "role": "Frontend Engineer",
                  "location": "Remote",
                  "status": "INTERVIEW",
                  "appliedDate": "2026-10-01",
                  "notes": "Referral"
                }
                """;

        mockMvc.perform(post("/api/applications").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(username = EMAIL)
    void create_withInvalidBody_returns400WithFieldErrors() throws Exception {
        String body = """
                { "company": "", "status": "APPLIED", "appliedDate": "2999-01-01" }
                """;

        mockMvc.perform(post("/api/applications").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.company").value("Company is required"))
                .andExpect(jsonPath("$.fieldErrors.role").value("Role is required"))
                .andExpect(jsonPath("$.fieldErrors.appliedDate").value("Applied date cannot be in the future"));

        // Validation failed, so the service must never be called
        verify(service, never()).create(any(), any());
    }

    @Test
    @WithMockUser(username = EMAIL)
    void getById_notOwned_returns404() throws Exception {
        when(service.getById(EMAIL, 99L)).thenThrow(new ResourceNotFoundException("Application 99 not found"));

        mockMvc.perform(get("/api/applications/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Application 99 not found"));
    }

    private JobApplicationResponse sampleResponse() {
        return new JobApplicationResponse(1L, "Google", "Frontend Engineer", "Remote",
                ApplicationStatus.INTERVIEW, LocalDate.of(2026, 10, 1), "Referral",
                Instant.now(), Instant.now());
    }
}
