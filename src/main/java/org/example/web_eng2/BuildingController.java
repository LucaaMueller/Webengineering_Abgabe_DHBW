package org.example.web_eng2;

import com.fasterxml.jackson.databind.util.JSONPObject;
import jakarta.servlet.http.HttpServletRequest;
import org.example.web_eng2.repository.BuildingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.example.web_eng2.repository.StoreyRepository;

import java.net.URI;
import java.sql.Date;
import java.time.OffsetDateTime;
import java.util.*;

import io.vertx.core.json.JsonObject;
import org.json.JSONObject;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;


@RestController
@RequestMapping("/api/v3/assets/buildings")
public class BuildingController {

    private final BuildingRepository buildingRepository;

    private final StoreyRepository storeyRepository;

    public BuildingController(BuildingRepository buildingRepository, StoreyRepository storeyRepository) {
        this.buildingRepository = buildingRepository;

        this.storeyRepository = storeyRepository;
    }






    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_view-profile')")
    public ResponseEntity<Map<String, Object>> getAllBuildings(
            @RequestParam(value = "include_deleted", defaultValue = "false") boolean includeDeleted) {
        try {
            // Liste der Gebäude abrufen
            List<Building> buildings = includeDeleted
                    ? buildingRepository.findAll()
                    : buildingRepository.findByDeletedAtIsNull();

            // Die Liste in eine Map mit dem Schlüssel "buildings" einbetten
            Map<String, Object> response = new HashMap<>();
            response.put("buildings", buildings);

            // Die Map zurückgeben
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_manage-account')")
    public ResponseEntity<Building> createBuilding(@Valid @RequestBody Building building, HttpServletRequest request) {
        try {
            Building savedBuilding = buildingRepository.save(building);

            // Erstelle die Location-URL für das neue Gebäude
            String location = request.getRequestURL().toString() + "/" + savedBuilding.getId();

            // Setze den Location-Header in der Antwort
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .header("Location", location)
                    .body(savedBuilding);
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
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }


    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_manage-account')")
    public ResponseEntity<?> updateOrCreateBuilding(
            @PathVariable String id, @Valid @RequestBody Building inputBuilding) {
        try {
            UUID uuid = UUID.fromString(id);
            // Suche das Gebäude in der Datenbank
            Optional<Building> optionalBuilding = buildingRepository.findById(uuid);

            if (optionalBuilding.isEmpty()) {
                // Neues Gebäude erstellen
                Building newBuilding = new Building();
                newBuilding.setId(uuid);
                newBuilding.setName(inputBuilding.getName());
                newBuilding.setStreetname(inputBuilding.getStreetname());
                newBuilding.setHousenumber(inputBuilding.getHousenumber());
                newBuilding.setcountryCode(inputBuilding.getCountryCode());
                newBuilding.setPostalcode(inputBuilding.getPostalcode());
                newBuilding.setCity(inputBuilding.getCity());
                newBuilding.setDeletedAt(null);

                // Speichern des neuen Gebäudes
                buildingRepository.save(newBuilding);

                // Erstelle die URI für das neue Gebäude
                URI location = ServletUriComponentsBuilder
                        .fromCurrentRequest()
                        .buildAndExpand(newBuilding.getId())
                        .toUri();

                // Rückgabe mit Status 201 Created
                return ResponseEntity.created(location).body(newBuilding);
            } else {
                // Vorhandenes Gebäude aktualisieren
                Building existingBuilding = optionalBuilding.get();
                existingBuilding.setName(inputBuilding.getName());
                existingBuilding.setStreetname(inputBuilding.getStreetname());
                existingBuilding.setHousenumber(inputBuilding.getHousenumber());
                existingBuilding.setcountryCode(inputBuilding.getCountryCode());
                existingBuilding.setPostalcode(inputBuilding.getPostalcode());
                existingBuilding.setCity(inputBuilding.getCity());

                // Wenn das Gebäude gelöscht ist, "undelete"
                if (existingBuilding.getDeletedAt() != null) {
                    existingBuilding.setDeletedAt(null);
                }

                // Speichern der Änderungen
                buildingRepository.save(existingBuilding);

                // Rückgabe mit Status 200 OK
                return ResponseEntity.ok(existingBuilding);
            }
        } catch (Exception e) {
            // Fehlerbehandlung
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred", "details", e.getMessage()));
        }
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBuilding(
            @PathVariable UUID id,
            @RequestParam(value = "permanent", defaultValue = "false") boolean permanent) {
        try {
            return buildingRepository.findById(id)
                    .map(building -> {
                        // Überprüfen, ob dem Gebäude aktive Storeys zugeordnet sind
                        List<Storey> activeStoreys = storeyRepository.findByBuildingAndDeletedAtIsNull(building);
                        if (!activeStoreys.isEmpty()) {
                            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                    .body(Map.of(
                                            "error", "Building has active storeys",
                                            "message", "Cannot delete a building with active storeys"
                                    ));
                        }

                        // Gebäude löschen
                        if (permanent) {
                            buildingRepository.delete(building);
                            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
                        } else {
                            building.setDeletedAt(java.time.OffsetDateTime.now());
                            buildingRepository.save(building);
                            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
                        }
                    })
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(Map.of(
                                    "error", "Building not found",
                                    "message", "The specified building does not exist"
                            )));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "An unexpected error occurred"));
        }
    }

}