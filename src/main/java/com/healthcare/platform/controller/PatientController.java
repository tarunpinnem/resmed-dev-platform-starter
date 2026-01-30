package com.healthcare.platform.controller;

import com.healthcare.platform.dto.ApiResponse;
import com.healthcare.platform.dto.PatientRequest;
import com.healthcare.platform.dto.PatientResponse;
import com.healthcare.platform.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for patient management operations.
 * Demonstrates RESTful API design with proper documentation.
 */
@RestController
@RequestMapping("/api/v1/patients")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Patients", description = "Patient management APIs")
@SecurityRequirement(name = "bearerAuth")
public class PatientController {

    private final PatientService patientService;

    @PostMapping
    @Operation(summary = "Create a new patient", description = "Creates a new patient record in the system")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Patient created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Patient with same email already exists"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ApiResponse<PatientResponse>> createPatient(
            @Valid @RequestBody PatientRequest request) {
        log.info("REST request to create patient");
        PatientResponse patient = patientService.createPatient(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Patient created successfully", patient));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get patient by ID", description = "Retrieves a patient by their unique identifier")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Patient found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Patient not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ApiResponse<PatientResponse>> getPatientById(
            @Parameter(description = "Patient ID", required = true)
            @PathVariable UUID id) {
        log.info("REST request to get patient: {}", id);
        PatientResponse patient = patientService.getPatientById(id);
        return ResponseEntity.ok(ApiResponse.success(patient));
    }

    @GetMapping
    @Operation(summary = "Get all patients", description = "Retrieves all patients with pagination support")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Patients retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ApiResponse<Page<PatientResponse>>> getAllPatients(
            @Parameter(description = "Search term for filtering by name")
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "lastName") Pageable pageable) {
        log.info("REST request to get all patients, search: {}", search);

        Page<PatientResponse> patients;
        if (search != null && !search.isBlank()) {
            patients = patientService.searchPatients(search, pageable);
        } else {
            patients = patientService.getAllPatients(pageable);
        }

        return ResponseEntity.ok(ApiResponse.success(patients));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a patient", description = "Updates an existing patient record")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Patient updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Patient not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ApiResponse<PatientResponse>> updatePatient(
            @Parameter(description = "Patient ID", required = true)
            @PathVariable UUID id,
            @Valid @RequestBody PatientRequest request) {
        log.info("REST request to update patient: {}", id);
        PatientResponse patient = patientService.updatePatient(id, request);
        return ResponseEntity.ok(ApiResponse.success("Patient updated successfully", patient));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a patient", description = "Soft deletes a patient by setting status to INACTIVE")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Patient deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Patient not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Void> deletePatient(
            @Parameter(description = "Patient ID", required = true)
            @PathVariable UUID id) {
        log.info("REST request to delete patient: {}", id);
        patientService.deletePatient(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/mrn/{mrn}")
    @Operation(summary = "Get patient by MRN", description = "Retrieves a patient by their Medical Record Number")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Patient found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Patient not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ApiResponse<PatientResponse>> getPatientByMrn(
            @Parameter(description = "Medical Record Number", required = true)
            @PathVariable String mrn) {
        log.info("REST request to get patient by MRN: {}", mrn);
        PatientResponse patient = patientService.getPatientByMrn(mrn);
        return ResponseEntity.ok(ApiResponse.success(patient));
    }
}
