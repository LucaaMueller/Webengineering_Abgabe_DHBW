package org.example.web_eng2;



import org.example.web_eng2.repository.BuildingRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BuildingService {

    private final BuildingRepository buildingRepository;

    public BuildingService(BuildingRepository buildingRepository) {
        this.buildingRepository = buildingRepository;
    }

    public List<Building> getAllBuildings(boolean includeDeleted) {
        if (includeDeleted) {
            return buildingRepository.findAll();
        }
        return buildingRepository.findByDeletedAtIsNull();
    }
}