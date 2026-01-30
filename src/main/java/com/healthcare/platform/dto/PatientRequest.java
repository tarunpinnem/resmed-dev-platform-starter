package com.healthcare.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Request DTO for creating or updating a patient.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Patient creation/update request")
public class PatientRequest {

    @NotBlank(message = "First name is required")
    @Size(min = 1, max = 100, message = "First name must be between 1 and 100 characters")
    @Schema(description = "Patient's first name", example = "John", requiredMode = Schema.RequiredMode.REQUIRED)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 1, max = 100, message = "Last name must be between 1 and 100 characters")
    @Schema(description = "Patient's last name", example = "Doe", requiredMode = Schema.RequiredMode.REQUIRED)
    private String lastName;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    @Schema(description = "Patient's date of birth", example = "1990-05-15", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate dateOfBirth;

    @Email(message = "Email must be valid")
    @Schema(description = "Patient's email address", example = "john.doe@example.com")
    private String email;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Phone number must be valid E.164 format")
    @Schema(description = "Patient's phone number in E.164 format", example = "+14155551234")
    private String phone;

    @Size(max = 500, message = "Address must not exceed 500 characters")
    @Schema(description = "Patient's address", example = "123 Healthcare Ave, Medical City, MC 12345")
    private String address;
}
