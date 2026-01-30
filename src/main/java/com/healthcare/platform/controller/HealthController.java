package com.healthcare.platform.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.availability.ApplicationAvailability;
import org.springframework.boot.availability.LivenessState;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Health and readiness endpoints for Kubernetes probes and monitoring.
 * Follows cloud-native health check patterns.
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Health", description = "Health and readiness endpoints")
public class HealthController {

    private final ApplicationAvailability availability;
    private final DataSource dataSource;

    @GetMapping("/health")
    @Operation(summary = "Liveness probe", description = "Returns the liveness status of the application")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "UP");
        response.put("timestamp", Instant.now().toString());

        LivenessState livenessState = availability.getLivenessState();
        response.put("liveness", livenessState.name());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/ready")
    @Operation(summary = "Readiness probe", description = "Returns the readiness status including dependency checks")
    public ResponseEntity<Map<String, Object>> ready() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("timestamp", Instant.now().toString());

        ReadinessState readinessState = availability.getReadinessState();
        boolean databaseReady = checkDatabaseConnection();

        Map<String, Object> checks = new LinkedHashMap<>();
        checks.put("database", databaseReady ? "UP" : "DOWN");
        checks.put("readiness", readinessState.name());

        boolean isReady = databaseReady && readinessState == ReadinessState.ACCEPTING_TRAFFIC;
        response.put("status", isReady ? "UP" : "DOWN");
        response.put("checks", checks);

        if (!isReady) {
            return ResponseEntity.status(503).body(response);
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/info")
    @Operation(summary = "Application info", description = "Returns application metadata and version information")
    public ResponseEntity<Map<String, Object>> info() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("name", "Healthcare Platform Starter Kit");
        response.put("version", "1.0.0");
        response.put("description", "Cloud-native healthcare service template");
        response.put("timestamp", Instant.now().toString());

        Map<String, String> build = new LinkedHashMap<>();
        build.put("java", System.getProperty("java.version"));
        build.put("springBoot", "3.2.1");
        response.put("build", build);

        return ResponseEntity.ok(response);
    }

    private boolean checkDatabaseConnection() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(2);
        } catch (Exception e) {
            log.warn("Database connection check failed: {}", e.getMessage());
            return false;
        }
    }
}
