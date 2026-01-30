package com.healthcare.platform.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.platform.dto.AuthRequest;
import com.healthcare.platform.dto.PatientRequest;
import com.healthcare.platform.repository.PatientRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Patient API.
 * Tests the complete request/response flow including authentication.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Patient API Integration Tests")
class PatientIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PatientRepository patientRepository;

    private static String authToken;
    private static String createdPatientId;

    @BeforeEach
    void setUp() throws Exception {
        if (authToken == null) {
            // Get auth token once for all tests
            AuthRequest authRequest = new AuthRequest("admin", "admin123");

            MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(authRequest)))
                    .andExpect(status().isOk())
                    .andReturn();

            String response = result.getResponse().getContentAsString();
            authToken = objectMapper.readTree(response)
                    .path("data")
                    .path("accessToken")
                    .asText();
        }
    }

    @Test
    @Order(1)
    @DisplayName("Should create a new patient")
    void shouldCreateNewPatient() throws Exception {
        PatientRequest request = PatientRequest.builder()
                .firstName("Integration")
                .lastName("Test")
                .dateOfBirth(LocalDate.of(1985, 6, 15))
                .email("integration.test@example.com")
                .phone("+14155551234")
                .address("123 Test Street")
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/patients")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.firstName").value("Integration"))
                .andExpect(jsonPath("$.data.lastName").value("Test"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.medicalRecordNumber").exists())
                .andReturn();

        // Store the created patient ID for subsequent tests
        String response = result.getResponse().getContentAsString();
        createdPatientId = objectMapper.readTree(response)
                .path("data")
                .path("id")
                .asText();
    }

    @Test
    @Order(2)
    @DisplayName("Should get patient by ID")
    void shouldGetPatientById() throws Exception {
        mockMvc.perform(get("/api/v1/patients/{id}", createdPatientId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(createdPatientId))
                .andExpect(jsonPath("$.data.firstName").value("Integration"));
    }

    @Test
    @Order(3)
    @DisplayName("Should get all patients with pagination")
    void shouldGetAllPatientsWithPagination() throws Exception {
        mockMvc.perform(get("/api/v1/patients")
                        .header("Authorization", "Bearer " + authToken)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").exists());
    }

    @Test
    @Order(4)
    @DisplayName("Should search patients by name")
    void shouldSearchPatientsByName() throws Exception {
        mockMvc.perform(get("/api/v1/patients")
                        .header("Authorization", "Bearer " + authToken)
                        .param("search", "Integration"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].firstName").value("Integration"));
    }

    @Test
    @Order(5)
    @DisplayName("Should update patient")
    void shouldUpdatePatient() throws Exception {
        PatientRequest updateRequest = PatientRequest.builder()
                .firstName("Updated")
                .lastName("Patient")
                .dateOfBirth(LocalDate.of(1985, 6, 15))
                .email("updated.patient@example.com")
                .build();

        mockMvc.perform(put("/api/v1/patients/{id}", createdPatientId)
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.firstName").value("Updated"))
                .andExpect(jsonPath("$.data.lastName").value("Patient"));
    }

    @Test
    @Order(6)
    @DisplayName("Should delete patient (soft delete)")
    void shouldDeletePatient() throws Exception {
        mockMvc.perform(delete("/api/v1/patients/{id}", createdPatientId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNoContent());

        // Verify patient is soft deleted (status changed to INACTIVE)
        mockMvc.perform(get("/api/v1/patients/{id}", createdPatientId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("INACTIVE"));
    }

    @Test
    @Order(7)
    @DisplayName("Should return 404 for non-existent patient")
    void shouldReturn404ForNonExistentPatient() throws Exception {
        mockMvc.perform(get("/api/v1/patients/{id}", "00000000-0000-0000-0000-000000000000")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @Order(8)
    @DisplayName("Should return 400 for invalid patient data")
    void shouldReturn400ForInvalidPatientData() throws Exception {
        PatientRequest invalidRequest = PatientRequest.builder()
                .firstName("")  // Invalid: blank
                .lastName("Test")
                .dateOfBirth(LocalDate.now().plusDays(1))  // Invalid: future date
                .build();

        mockMvc.perform(post("/api/v1/patients")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    @Order(9)
    @DisplayName("Should return 401 without authentication")
    void shouldReturn401WithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/patients"))
                .andExpect(status().isUnauthorized());
    }
}
