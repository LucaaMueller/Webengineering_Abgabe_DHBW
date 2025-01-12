package org.example.web_eng2;

import jakarta.servlet.http.HttpServletRequest;
import org.example.web_eng2.repository.BuildingRepository;
import org.example.web_eng2.repository.StoreyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.*;

@RestController
@RequestMapping("/api/v3/assets/buildings")
public class BuildingController {

    private static final Logger logger = LoggerFactory.getLogger(BuildingController.class);

    private final BuildingRepository buildingRepository;
    private final StoreyRepository storeyRepository;

    public BuildingController(BuildingRepository buildingRepository, StoreyRepository storeyRepository) {
        this.buildingRepository = buildingRepository;
        this.storeyRepository = storeyRepository;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_view-profile')")
    public ResponseEntity<Map<String, Object>> getAllBuildings(
            @RequestParam(value = "include_deleted", defaultValue = "false") boolean includeDeleted,
            HttpServletRequest request) {
        try {
            List<Building> buildings = includeDeleted
                    ? buildingRepository.findAll()
                    : buildingRepository.findByDeletedAtIsNull();

            logger.info("GET /buildings - include_deleted={}, requested by User: {}", includeDeleted, request.getRemoteUser());

            Map<String, Object> response = new HashMap<>();
            response.put("buildings", buildings);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("GET /buildings failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_manage-account')")
    public ResponseEntity<Building> createBuilding(@Valid @RequestBody Building building, HttpServletRequest request) {
        try {
            Building savedBuilding = buildingRepository.save(building);

            logger.info("POST /buildings - Created Building with ID: {}, requested by User: {}", savedBuilding.getId(), request.getRemoteUser());

            String location = request.getRequestURL().toString() + "/" + savedBuilding.getId();
            return ResponseEntity.status(HttpStatus.CREATED).header("Location", location).body(savedBuilding);
        } catch (Exception e) {
            logger.error("POST /buildings failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Building> getBuildingById(@PathVariable UUID id, HttpServletRequest request) {
        try {
            logger.info("GET /buildings/{} - requested by User: {}", id, request.getRemoteUser());

            return buildingRepository.findById(id)
                    .map(building -> ResponseEntity.ok(building))
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        } catch (Exception e) {
            logger.error("GET /buildings/{} failed", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_manage-account')")
    public ResponseEntity<?> updateOrCreateBuilding(
            @PathVariable String id, @Valid @RequestBody Building inputBuilding, HttpServletRequest request) {
        try {
            UUID uuid = UUID.fromString(id);
            Optional<Building> optionalBuilding = buildingRepository.findById(uuid);

            if (optionalBuilding.isEmpty()) {
                Building newBuilding = new Building();
                newBuilding.setId(uuid);
                newBuilding.setName(inputBuilding.getName());
                newBuilding.setStreetname(inputBuilding.getStreetname());
                newBuilding.setHousenumber(inputBuilding.getHousenumber());
                newBuilding.setcountryCode(inputBuilding.getCountryCode());
                newBuilding.setPostalcode(inputBuilding.getPostalcode());
                newBuilding.setCity(inputBuilding.getCity());
                newBuilding.setDeletedAt(null);

                buildingRepository.save(newBuilding);

                URI location = ServletUriComponentsBuilder
                        .fromCurrentRequest()
                        .buildAndExpand(newBuilding.getId())
                        .toUri();

                logger.info("PUT /buildings/{} - Created new Building, requested by User: {}", id, request.getRemoteUser());

                return ResponseEntity.created(location).body(newBuilding);
            } else {
                Building existingBuilding = optionalBuilding.get();
                existingBuilding.setName(inputBuilding.getName());
                existingBuilding.setStreetname(inputBuilding.getStreetname());
                existingBuilding.setHousenumber(inputBuilding.getHousenumber());
                existingBuilding.setcountryCode(inputBuilding.getCountryCode());
                existingBuilding.setPostalcode(inputBuilding.getPostalcode());
                existingBuilding.setCity(inputBuilding.getCity());

                if (existingBuilding.getDeletedAt() != null) {
                    existingBuilding.setDeletedAt(null);
                }

                buildingRepository.save(existingBuilding);

                logger.info("PUT /buildings/{} - Updated existing Building, requested by User: {}", id, request.getRemoteUser());

                return ResponseEntity.ok(existingBuilding);
            }
        } catch (Exception e) {
            logger.error("PUT /buildings/{} failed", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred", "details", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBuilding(
            @PathVariable UUID id,
            @RequestParam(value = "permanent", defaultValue = "false") boolean permanent,
            HttpServletRequest request) {
        try {
            return buildingRepository.findById(id)
                    .map(building -> {
                        List<Storey> activeStoreys = storeyRepository.findByBuildingAndDeletedAtIsNull(building);
                        if (!activeStoreys.isEmpty()) {
                            logger.warn("DELETE /buildings/{} - Attempt to delete Building with active storeys, requested by User: {}", id, request.getRemoteUser());
                            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                    .body(Map.of(
                                            "error", "Building has active storeys",
                                            "message", "Cannot delete a building with active storeys"
                                    ));
                        }

                        if (permanent) {
                            buildingRepository.delete(building);
                            logger.info("DELETE /buildings/{} - Permanently deleted, requested by User: {}", id, request.getRemoteUser());
                            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
                        } else {
                            building.setDeletedAt(java.time.OffsetDateTime.now());
                            buildingRepository.save(building);
                            logger.info("DELETE /buildings/{} - Soft-deleted, requested by User: {}", id, request.getRemoteUser());
                            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
                        }
                    })
                    .orElseGet(() -> {
                        logger.warn("DELETE /buildings/{} - Building not found, requested by User: {}", id, request.getRemoteUser());
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(Map.of(
                                        "error", "Building not found",
                                        "message", "The specified building does not exist"
                                ));
                    });
        } catch (Exception e) {
            logger.error("DELETE /buildings/{} failed", id, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "An unexpected error occurred"));
        }
    }
}