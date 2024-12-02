package org.example.web_eng2;

import org.example.web_eng2.repository.BuildingRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/v3/assets/buildings")
public class BuildingController {

    private final BuildingRepository buildingRepository;

    public BuildingController(BuildingRepository buildingRepository) {
        this.buildingRepository = buildingRepository;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_view-profile')")
    public ResponseEntity<List<Building>> getAllBuildings(
            @RequestParam(value = "include_deleted", defaultValue = "false") boolean includeDeleted) {
        try {
            List<Building> buildings = includeDeleted
                    ? buildingRepository.findAll()
                    : buildingRepository.findByDeletedAtIsNull();
            return ResponseEntity.ok(buildings);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_manage-account')")
    public ResponseEntity<Building> createBuilding(@Valid @RequestBody Building building) {
        try {
            Building savedBuilding = buildingRepository.save(building);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedBuilding);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Building> getBuildingById(@PathVariable UUID id) {
        try {
            return buildingRepository.findById(id)
                    .map(building -> ResponseEntity.ok(building))
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Building> updateOrCreateBuilding(
            @PathVariable UUID id,
            @Valid @RequestBody Building building) {
        try {
            return buildingRepository.findById(id)
                    .map(existingBuilding -> {
                        existingBuilding.setName(building.getName());
                        existingBuilding.setStreetname(building.getStreetname());
                        existingBuilding.setHousenumber(building.getHousenumber());
                        existingBuilding.setcountryCode(building.getCountryCode());
                        existingBuilding.setPostalcode(building.getPostalcode());
                        existingBuilding.setCity(building.getCity());
                        existingBuilding.setDeletedAt(building.getDeletedAt());
                        Building updatedBuilding = buildingRepository.save(existingBuilding);
                        return ResponseEntity.ok(updatedBuilding);
                    })
                    .orElseGet(() -> {
                        building.setId(id);
                        Building newBuilding = buildingRepository.save(building);
                        return ResponseEntity.status(HttpStatus.CREATED).body(newBuilding);
                    });
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

