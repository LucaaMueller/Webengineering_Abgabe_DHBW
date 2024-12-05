package org.example.web_eng2;

import org.example.web_eng2.repository.RoomRepository;
import org.example.web_eng2.repository.StoreyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class RoomService {

    private final RoomRepository roomRepository;

    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    public List<Room> getAllRooms(boolean includeDeleted, UUID storeyId) {
        if (storeyId != null) {
            if (includeDeleted) {
                return roomRepository.findByStoreyId(storeyId);
            } else {
                return roomRepository.findByStoreyIdAndDeletedAtIsNull(storeyId);
            }
        } else {
            if (includeDeleted) {
                return roomRepository.findAll();
            } else {
                return roomRepository.findByDeletedAtIsNull();
            }
        }
    }
}