package org.example.web_eng2.repository;

import org.example.web_eng2.Building;
import org.example.web_eng2.Storey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StoreyRepository extends JpaRepository<Storey, UUID> {

    List<Storey> findByBuildingAndDeletedAtIsNull(Building building);

    List<Storey> findByDeletedAtIsNull();

    Optional<Storey> findByIdAndDeletedAtIsNull(UUID id);





}