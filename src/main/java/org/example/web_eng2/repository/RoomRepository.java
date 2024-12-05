package org.example.web_eng2.repository;

import org.example.web_eng2.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;



@Repository
public interface RoomRepository extends JpaRepository<Room, UUID> {

    List<Room> findByStoreyId(UUID storeyId);

    List<Room> findByStoreyIdAndDeletedAtIsNull(UUID storeyId);

    List<Room> findByDeletedAtIsNull();
}
