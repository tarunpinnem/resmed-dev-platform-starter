package com.healthcare.platform.dto;

import com.healthcare.platform.entity.Patient;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for patient data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Patient response")
public class PatientResponse {

    @Schema(description = "Unique patient identifier", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "Patient's first name", example = "John")
    private String firstName;

    @Schema(description = "Patient's last name", example = "Doe")
    private String lastName;

    @Schema(description = "Patient's date of birth", example = "1990-05-15")
    private LocalDate dateOfBirth;

    @Schema(description = "Patient's email address", example = "john.doe@example.com")
    private String email;

    @Schema(description = "Patient's phone number", example = "+14155551234")
    private String phone;

    @Schema(description = "Patient's address", example = "123 Healthcare Ave, Medical City, MC 12345")
    private String address;

    @Schema(description = "Medical record number", example = "MRN-1234567890")
    private String medicalRecordNumber;

    @Schema(description = "Patient status", example = "ACTIVE")
    private String status;

    @Schema(description = "Record creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Record last update timestamp")
    private LocalDateTime updatedAt;

    /**
     * Convert Patient entity to response DTO.
     */
    public static PatientResponse fromEntity(Patient patient) {
        return PatientResponse.builder()
                .id(patient.getId())
                .firstName(patient.getFirstName())
                .lastName(patient.getLastName())
                .dateOfBirth(patient.getDateOfBirth())
                .email(patient.getEmail())
                .phone(patient.getPhone())
                .address(patient.getAddress())
                .medicalRecordNumber(patient.getMedicalRecordNumber())
                .status(patient.getStatus().name())
                .createdAt(patient.getCreatedAt())
                .updatedAt(patient.getUpdatedAt())
                .build();
    }
}
