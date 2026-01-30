package com.healthcare.platform.service;

import com.healthcare.platform.dto.PatientRequest;
import com.healthcare.platform.dto.PatientResponse;
import com.healthcare.platform.entity.Patient;
import com.healthcare.platform.exception.ResourceNotFoundException;
import com.healthcare.platform.exception.DuplicateResourceException;
import com.healthcare.platform.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service layer for patient operations.
 * Handles business logic and transaction management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PatientService {

    private final PatientRepository patientRepository;

    /**
     * Create a new patient.
     */
    @Transactional
    public PatientResponse createPatient(PatientRequest request) {
        log.info("Creating new patient: {} {}", request.getFirstName(), request.getLastName());

        // Check for duplicate email
        if (request.getEmail() != null && patientRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Patient with email " + request.getEmail() + " already exists");
        }

        Patient patient = Patient.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .dateOfBirth(request.getDateOfBirth())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .status(Patient.PatientStatus.ACTIVE)
                .build();

        Patient savedPatient = patientRepository.save(patient);
        log.info("Patient created with ID: {}", savedPatient.getId());

        return PatientResponse.fromEntity(savedPatient);
    }

    /**
     * Get patient by ID.
     */
    public PatientResponse getPatientById(UUID id) {
        log.debug("Fetching patient with ID: {}", id);

        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "id", id.toString()));

        return PatientResponse.fromEntity(patient);
    }

    /**
     * Get all patients with pagination.
     */
    public Page<PatientResponse> getAllPatients(Pageable pageable) {
        log.debug("Fetching all patients, page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());

        return patientRepository.findAll(pageable)
                .map(PatientResponse::fromEntity);
    }

    /**
     * Search patients by name.
     */
    public Page<PatientResponse> searchPatients(String searchTerm, Pageable pageable) {
        log.debug("Searching patients with term: {}", searchTerm);

        return patientRepository.searchByName(searchTerm, pageable)
                .map(PatientResponse::fromEntity);
    }

    /**
     * Update an existing patient.
     */
    @Transactional
    public PatientResponse updatePatient(UUID id, PatientRequest request) {
        log.info("Updating patient with ID: {}", id);

        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "id", id.toString()));

        // Check for duplicate email (excluding current patient)
        if (request.getEmail() != null && !request.getEmail().equals(patient.getEmail())) {
            if (patientRepository.existsByEmail(request.getEmail())) {
                throw new DuplicateResourceException("Patient with email " + request.getEmail() + " already exists");
            }
        }

        patient.setFirstName(request.getFirstName());
        patient.setLastName(request.getLastName());
        patient.setDateOfBirth(request.getDateOfBirth());
        patient.setEmail(request.getEmail());
        patient.setPhone(request.getPhone());
        patient.setAddress(request.getAddress());

        Patient updatedPatient = patientRepository.save(patient);
        log.info("Patient updated: {}", id);

        return PatientResponse.fromEntity(updatedPatient);
    }

    /**
     * Delete a patient (soft delete by setting status to INACTIVE).
     */
    @Transactional
    public void deletePatient(UUID id) {
        log.info("Deleting patient with ID: {}", id);

        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "id", id.toString()));

        patient.setStatus(Patient.PatientStatus.INACTIVE);
        patientRepository.save(patient);
        log.info("Patient soft-deleted: {}", id);
    }

    /**
     * Get patient by medical record number.
     */
    public PatientResponse getPatientByMrn(String mrn) {
        log.debug("Fetching patient with MRN: {}", mrn);

        Patient patient = patientRepository.findByMedicalRecordNumber(mrn)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "medicalRecordNumber", mrn));

        return PatientResponse.fromEntity(patient);
    }
}
