package org.example.web_eng2;

import org.example.web_eng2.Building;
import org.example.web_eng2.Storey;
import org.example.web_eng2.repository.BuildingRepository;
import org.example.web_eng2.repository.StoreyRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class StoreyService {

    private final StoreyRepository storeyRepository;
    private final BuildingRepository buildingRepository;

    public StoreyService(StoreyRepository storeyRepository, BuildingRepository buildingRepository) {
        this.storeyRepository = storeyRepository;
        this.buildingRepository = buildingRepository;
    }

    public List<Storey> getAllStoreys(boolean includeDeleted) {
        return includeDeleted ? storeyRepository.findAll() : storeyRepository.findByDeletedAtIsNull();
    }

    public Optional<Storey> getStoreyById(UUID id) {
        return storeyRepository.findById(id);
    }

    public Storey createStorey(String name, UUID buildingId) {
        Building building = buildingRepository.findById(buildingId)
                .orElseThrow(() -> new IllegalArgumentException("Building not found"));

        if (building.getDeletedAt() != null) {
            throw new IllegalStateException("Building is marked as deleted");
        }

        Storey storey = new Storey();
        storey.setName(name);
        storey.setBuilding(building);

        return storeyRepository.save(storey);
    }

    public void deleteStorey(UUID id) {
        Storey storey = storeyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Storey not found"));

        storey.setDeletedAt(java.time.Instant.now());
        storeyRepository.save(storey);
    }
}