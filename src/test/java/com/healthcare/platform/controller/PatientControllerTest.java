package com.healthcare.platform.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.platform.dto.PatientRequest;
import com.healthcare.platform.dto.PatientResponse;
import com.healthcare.platform.exception.ResourceNotFoundException;
import com.healthcare.platform.security.JwtTokenProvider;
import com.healthcare.platform.service.PatientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PatientController.class)
@DisplayName("Patient Controller Tests")
class PatientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PatientService patientService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    private PatientRequest validRequest;
    private PatientResponse sampleResponse;
    private UUID patientId;

    @BeforeEach
    void setUp() {
        patientId = UUID.randomUUID();

        validRequest = PatientRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .dateOfBirth(LocalDate.of(1990, 5, 15))
                .email("john.doe@example.com")
                .phone("+14155551234")
                .address("123 Healthcare Ave")
                .build();

        sampleResponse = PatientResponse.builder()
                .id(patientId)
                .firstName("John")
                .lastName("Doe")
                .dateOfBirth(LocalDate.of(1990, 5, 15))
                .email("john.doe@example.com")
                .phone("+14155551234")
                .address("123 Healthcare Ave")
                .medicalRecordNumber("MRN-123456")
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("POST /api/v1/patients")
    class CreatePatient {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("should create patient successfully")
        void shouldCreatePatientSuccessfully() throws Exception {
            when(patientService.createPatient(any(PatientRequest.class)))
                    .thenReturn(sampleResponse);

            mockMvc.perform(post("/api/v1/patients")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.firstName").value("John"))
                    .andExpect(jsonPath("$.data.lastName").value("Doe"));

            verify(patientService).createPatient(any(PatientRequest.class));
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("should return 400 for invalid request")
        void shouldReturn400ForInvalidRequest() throws Exception {
            PatientRequest invalidRequest = PatientRequest.builder()
                    .firstName("")  // Invalid: blank
                    .lastName("Doe")
                    .dateOfBirth(LocalDate.now().plusDays(1))  // Invalid: future date
                    .build();

            mockMvc.perform(post("/api/v1/patients")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));

            verify(patientService, never()).createPatient(any());
        }

        @Test
        @DisplayName("should return 401 for unauthenticated request")
        void shouldReturn401ForUnauthenticatedRequest() throws Exception {
            mockMvc.perform(post("/api/v1/patients")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/patients/{id}")
    class GetPatientById {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("should return patient by ID")
        void shouldReturnPatientById() throws Exception {
            when(patientService.getPatientById(patientId)).thenReturn(sampleResponse);

            mockMvc.perform(get("/api/v1/patients/{id}", patientId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(patientId.toString()))
                    .andExpect(jsonPath("$.data.firstName").value("John"));

            verify(patientService).getPatientById(patientId);
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("should return 404 for non-existent patient")
        void shouldReturn404ForNonExistentPatient() throws Exception {
            UUID nonExistentId = UUID.randomUUID();
            when(patientService.getPatientById(nonExistentId))
                    .thenThrow(new ResourceNotFoundException("Patient", "id", nonExistentId.toString()));

            mockMvc.perform(get("/api/v1/patients/{id}", nonExistentId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/patients")
    class GetAllPatients {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("should return paginated patients")
        void shouldReturnPaginatedPatients() throws Exception {
            Page<PatientResponse> page = new PageImpl<>(List.of(sampleResponse));
            when(patientService.getAllPatients(any(Pageable.class))).thenReturn(page);

            mockMvc.perform(get("/api/v1/patients")
                            .param("page", "0")
                            .param("size", "20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content[0].firstName").value("John"));

            verify(patientService).getAllPatients(any(Pageable.class));
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("should search patients by name")
        void shouldSearchPatientsByName() throws Exception {
            Page<PatientResponse> page = new PageImpl<>(List.of(sampleResponse));
            when(patientService.searchPatients(eq("John"), any(Pageable.class))).thenReturn(page);

            mockMvc.perform(get("/api/v1/patients")
                            .param("search", "John"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            verify(patientService).searchPatients(eq("John"), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/patients/{id}")
    class UpdatePatient {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("should update patient successfully")
        void shouldUpdatePatientSuccessfully() throws Exception {
            PatientResponse updatedResponse = PatientResponse.builder()
                    .id(patientId)
                    .firstName("Jane")
                    .lastName("Doe")
                    .dateOfBirth(LocalDate.of(1990, 5, 15))
                    .status("ACTIVE")
                    .build();

            when(patientService.updatePatient(eq(patientId), any(PatientRequest.class)))
                    .thenReturn(updatedResponse);

            PatientRequest updateRequest = PatientRequest.builder()
                    .firstName("Jane")
                    .lastName("Doe")
                    .dateOfBirth(LocalDate.of(1990, 5, 15))
                    .build();

            mockMvc.perform(put("/api/v1/patients/{id}", patientId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.firstName").value("Jane"));

            verify(patientService).updatePatient(eq(patientId), any(PatientRequest.class));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/patients/{id}")
    class DeletePatient {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("should delete patient successfully")
        void shouldDeletePatientSuccessfully() throws Exception {
            doNothing().when(patientService).deletePatient(patientId);

            mockMvc.perform(delete("/api/v1/patients/{id}", patientId)
                            .with(csrf()))
                    .andExpect(status().isNoContent());

            verify(patientService).deletePatient(patientId);
        }
    }
}
