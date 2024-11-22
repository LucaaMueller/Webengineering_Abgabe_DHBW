package org.example.web_eng2;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v3/assets")
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getHealthStatus() {
        Map<String, Object> healthStatus = new HashMap<>();
        healthStatus.put("live", true);
        healthStatus.put("ready", true);
        Map<String, Object> databases = new HashMap<>();
        databases.put("connected", true);
        healthStatus.put("databases", Map.of("assets", databases));

        return ResponseEntity.ok(healthStatus);
    }


    @GetMapping("/health/live")
    public ResponseEntity<Map<String, Object>> getLiveStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("live", true);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health/ready")
    public ResponseEntity<Map<String, Object>> getReadyStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("ready", true);
        return ResponseEntity.ok(response);
    }




}