package org.example.web_eng2;

import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.*;

@RestController
@RequestMapping("/api/v3/assets/storeys")
public class StoreyController {

    private final StoreyService storeyService;

    private final StoreyRepository storeyRepository;

    private final BuildingRepository buildingRepository;

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(StoreyController.class);

    public StoreyController(StoreyService storeyService, StoreyRepository storeyRepository, BuildingRepository buildingRepository) {
        this.storeyService = storeyService;
        this.storeyRepository = storeyRepository;
        this.buildingRepository = buildingRepository;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_view-profile')")
    public ResponseEntity<Map<String, Object>> getAllStoreys(
            @RequestParam(value = "include_deleted", defaultValue = "false") boolean includeDeleted,
            @RequestParam(value = "building_id", required = false) UUID buildingId, HttpServletRequest request) {
        logger.info("GET /storeys - Requested by User: {}, include_deleted: {}, building_id: {}", request.getRemoteUser(), includeDeleted, buildingId);
        try {
            List<Storey> allStoreys = storeyRepository.findAll();

            if (buildingId != null) {
                allStoreys = allStoreys.stream()
                        .filter(storey -> storey.getBuilding().getId().equals(buildingId))
                        .toList();
                logger.debug("Filtered by building_id: {}", buildingId);
            }

            List<Storey> filteredStoreys = includeDeleted
                    ? allStoreys
                    : allStoreys.stream()
                    .filter(storey -> storey.getDeletedAt() == null)
                    .toList();

            List<Map<String, Object>> storeyList = filteredStoreys.stream().map(storey -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", storey.getId());
                map.put("name", storey.getName());
                map.put("building_id", storey.getBuilding().getId());
                map.put("deleted_at", storey.getDeletedAt());
                return map;
            }).toList();

            Map<String, Object> response = new HashMap<>();
            response.put("storeys", storeyList);
            response.put("total_unfiltered", Double.valueOf(allStoreys.size()));

            logger.info("GET /storeys - Successfully returned {} storeys", storeyList.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("GET /storeys failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred"));
        }
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_manage-account')")
    public ResponseEntity<?> createStorey(@RequestBody Map<String, Object> payload, HttpServletRequest request) {
        logger.info("POST /storeys - Requested by User: {}, Payload: {}", request.getRemoteUser(), payload);
        try {
            String name = (String) payload.get("name");
            UUID buildingId = UUID.fromString((String) payload.get("building_id"));

            Optional<Building> buildingOptional = buildingRepository.findById(buildingId);
            if (buildingOptional.isEmpty()) {
                logger.warn("POST /storeys - Building not found for ID: {}", buildingId);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Building not found"));
            }

            Building building = buildingOptional.get();
            if (building.getDeletedAt() != null) {
                logger.warn("POST /storeys - Building is deleted: {}", buildingId);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Building is deleted"));
            }

            Storey storey = new Storey();
            storey.setName(name);
            storey.setBuilding(building);

            Storey savedStorey = storeyRepository.save(storey);

            Map<String, Object> response = new HashMap<>();
            response.put("id", savedStorey.getId());
            response.put("name", savedStorey.getName());
            response.put("building_id", savedStorey.getBuilding().getId());
            response.put("deleted_at", savedStorey.getDeletedAt());

            logger.info("POST /storeys - Successfully created Storey with ID: {}", savedStorey.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            logger.error("POST /storeys - Invalid input", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Invalid input"));
        } catch (Exception e) {
            logger.error("POST /storeys failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Server error"));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getStoreyById(@PathVariable("id") UUID id, HttpServletRequest request) {
        logger.info("GET /storeys/{} - Requested by User: {}", id, request.getRemoteUser());
        try {
            Optional<Storey> storeyOptional = storeyRepository.findById(id);

            if (storeyOptional.isPresent()) {
                Storey storey = storeyOptional.get();

                Map<String, Object> response = new HashMap<>();
                response.put("id", storey.getId());
                response.put("name", storey.getName());
                response.put("building_id", storey.getBuilding().getId());
                response.put("deleted_at", storey.getDeletedAt());

                logger.info("GET /storeys/{} - Successfully retrieved Storey", id);
                return ResponseEntity.ok(response);
            } else {
                logger.warn("GET /storeys/{} - Storey not found", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Storey not found", "id", id.toString()));
            }
        } catch (Exception e) {
            logger.error("GET /storeys/{} failed", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred"));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_manage-account')")
    public ResponseEntity<?> updateOrCreateStorey(
            @PathVariable String id, @RequestBody Map<String, Object> payload, HttpServletRequest request) {
        logger.info("PUT /storeys/{} - Requested by User: {}, Payload: {}", id, request.getRemoteUser(), payload);
        try {
            UUID storeyId = UUID.fromString(id);

            String buildingIdStr = (String) payload.get("building_id");
            if (buildingIdStr == null || buildingIdStr.isEmpty()) {
                logger.warn("PUT /storeys/{} - Building ID is required", id);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Building ID is required"));
            }

            UUID buildingId;
            try {
                buildingId = UUID.fromString(buildingIdStr);
            } catch (IllegalArgumentException e) {
                logger.warn("PUT /storeys/{} - Invalid Building ID format", id);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Invalid Building ID format"));
            }

            Optional<Building> optionalBuilding = buildingRepository.findById(buildingId);
            if (optionalBuilding.isEmpty()) {
                logger.warn("PUT /storeys/{} - Building does not exist", id);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Building does not exist"));
            }
            Building building = optionalBuilding.get();

            String name = (String) payload.get("name");
            if (name == null || name.isEmpty()) {
                logger.warn("PUT /storeys/{} - Name is required", id);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Name is required"));
            }

            Optional<Storey> optionalStorey = storeyRepository.findById(storeyId);
            if (optionalStorey.isEmpty()) {
                Storey newStorey = new Storey();
                newStorey.setId(storeyId);
                newStorey.setName(name);
                newStorey.setBuilding(building);
                newStorey.setDeletedAt(null);

                storeyRepository.saveAndFlush(newStorey);

                URI location = ServletUriComponentsBuilder
                        .fromCurrentRequest()
                        .buildAndExpand(newStorey.getId())
                        .toUri();

                Map<String, Object> response = Map.of(
                        "id", newStorey.getId(),
                        "name", newStorey.getName(),
                        "building_id", newStorey.getBuilding().getId()
                );

                logger.info("PUT /storeys/{} - Created new Storey", id);
                return ResponseEntity.created(location).body(response);
            } else {
                Storey existingStorey = optionalStorey.get();
                existingStorey.setName(name);
                existingStorey.setBuilding(building);

                if (existingStorey.getDeletedAt() != null) {
                    existingStorey.setDeletedAt(null);
                }

                storeyRepository.saveAndFlush(existingStorey);

                Map<String, Object> response = Map.of(
                        "id", existingStorey.getId(),
                        "name", existingStorey.getName(),
                        "building_id", existingStorey.getBuilding().getId()
                );

                logger.info("PUT /storeys/{} - Updated existing Storey", id);
                return ResponseEntity.ok(response);
            }
        } catch (Exception e) {
            logger.error("PUT /storeys/{} failed", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred"));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_manage-account')")
    public ResponseEntity<?> deleteStorey(
            @PathVariable UUID id,
            @RequestParam(value = "permanent", defaultValue = "false") boolean permanent, HttpServletRequest request) {
        logger.info("DELETE /storeys/{} - Requested by User: {}, Permanent: {}", id, request.getRemoteUser(), permanent);
        try {
            Optional<Storey> storeyOptional = storeyRepository.findById(id);
            if (storeyOptional.isEmpty()) {
                logger.warn("DELETE /storeys/{} - Storey not found", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Storey not found"));
            }

            Storey storey = storeyOptional.get();

            if (permanent) {
                storeyRepository.delete(storey);
                logger.info("DELETE /storeys/{} - Permanently deleted Storey", id);
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            } else {
                if (storey.getDeletedAt() != null) {
                    logger.warn("DELETE /storeys/{} - Storey is already soft-deleted", id);
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(Map.of("error", "Storey is already soft-deleted"));
                }
                storey.setDeletedAt(java.time.Instant.now());
                storeyRepository.save(storey);
                logger.info("DELETE /storeys/{} - Soft-deleted Storey", id);
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            }
        } catch (Exception e) {
            logger.error("DELETE /storeys/{} failed", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred"));
        }
    }
}
