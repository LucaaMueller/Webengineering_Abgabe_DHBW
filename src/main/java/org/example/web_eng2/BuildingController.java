package org.example.web_eng2;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class BuildingController {

    private final BuildingService buildingService;

    public BuildingController(BuildingService buildingService) {
        this.buildingService = buildingService;
    }

    @GetMapping("/api/v3/assets/buildings")
    public List<Building> getAllBuildings(
            @RequestParam(value = "include_deleted", defaultValue = "false") boolean includeDeleted) {
        return buildingService.getAllBuildings(includeDeleted);
    }
}