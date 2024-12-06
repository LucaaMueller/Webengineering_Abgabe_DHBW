package org.example.web_eng2;

import org.example.web_eng2.repository.BuildingRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v3/assets")
public class HealthController {

    private final BuildingRepository buildingRepository;

    public HealthController(BuildingRepository buildingRepository) {
        this.buildingRepository = buildingRepository;
    }

    @GetMapping("/health")
    public ResponseEntity<Object> getHealthStatus() {
        boolean isDbConnected;

        try {
            // Überprüfe die Datenbankverbindung mit einem Testzugriff
            buildingRepository.count();
            isDbConnected = true;
        } catch (Exception e) {
            isDbConnected = false;
        }

        // Health-Informationen direkt in eine Map packen
        Map<String, Object> healthStatus = new HashMap<>();
        healthStatus.put("live", true); // Der Service läuft
        healthStatus.put("ready", isDbConnected); // Ready-Status basiert auf DB-Verbindung

        Map<String, Object> databases = new HashMap<>();
        Map<String, Object> assetsDb = new HashMap<>();
        assetsDb.put("connected", isDbConnected);

        if (!isDbConnected) {
            assetsDb.put("error", "Database connection could not be established.");
        }

        databases.put("assets", assetsDb);
        healthStatus.put("databases", databases);

        // Rückgabe abhängig vom Ready-Status
        if (isDbConnected) {
            return ResponseEntity.ok(healthStatus);
        } else {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(healthStatus);
        }
    }

    @GetMapping("/health/live")
    public ResponseEntity<Map<String, Object>> getLiveStatus() {
        // Der Live-Status ist immer true, da der Service läuft
        Map<String, Object> response = new HashMap<>();
        response.put("live", true);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health/ready")
    public ResponseEntity<Map<String, Object>> getReadyStatus() {
        boolean isDbConnected;

        try {
            buildingRepository.count(); // Überprüfe die Datenbankverbindung
            isDbConnected = true;
        } catch (Exception e) {
            isDbConnected = false;
        }

        Map<String, Object> response = new HashMap<>();
        response.put("ready", isDbConnected);

        if (!isDbConnected) {
            response.put("error", "Database connection could not be established.");
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
        }

        return ResponseEntity.ok(response);
    }
}
