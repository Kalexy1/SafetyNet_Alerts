package com.safetynet.safetynet_alerts.integration.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.safetynet.safetynet_alerts.model.FireStation;
import com.safetynet.safetynet_alerts.repository.FireStationRepository;

class FireStationRepositoryTest {

    private FireStationRepository fireStationRepository;
    private ObjectMapper objectMapper;
    private ResourceLoader resourceLoader;
    private Resource mockResource;
    private File mockFile;
    private static final String SOURCE_JSON_FILE_PATH = "data.json";
    private static final String TARGET_JSON_FILE_PATH = "target/classes/data.json";
    private static MockedStatic<Files> filesMockedStatic; 
    
    @BeforeAll
    static void setUpAll() {
        filesMockedStatic = mockStatic(Files.class);
        filesMockedStatic.when(() -> Files.copy(any(), any())).thenAnswer(invocation -> null);
    }

    @AfterAll
    static void tearDownAll() {
        filesMockedStatic.close();
    }

    @BeforeEach
    void setUp() throws IOException {
        objectMapper = mock(ObjectMapper.class);

        Map<String, List<FireStation>> mockDataMap = new HashMap<>();
        mockDataMap.put("firestations", List.of(new FireStation("123 Main St", "1")));

        when(objectMapper.readValue(any(File.class), any(TypeReference.class)))
                .thenReturn(mockDataMap);

        fireStationRepository = new FireStationRepository(
            objectMapper,
            SOURCE_JSON_FILE_PATH,
            TARGET_JSON_FILE_PATH
        );
    }

    @Test
    void testLoadDataSuccessfully() {
        List<FireStation> allFireStations = fireStationRepository.getAllFireStations();

        assertEquals(1, allFireStations.size());
        assertEquals("123 Main St", allFireStations.get(0).getAddress());
        assertEquals("1", allFireStations.get(0).getStation());
    }

    @Test
    void testGetAllFireStations() {
        List<FireStation> allFireStations = fireStationRepository.getAllFireStations();

        assertEquals(1, allFireStations.size());
        assertEquals("123 Main St", allFireStations.get(0).getAddress());
    }

    @Test
    void testGetFireStationByAddressFound() {
        Optional<FireStation> fireStation = fireStationRepository.getFireStationByAddress("123 Main St");

        assertTrue(fireStation.isPresent());
        assertEquals("123 Main St", fireStation.get().getAddress());
        assertEquals("1", fireStation.get().getStation());
    }

    @Test
    void testGetFireStationByAddressNotFound() {
        Optional<FireStation> fireStation = fireStationRepository.getFireStationByAddress("Unknown Address");

        assertFalse(fireStation.isPresent());
    }

    @Test
    void testAddFireStation() {
        fireStationRepository.addFireStation(new FireStation("456 Elm St", "2"));

        List<FireStation> allFireStations = fireStationRepository.getAllFireStations();
        assertEquals(2, allFireStations.size());
    }

    @Test
    void testAddFireStationReplacesExisting() {
        fireStationRepository.addFireStation(new FireStation("123 Main St", "2"));

        List<FireStation> allFireStations = fireStationRepository.getAllFireStations();
        assertEquals(1, allFireStations.size());
        assertEquals("123 Main St", allFireStations.get(0).getAddress());
        assertEquals("2", allFireStations.get(0).getStation());
    }

    @Test
    void testDeleteFireStationFound() {
        boolean result = fireStationRepository.deleteFireStation("123 Main St");

        assertTrue(result);
        assertTrue(fireStationRepository.getAllFireStations().isEmpty());
    }

    @Test
    void testDeleteFireStationNotFound() {
        boolean result = fireStationRepository.deleteFireStation("Unknown Address");

        assertFalse(result);
    }

    @Test
    void testUpdateFireStation() {
        fireStationRepository.updateFireStation("123 Main St", "2");

        Optional<FireStation> updatedFireStation = fireStationRepository.getFireStationByAddress("123 Main St");
        assertTrue(updatedFireStation.isPresent());
        assertEquals("2", updatedFireStation.get().getStation());
    }

    @Test
    void testUpdateFireStationNotFound() {
        fireStationRepository.updateFireStation("Unknown Address", "2");

        assertEquals(1, fireStationRepository.getAllFireStations().size());
    }

    @Test
    void testReloadData() throws IOException {
        Map<String, List<FireStation>> newMockData = new HashMap<>();
        newMockData.put("firestations", List.of(new FireStation("456 Elm St", "2")));

        when(objectMapper.readValue(any(File.class), any(TypeReference.class)))
            .thenReturn(newMockData);

        fireStationRepository.reloadData();

        List<FireStation> allFireStations = fireStationRepository.getAllFireStations();
        assertEquals(1, allFireStations.size());
        assertEquals("456 Elm St", allFireStations.get(0).getAddress());
    }
    
}
