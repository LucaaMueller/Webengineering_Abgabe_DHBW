package org.example.web_eng2.repository;

import org.example.web_eng2.Building;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface BuildingRepository extends JpaRepository<Building, String> {


    List<Building> findByDeletedAtIsNull();

    List<Building> findAll();
}