package org.example.web_eng2;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.web_eng2.repository.BuildingRepository;
import org.example.web_eng2.repository.RoomRepository;
import org.example.web_eng2.repository.StoreyRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class RoomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private StoreyRepository storeyRepository;

    @Autowired
    private BuildingRepository buildingRepository;

    private UUID testBuildingId;
    private UUID testStoreyId;

    @BeforeEach
    void setup() {
        // Test Building erstellen
        Building testBuilding = new Building();
        testBuildingId = UUID.randomUUID();
        testBuilding.setId(testBuildingId);
        testBuilding.setName("Test Building");
        testBuilding.setStreetname("Test Street");
        testBuilding.setHousenumber("123");
        testBuilding.setcountryCode("DE");
        testBuilding.setPostalcode("12345");
        testBuilding.setCity("Test City");
        buildingRepository.saveAndFlush(testBuilding);

        // Test Storey erstellen
        Storey testStorey = new Storey();
        testStoreyId = UUID.randomUUID();
        testStorey.setId(testStoreyId);
        testStorey.setName("Test Storey");
        testStorey.setBuilding(testBuilding);
        storeyRepository.saveAndFlush(testStorey);
    }

    @AfterEach
    void cleanup() {
        roomRepository.deleteAll();
        storeyRepository.deleteAll();
        buildingRepository.deleteAll();
    }

    @Test
    void testGetAllRoomsWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/v3/assets/rooms"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "ROLE_manage-account")
    void testCreateRoomWithPermission() throws Exception {
        Map<String, Object> payload = Map.of(
                "name", "Test Room",
                "storey_id", testStoreyId.toString()
        );

        mockMvc.perform(post("/api/v3/assets/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Room"))
                .andExpect(jsonPath("$.storey_id").value(testStoreyId.toString()));
    }

    @Test
    @WithMockUser(authorities = "ROLE_view-profile")
    void testCreateRoomWithoutPermission() throws Exception {
        Map<String, Object> payload = Map.of(
                "name", "Unauthorized Room",
                "storey_id", testStoreyId.toString()
        );

        mockMvc.perform(post("/api/v3/assets/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ROLE_manage-account")
    void testUpdateRoomWithPermission() throws Exception {
        // Create a test room
        Room room = new Room();
        UUID roomId = UUID.randomUUID();
        room.setId(roomId);
        room.setName("Old Room");
        room.setStorey(storeyRepository.findById(testStoreyId).get());
        roomRepository.saveAndFlush(room);

        // Update the room
        Map<String, Object> payload = Map.of(
                "name", "Updated Room",
                "storey_id", testStoreyId.toString()
        );

        mockMvc.perform(put("/api/v3/assets/rooms/" + roomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Room"))
                .andExpect(jsonPath("$.storey_id").value(testStoreyId.toString()));
    }

    @Test
    @WithMockUser(authorities = "ROLE_manage-account")
    void testDeleteRoomWithPermission() throws Exception {
        // Create a test room
        Room room = new Room();
        UUID roomId = UUID.randomUUID();
        room.setId(roomId);
        room.setName("Room to Delete");
        room.setStorey(storeyRepository.findById(testStoreyId).get());
        roomRepository.saveAndFlush(room);

        // Delete the room
        mockMvc.perform(delete("/api/v3/assets/rooms/" + roomId)
                        .param("permanent", "true"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testGetRoomByIdWithoutAuthentication() throws Exception {
        // Create a test room
        Room room = new Room();
        UUID roomId = UUID.randomUUID();
        room.setId(roomId);
        room.setName("Room to Fetch");
        room.setStorey(storeyRepository.findById(testStoreyId).get());
        roomRepository.saveAndFlush(room);

        // Fetch the room
        mockMvc.perform(get("/api/v3/assets/rooms/" + roomId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(roomId.toString()))
                .andExpect(jsonPath("$.name").value("Room to Fetch"))
                .andExpect(jsonPath("$.storey_id").value(testStoreyId.toString()));
    }
}
