package org.example.web_eng2.repository;

import org.example.web_eng2.Building;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BuildingRepository extends JpaRepository<Building, UUID> {
    List<Building> findByDeletedAtIsNull();
}
