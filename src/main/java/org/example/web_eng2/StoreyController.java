package org.example.web_eng2;

import jakarta.transaction.Transactional;
import org.example.web_eng2.Storey;
import org.example.web_eng2.StoreyService;
import org.example.web_eng2.repository.StoreyRepository;
import org.example.web_eng2.repository.BuildingRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.example.web_eng2.repository.BuildingRepository;
import org.springframework.web.server.ResponseStatusException;


import java.util.*;

@RestController
@RequestMapping("/api/v3/assets/storeys")
public class StoreyController {

    private final StoreyService storeyService;

    private final StoreyRepository storeyRepository;

    private final BuildingRepository buildingRepository;


    public StoreyController(StoreyService storeyService, StoreyRepository storeyRepository, BuildingRepository buildingRepository) {
        this.storeyService = storeyService;
        this.storeyRepository = storeyRepository;
        this.buildingRepository = buildingRepository;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_view-profile')")
    public ResponseEntity<List<Map<String, Object>>> getAllStoreys(
            @RequestParam(value = "include_deleted", defaultValue = "false") boolean includeDeleted) {
        List<Storey> storeys = storeyService.getAllStoreys(includeDeleted);

        List<Map<String, Object>> response = storeys.stream().map(storey -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", storey.getId());
            map.put("name", storey.getName());
            map.put("building_id", storey.getBuilding().getId());
            map.put("deleted_at", storey.getDeletedAt());
            return map;
        }).toList();

        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_manage-account')")
    public ResponseEntity<?> createStorey(@RequestBody Map<String, Object> payload) {
        try {
            String name = (String) payload.get("name");
            UUID buildingId = UUID.fromString((String) payload.get("building_id"));

            Storey storey = storeyService.createStorey(name, buildingId);
            return ResponseEntity.status(HttpStatus.CREATED).body(storey);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred");
        }
    }




    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_manage-account')")
    public ResponseEntity<?> updateOrCreateStorey(
            @PathVariable UUID id,
            @RequestBody Map<String, Object> payload) {
        try {
            // Name extrahieren und prüfen
            String name = (String) payload.get("name");
            if (name == null || name.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Name is required"));
            }

            // Deleted At extrahieren (optional)
            String deletedAtStr = (String) payload.get("deleted_at");
            java.time.Instant deletedAt = (deletedAtStr != null && !deletedAtStr.isEmpty())
                    ? java.time.Instant.parse(deletedAtStr)
                    : null;

            // Storey aus der Datenbank laden oder neu erstellen
            Storey storey = storeyRepository.findById(id).orElse(new Storey());
            storey.setId(id);
            storey.setName(name);
            storey.setDeletedAt(deletedAt);

            // Zugehörige Building ID aus der Datenbank prüfen
            if (storey.getBuilding() != null) {
                UUID buildingId = storey.getBuilding().getId(); // Fremdschlüssel aus der Relationship
                System.out.println("Building ID: " + buildingId);
            } else {
                System.out.println("No Building associated with this Storey");
            }

            // Speichern
            Storey savedStorey = storeyRepository.save(storey);

            // Response vorbereiten
            Map<String, Object> response = new HashMap<>();
            response.put("id", savedStorey.getId());
            response.put("name", savedStorey.getName());
            response.put("building_id", savedStorey.getBuilding() != null ? savedStorey.getBuilding().getId() : null);
            response.put("deleted_at", savedStorey.getDeletedAt());

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred"));
        }
    }



    @GetMapping("/{id}")
    public ResponseEntity<?> getStoreyById(@PathVariable("id") UUID id) {
        Optional<Storey> storeyOptional = storeyRepository.findById(id);

        if (storeyOptional.isPresent()) {
            Storey storey = storeyOptional.get();

            // Manuelle Filterung der benötigten Felder
            Map<String, Object> response = new HashMap<>();
            response.put("id", storey.getId());
            response.put("name", storey.getName());
            response.put("building_id", storey.getBuilding().getId());
            response.put("deleted_at", storey.getDeletedAt());

            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Storey not found", "id", id.toString()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_manage-account')")
    public ResponseEntity<?> deleteStorey(
            @PathVariable UUID id,
            @RequestParam(value = "permanent", defaultValue = "false") boolean permanent) {
        try {
            // Storey aus der Datenbank laden
            Optional<Storey> storeyOptional = storeyRepository.findById(id);
            if (storeyOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Storey not found"));
            }

            Storey storey = storeyOptional.get();

            if (permanent) {
                // Permanente Löschung
                storeyRepository.delete(storey);
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            } else {
                // Soft-Löschung
                if (storey.getDeletedAt() != null) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(Map.of("error", "Storey is already soft-deleted"));
                }
                storey.setDeletedAt(java.time.Instant.now());
                storeyRepository.save(storey);
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred"));
        }
    }





}



