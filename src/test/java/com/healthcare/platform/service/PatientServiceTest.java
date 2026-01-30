package com.healthcare.platform.service;

import com.healthcare.platform.dto.PatientRequest;
import com.healthcare.platform.dto.PatientResponse;
import com.healthcare.platform.entity.Patient;
import com.healthcare.platform.exception.DuplicateResourceException;
import com.healthcare.platform.exception.ResourceNotFoundException;
import com.healthcare.platform.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Patient Service Tests")
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private PatientService patientService;

    private PatientRequest validRequest;
    private Patient samplePatient;
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

        samplePatient = Patient.builder()
                .id(patientId)
                .firstName("John")
                .lastName("Doe")
                .dateOfBirth(LocalDate.of(1990, 5, 15))
                .email("john.doe@example.com")
                .phone("+14155551234")
                .address("123 Healthcare Ave")
                .medicalRecordNumber("MRN-123456")
                .status(Patient.PatientStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("createPatient")
    class CreatePatient {

        @Test
        @DisplayName("should create patient successfully")
        void shouldCreatePatientSuccessfully() {
            when(patientRepository.existsByEmail(anyString())).thenReturn(false);
            when(patientRepository.save(any(Patient.class))).thenReturn(samplePatient);

            PatientResponse result = patientService.createPatient(validRequest);

            assertThat(result).isNotNull();
            assertThat(result.getFirstName()).isEqualTo("John");
            assertThat(result.getLastName()).isEqualTo("Doe");
            assertThat(result.getEmail()).isEqualTo("john.doe@example.com");

            ArgumentCaptor<Patient> patientCaptor = ArgumentCaptor.forClass(Patient.class);
            verify(patientRepository).save(patientCaptor.capture());

            Patient savedPatient = patientCaptor.getValue();
            assertThat(savedPatient.getFirstName()).isEqualTo("John");
            assertThat(savedPatient.getStatus()).isEqualTo(Patient.PatientStatus.ACTIVE);
        }

        @Test
        @DisplayName("should throw DuplicateResourceException for existing email")
        void shouldThrowExceptionForDuplicateEmail() {
            when(patientRepository.existsByEmail("john.doe@example.com")).thenReturn(true);

            assertThatThrownBy(() -> patientService.createPatient(validRequest))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("john.doe@example.com");

            verify(patientRepository, never()).save(any());
        }

        @Test
        @DisplayName("should create patient without email")
        void shouldCreatePatientWithoutEmail() {
            PatientRequest requestWithoutEmail = PatientRequest.builder()
                    .firstName("Jane")
                    .lastName("Smith")
                    .dateOfBirth(LocalDate.of(1985, 3, 20))
                    .build();

            Patient patientWithoutEmail = Patient.builder()
                    .id(UUID.randomUUID())
                    .firstName("Jane")
                    .lastName("Smith")
                    .dateOfBirth(LocalDate.of(1985, 3, 20))
                    .status(Patient.PatientStatus.ACTIVE)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            when(patientRepository.save(any(Patient.class))).thenReturn(patientWithoutEmail);

            PatientResponse result = patientService.createPatient(requestWithoutEmail);

            assertThat(result).isNotNull();
            assertThat(result.getFirstName()).isEqualTo("Jane");
            verify(patientRepository, never()).existsByEmail(anyString());
        }
    }

    @Nested
    @DisplayName("getPatientById")
    class GetPatientById {

        @Test
        @DisplayName("should return patient by ID")
        void shouldReturnPatientById() {
            when(patientRepository.findById(patientId)).thenReturn(Optional.of(samplePatient));

            PatientResponse result = patientService.getPatientById(patientId);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(patientId);
            assertThat(result.getFirstName()).isEqualTo("John");
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException for non-existent patient")
        void shouldThrowExceptionForNonExistentPatient() {
            UUID nonExistentId = UUID.randomUUID();
            when(patientRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> patientService.getPatientById(nonExistentId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Patient")
                    .hasMessageContaining("id");
        }
    }

    @Nested
    @DisplayName("getAllPatients")
    class GetAllPatients {

        @Test
        @DisplayName("should return paginated patients")
        void shouldReturnPaginatedPatients() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Patient> patientPage = new PageImpl<>(List.of(samplePatient));

            when(patientRepository.findAll(pageable)).thenReturn(patientPage);

            Page<PatientResponse> result = patientService.getAllPatients(pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getFirstName()).isEqualTo("John");
        }
    }

    @Nested
    @DisplayName("updatePatient")
    class UpdatePatient {

        @Test
        @DisplayName("should update patient successfully")
        void shouldUpdatePatientSuccessfully() {
            PatientRequest updateRequest = PatientRequest.builder()
                    .firstName("Jane")
                    .lastName("Doe")
                    .dateOfBirth(LocalDate.of(1990, 5, 15))
                    .email("jane.doe@example.com")
                    .build();

            Patient updatedPatient = Patient.builder()
                    .id(patientId)
                    .firstName("Jane")
                    .lastName("Doe")
                    .dateOfBirth(LocalDate.of(1990, 5, 15))
                    .email("jane.doe@example.com")
                    .status(Patient.PatientStatus.ACTIVE)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            when(patientRepository.findById(patientId)).thenReturn(Optional.of(samplePatient));
            when(patientRepository.existsByEmail("jane.doe@example.com")).thenReturn(false);
            when(patientRepository.save(any(Patient.class))).thenReturn(updatedPatient);

            PatientResponse result = patientService.updatePatient(patientId, updateRequest);

            assertThat(result.getFirstName()).isEqualTo("Jane");
            assertThat(result.getEmail()).isEqualTo("jane.doe@example.com");
        }

        @Test
        @DisplayName("should throw exception when updating to existing email")
        void shouldThrowExceptionForDuplicateEmailOnUpdate() {
            PatientRequest updateRequest = PatientRequest.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .dateOfBirth(LocalDate.of(1990, 5, 15))
                    .email("existing@example.com")
                    .build();

            when(patientRepository.findById(patientId)).thenReturn(Optional.of(samplePatient));
            when(patientRepository.existsByEmail("existing@example.com")).thenReturn(true);

            assertThatThrownBy(() -> patientService.updatePatient(patientId, updateRequest))
                    .isInstanceOf(DuplicateResourceException.class);
        }
    }

    @Nested
    @DisplayName("deletePatient")
    class DeletePatient {

        @Test
        @DisplayName("should soft delete patient")
        void shouldSoftDeletePatient() {
            when(patientRepository.findById(patientId)).thenReturn(Optional.of(samplePatient));
            when(patientRepository.save(any(Patient.class))).thenReturn(samplePatient);

            patientService.deletePatient(patientId);

            ArgumentCaptor<Patient> patientCaptor = ArgumentCaptor.forClass(Patient.class);
            verify(patientRepository).save(patientCaptor.capture());

            assertThat(patientCaptor.getValue().getStatus()).isEqualTo(Patient.PatientStatus.INACTIVE);
        }
    }
}
