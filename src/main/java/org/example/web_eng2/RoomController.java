package org.example.web_eng2;

import org.example.web_eng2.repository.RoomRepository;
import org.example.web_eng2.repository.StoreyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/api/v3/assets/rooms")
public class RoomController {

    private final RoomService roomService;
    private final RoomRepository roomRepository;
    private final StoreyRepository storeyRepository;
    private static final Logger logger = LoggerFactory.getLogger(RoomController.class);

    public RoomController(RoomService roomService, RoomRepository roomRepository, StoreyRepository storeyRepository) {
        this.roomService = roomService;
        this.roomRepository = roomRepository;
        this.storeyRepository = storeyRepository;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_view-profile')")
    public ResponseEntity<?> getAllRooms(
            @RequestParam(value = "include_deleted", defaultValue = "false") boolean includeDeleted,
            @RequestParam(value = "storey_id", required = false) UUID storeyId) {
        try {
            logger.info("GET /rooms - Request to fetch all rooms (include_deleted: {}, storey_id: {})", includeDeleted, storeyId);

            List<Room> rooms = roomService.getAllRooms(includeDeleted, storeyId);

            List<Map<String, Object>> response = rooms.stream().map(room -> {
                Map<String, Object> roomMap = new HashMap<>();
                roomMap.put("id", room.getId());
                roomMap.put("name", room.getName());
                roomMap.put("storey_id", room.getStorey().getId());
                roomMap.put("deleted_at", room.getDeletedAt());
                return roomMap;
            }).toList();

            logger.info("GET /rooms - Successfully fetched {} rooms", rooms.size());
            return ResponseEntity.ok(Map.of("rooms", response));
        } catch (Exception e) {
            logger.error("GET /rooms - Failed to fetch rooms", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred"));
        }
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_manage-account')")
    public ResponseEntity<?> createRoom(@RequestBody Map<String, Object> payload) {
        try {
            logger.info("POST /rooms - Request to create a room");

            String name = (String) payload.get("name");
            UUID storeyId = UUID.fromString((String) payload.get("storey_id"));

            Optional<Storey> storeyOptional = storeyRepository.findById(storeyId);
            if (storeyOptional.isEmpty() || storeyOptional.get().getDeletedAt() != null) {
                logger.warn("POST /rooms - Invalid or deleted storey_id: {}", storeyId);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Invalid or deleted storey ID"));
            }

            Storey storey = storeyOptional.get();

            Room room = new Room();
            room.setName(name);
            room.setStorey(storey);
            room = roomRepository.save(room);

            Map<String, Object> response = new HashMap<>();
            response.put("id", room.getId());
            response.put("name", room.getName());
            response.put("storey_id", room.getStorey().getId());

            logger.info("POST /rooms - Successfully created room with ID: {}", room.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            logger.warn("POST /rooms - Bad request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("POST /rooms - Failed to create room", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred"));
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_view-profile')")
    public ResponseEntity<?> getRoomById(@PathVariable("id") UUID id) {
        try {
            logger.info("GET /rooms/{} - Request to fetch room details", id);

            Optional<Room> roomOptional = roomRepository.findById(id);

            if (roomOptional.isPresent()) {
                Room room = roomOptional.get();
                Map<String, Object> response = new HashMap<>();
                response.put("id", room.getId());
                response.put("name", room.getName());
                response.put("storey_id", room.getStorey().getId());
                response.put("deleted_at", room.getDeletedAt());

                logger.info("GET /rooms/{} - Successfully fetched room details", id);
                return ResponseEntity.ok(response);
            } else {
                logger.warn("GET /rooms/{} - Room not found", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Room not found", "id", id.toString()));
            }
        } catch (Exception e) {
            logger.error("GET /rooms/{} - Failed to fetch room details", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred"));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_manage-account')")
    public ResponseEntity<?> updateOrCreateRoom(
            @PathVariable String id,
            @RequestBody Map<String, Object> payload) {
        try {
            logger.info("PUT /rooms/{} - Request to update or create room", id);

            UUID roomId = UUID.fromString(id);
            String name = (String) payload.get("name");

            if (name == null || name.isEmpty()) {
                logger.warn("PUT /rooms/{} - Name is required", id);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Name is required"));
            }

            String storeyIdStr = (String) payload.get("storey_id");
            UUID storeyId = UUID.fromString(storeyIdStr);

            Optional<Storey> storeyOptional = storeyRepository.findById(storeyId);
            if (storeyOptional.isEmpty()) {
                logger.warn("PUT /rooms/{} - Storey does not exist: {}", id, storeyId);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Storey does not exist"));
            }

            Storey storey = storeyOptional.get();

            Optional<Room> roomOptional = roomRepository.findById(roomId);
            if (roomOptional.isEmpty()) {
                Room newRoom = new Room();
                newRoom.setId(roomId);
                newRoom.setName(name);
                newRoom.setStorey(storey);

                roomRepository.save(newRoom);

                URI location = ServletUriComponentsBuilder
                        .fromCurrentRequest()
                        .buildAndExpand(newRoom.getId())
                        .toUri();

                logger.info("PUT /rooms/{} - Created new room with ID: {}", id, newRoom.getId());
                return ResponseEntity.created(location).body(Map.of(
                        "id", newRoom.getId(),
                        "name", newRoom.getName(),
                        "storey_id", newRoom.getStorey().getId()
                ));
            } else {
                Room existingRoom = roomOptional.get();
                existingRoom.setName(name);
                existingRoom.setStorey(storey);

                roomRepository.save(existingRoom);

                logger.info("PUT /rooms/{} - Updated existing room with ID: {}", id, existingRoom.getId());
                return ResponseEntity.ok(Map.of(
                        "id", existingRoom.getId(),
                        "name", existingRoom.getName(),
                        "storey_id", existingRoom.getStorey().getId()
                ));
            }
        } catch (Exception e) {
            logger.error("PUT /rooms/{} - Failed to update or create room", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred", "details", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_manage-account')")
    public ResponseEntity<?> deleteRoom(
            @PathVariable UUID id,
            @RequestParam(value = "permanent", defaultValue = "false") boolean permanent) {
        try {
            logger.info("DELETE /rooms/{} - Request to delete room (permanent: {})", id, permanent);

            Optional<Room> roomOptional = roomRepository.findById(id);

            if (roomOptional.isEmpty()) {
                logger.warn("DELETE /rooms/{} - Room not found", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Room not found", "id", id.toString()));
            }

            Room room = roomOptional.get();

            if (permanent) {
                roomRepository.delete(room);
                logger.info("DELETE /rooms/{} - Permanently deleted room", id);
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            } else {
                room.setDeletedAt(Instant.now());
                roomRepository.save(room);
                logger.info("DELETE /rooms/{} - Soft-deleted room", id);
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            }
        } catch (Exception e) {
            logger.error("DELETE /rooms/{} - Failed to delete room", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred"));
        }
    }
}
