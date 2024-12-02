package org.example.web_eng2;

import org.example.web_eng2.repository.BuildingRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v3/assets/buildings")
public class BuildingController {

    private final BuildingRepository buildingRepository;

    // Konstruktor-Injektion
    public BuildingController(BuildingRepository buildingRepository) {
        this.buildingRepository = buildingRepository;
    }

    /**
     * GET: Alle Gebäude abrufen.
     *
     * @param includeDeleted Optionaler Parameter, um auch gelöschte Gebäude anzuzeigen.
     * @return Liste der Gebäude.
     */
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_view-profile')") // Erfordert die Rolle "view-profile"
    public ResponseEntity<List<Building>> getAllBuildings(
            @RequestParam(value = "include_deleted", defaultValue = "false") boolean includeDeleted) {
        try {
            List<Building> buildings = includeDeleted
                    ? buildingRepository.findAll()
                    : buildingRepository.findByDeletedAtIsNull();
            return ResponseEntity.ok(buildings); // Status 200 + Daten
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // Status 500
        }
    }

    /**
     * POST: Neues Gebäude erstellen.
     *
     * @param building Das zu speichernde Gebäude-Objekt.
     * @return Das erstellte Gebäude.
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_manage-account')") // Erfordert die Rolle "manage-account"
    public ResponseEntity<Building> createBuilding(@Valid @RequestBody Building building) {
        try {
            Building savedBuilding = buildingRepository.save(building);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedBuilding); // Status 201
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // Status 500
        }
    }

    @GetMapping("/test")
    public ResponseEntity<String> testEndpoint(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader != null) {
            System.out.println("Authorization Header: " + authHeader);
            return ResponseEntity.ok("Token erhalten: " + authHeader);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Kein Token erhalten");
    }

}
