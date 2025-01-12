package org.example.web_eng2;

import jakarta.servlet.http.HttpServletRequest;
import org.example.web_eng2.Storey;
import org.example.web_eng2.StoreyService;
import org.example.web_eng2.repository.StoreyRepository;
import org.example.web_eng2.repository.BuildingRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
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
            @RequestParam(value = "building_id", required = false) UUID buildingId) {
        logger.info("GET /storeys - Retrieving all storeys, include_deleted={}, building_id={}", includeDeleted, buildingId);
        try {
            List<Storey> allStoreys = storeyRepository.findAll();

            if (buildingId != null) {
                allStoreys = allStoreys.stream()
                        .filter(storey -> storey.getBuilding().getId().equals(buildingId))
                        .toList();
                logger.info("Filtered storeys by building_id={}", buildingId);
            }

            List<Storey> filteredStoreys = includeDeleted
                    ? allStoreys
                    : allStoreys.stream().filter(storey -> storey.getDeletedAt() == null).toList();

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

            logger.info("GET /storeys - Successfully retrieved {} storeys", storeyList.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("GET /storeys failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred", "details", e.getMessage()));
        }
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_manage-account')")
    public ResponseEntity<?> createStorey(@RequestBody Map<String, Object> payload, HttpServletRequest request) {
        logger.info("POST /storeys - Creating a new storey, requested by User: {}", request.getRemoteUser());
        try {
            String name = (String) payload.get("name");
            UUID buildingId = UUID.fromString((String) payload.get("building_id"));

            Optional<Building> buildingOptional = buildingRepository.findById(buildingId);
            if (buildingOptional.isEmpty()) {
                logger.warn("POST /storeys - Building with id={} not found", buildingId);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Building not found", "message", "The specified building does not exist"));
            }

            Building building = buildingOptional.get();
            if (building.getDeletedAt() != null) {
                logger.warn("POST /storeys - Cannot create storey for deleted building with id={}", buildingId);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Building is deleted", "message", "Cannot create a storey for a deleted building"));
            }

            Storey storey = new Storey();
            storey.setName(name);
            storey.setBuilding(building);

            Storey savedStorey = storeyRepository.save(storey);

            logger.info("POST /storeys - Successfully created new storey with id={}", savedStorey.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "id", savedStorey.getId(),
                    "name", savedStorey.getName(),
                    "building_id", savedStorey.getBuilding().getId()
            ));
        } catch (Exception e) {
            logger.error("POST /storeys failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred", "details", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_manage-account')")
    public ResponseEntity<?> updateOrCreateStorey(
            @PathVariable String id, @RequestBody Map<String, Object> payload, HttpServletRequest request) {
        logger.info("PUT /storeys/{} - Requested by User: {}", id, request.getRemoteUser());
        try {
            UUID storeyId = UUID.fromString(id);

            String buildingIdStr = (String) payload.get("building_id");
            if (buildingIdStr == null || buildingIdStr.isEmpty()) {
                logger.warn("PUT /storeys/{} - Building ID is missing", id);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Building ID is required"));
            }

            UUID buildingId = UUID.fromString(buildingIdStr);
            Optional<Building> optionalBuilding = buildingRepository.findById(buildingId);
            if (optionalBuilding.isEmpty()) {
                logger.warn("PUT /storeys/{} - Building with id={} does not exist", id, buildingId);
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

                storeyRepository.save(newStorey);

                URI location = ServletUriComponentsBuilder
                        .fromCurrentRequest()
                        .buildAndExpand(newStorey.getId())
                        .toUri();

                logger.info("PUT /storeys/{} - Created new Storey", id);
                return ResponseEntity.created(location).body(Map.of(
                        "id", newStorey.getId(),
                        "name", newStorey.getName(),
                        "building_id", newStorey.getBuilding().getId()
                ));
            } else {
                Storey existingStorey = optionalStorey.get();
                existingStorey.setName(name);
                existingStorey.setBuilding(building);

                storeyRepository.save(existingStorey);

                logger.info("PUT /storeys/{} - Updated existing Storey", id);
                return ResponseEntity.ok(Map.of(
                        "id", existingStorey.getId(),
                        "name", existingStorey.getName(),
                        "building_id", existingStorey.getBuilding().getId()
                ));
            }
        } catch (Exception e) {
            logger.error("PUT /storeys/{} failed", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred", "details", e.getMessage()));
        }
    }
}
