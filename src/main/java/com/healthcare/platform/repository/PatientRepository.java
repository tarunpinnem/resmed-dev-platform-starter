package com.healthcare.platform.repository;

import com.healthcare.platform.entity.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Patient entity operations.
 */
@Repository
public interface PatientRepository extends JpaRepository<Patient, UUID> {

    /**
     * Find patient by medical record number.
     */
    Optional<Patient> findByMedicalRecordNumber(String medicalRecordNumber);

    /**
     * Find patient by email.
     */
    Optional<Patient> findByEmail(String email);

    /**
     * Search patients by name (first or last name contains the search term).
     */
    @Query("SELECT p FROM Patient p WHERE " +
           "LOWER(p.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Patient> searchByName(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Find all active patients.
     */
    Page<Patient> findByStatus(Patient.PatientStatus status, Pageable pageable);

    /**
     * Check if email already exists (for validation).
     */
    boolean existsByEmail(String email);

    /**
     * Check if medical record number already exists.
     */
    boolean existsByMedicalRecordNumber(String medicalRecordNumber);
}
