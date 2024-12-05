package org.example.web_eng2;

import org.example.web_eng2.repository.RoomRepository;
import org.example.web_eng2.repository.StoreyRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/api/v3/assets/rooms")
public class RoomController {

    private final RoomService roomService;

    private final RoomRepository roomRepository;

    private final StoreyRepository storeyRepository;

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
            List<Room> rooms = roomService.getAllRooms(includeDeleted, storeyId);

            List<Map<String, Object>> response = rooms.stream().map(room -> {
                Map<String, Object> roomMap = new HashMap<>();
                roomMap.put("id", room.getId());
                roomMap.put("name", room.getName());
                roomMap.put("storey_id", room.getStorey().getId());
                roomMap.put("deleted_at", room.getDeletedAt());
                return roomMap;
            }).toList();

            return ResponseEntity.ok(Map.of("rooms", response));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred"));
        }
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_manage-account')")
    public ResponseEntity<?> createRoom(@RequestBody Map<String, Object> payload) {
        try {
            // Request-Parameter auslesen
            String name = (String) payload.get("name");
            UUID storeyId = UUID.fromString((String) payload.get("storey_id"));

            // Validierung: Ist die Storey-ID vorhanden und aktiv?
            Optional<Storey> storeyOptional = storeyRepository.findById(storeyId);
            if (storeyOptional.isEmpty() || storeyOptional.get().getDeletedAt() != null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Invalid or deleted storey ID"));
            }

            Storey storey = storeyOptional.get();


            // Raum erstellen und speichern
            Room room = new Room();
            room.setName(name);
            room.setStorey(storey);
            room = roomRepository.save(room);

            // Response zurückgeben
            Map<String, Object> response = new HashMap<>();
            response.put("id", room.getId());
            response.put("name", room.getName());
            response.put("storey_id", room.getStorey().getId());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred"));
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_view-profile')")
    public ResponseEntity<?> getRoomById(@PathVariable("id") UUID id) {
        try {
            // Suche nach dem Raum anhand der ID
            Optional<Room> roomOptional = roomRepository.findById(id);

            if (roomOptional.isPresent()) {
                Room room = roomOptional.get();

                // Erstelle eine Antwort mit den erforderlichen Daten
                Map<String, Object> response = new HashMap<>();
                response.put("id", room.getId());
                response.put("name", room.getName());
                response.put("storey_id", room.getStorey().getId());
                response.put("deleted_at", room.getDeletedAt());

                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Room not found", "id", id.toString()));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred"));
        }
    }


    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_manage-account')")
    public ResponseEntity<?> updateOrCreateRoom(
            @PathVariable UUID id,
            @RequestBody Map<String, Object> payload) {
        try {
            // Name extrahieren und prüfen
            String name = (String) payload.get("name");
            if (name == null || name.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Name is required"));
            }

            // Deleted At extrahieren (optional)
            String deletedAtStr = (String) payload.get("deleted_at");
            java.time.Instant deletedAt = (deletedAtStr != null && !deletedAtStr.isEmpty())
                    ? java.time.Instant.parse(deletedAtStr)
                    : null;

            // Room aus der Datenbank laden oder neu erstellen
            Room room = roomRepository.findById(id).orElse(new Room());
            room.setId(id);
            room.setName(name);
            room.setDeletedAt(deletedAt);

            // Storey ID aus dem Payload extrahieren und prüfen
            String storeyIdStr = (String) payload.get("storey_id");
            if (storeyIdStr == null || storeyIdStr.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Storey ID is required"));
            }

            UUID storeyId;
            try {
                storeyId = UUID.fromString(storeyIdStr);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Invalid Storey ID format"));
            }

            // Zugehörige Storey aus der Datenbank prüfen
          /*  Optional<Storey> storeyOptional = storeyRepository.findById(storeyId);
            if (storeyOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Storey ID not found in database"));
            }
            room.setStorey(storeyOptional.get());*/

            // Speichern
            Room savedRoom = roomRepository.save(room);

            // Response vorbereiten
            Map<String, Object> response = new HashMap<>();
            response.put("id", savedRoom.getId());
            response.put("name", savedRoom.getName());
            response.put("storey_id", savedRoom.getStorey().getId());
            response.put("deleted_at", savedRoom.getDeletedAt());

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred"));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_manage-account')")
    public ResponseEntity<?> deleteRoom(
            @PathVariable UUID id,
            @RequestParam(value = "permanent", defaultValue = "false") boolean permanent) {
        try {
            // Prüfen, ob der Raum existiert
            Optional<Room> roomOptional = roomRepository.findById(id);

            if (roomOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Room not found", "id", id.toString()));
            }

            Room room = roomOptional.get();

            if (permanent) {
                // Permanente Löschung
                roomRepository.delete(room);
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            } else {
                // Soft-Delete: deleted_at auf den aktuellen Zeitpunkt setzen
                room.setDeletedAt(java.time.Instant.now());
                roomRepository.save(room);
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred"));
        }
    }



}