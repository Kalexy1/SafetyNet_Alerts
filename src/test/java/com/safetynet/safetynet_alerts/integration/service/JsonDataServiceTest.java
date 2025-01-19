package com.safetynet.safetynet_alerts.integration.service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.safetynet.safetynet_alerts.model.FireStation;
import com.safetynet.safetynet_alerts.model.JsonWrapper;
import com.safetynet.safetynet_alerts.model.MedicalRecord;
import com.safetynet.safetynet_alerts.model.Person;
import com.safetynet.safetynet_alerts.service.JsonDataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JsonDataServiceTest {

    private ObjectMapper objectMapper;
    private JsonDataService jsonDataService;
    private File testFile;

    @BeforeEach
    void setUp() throws IOException {
        objectMapper = new ObjectMapper();
        testFile = File.createTempFile("test", ".json");
        testFile.deleteOnExit();

        JsonWrapper mockWrapper = new JsonWrapper();
        mockWrapper.setPersons(List.of(new Person("John", "Doe", "123 Main St", "City", "12345", "123-456", "john.doe@example.com")));
        mockWrapper.setFirestations(List.of(new FireStation("123 Main St", "1")));
        mockWrapper.setMedicalrecords(List.of(new MedicalRecord("John", "Doe", "01/01/2000", List.of("Med1"), List.of("Allergy1"))));

        objectMapper.writeValue(testFile, mockWrapper);

        jsonDataService = new JsonDataService(objectMapper, testFile.getAbsolutePath(), null);
    }

    @Test
    void testLoadDataSuccess() throws IOException {
        File tempFile = File.createTempFile("data", ".json");
        tempFile.deleteOnExit();

        String validJson = """
        {
            "persons": [{"firstName": "John", "lastName": "Doe", "address": "123 Main St", "city": "City", "zip": "12345", "phone": "123-456", "email": "john.doe@example.com"}],
            "firestations": [{"address": "123 Main St", "station": "1"}],
            "medicalrecords": [{"firstName": "John", "lastName": "Doe", "birthdate": "01/01/2000", "medications": ["Med1"], "allergies": ["Allergy1"]}]
        }
        """;
        Files.writeString(tempFile.toPath(), validJson);

        jsonDataService.setFileForTest(tempFile);

        jsonDataService.reloadData();

        assertEquals(1, jsonDataService.getPersons().size());
        assertEquals(1, jsonDataService.getFireStations().size());
        assertTrue(jsonDataService.getMedicalRecord("John", "Doe").isPresent());
    }

    @Test
    void testLoadDataFileNotFound() {
        jsonDataService = new JsonDataService(objectMapper, "nonexistent.json", null);
        List<Person> persons = jsonDataService.getPersons();
        assertTrue(persons.isEmpty());
    }

    @Test
    void testLoadDataInvalidJsonFormat() throws IOException {
        objectMapper = mock(ObjectMapper.class);
        when(objectMapper.readValue(any(File.class), eq(JsonWrapper.class))).thenThrow(JsonParseException.class);

        jsonDataService = new JsonDataService(objectMapper, testFile.getAbsolutePath(), null);

        List<Person> persons = jsonDataService.getPersons();
        assertTrue(persons.isEmpty());
    }

    @Test
    void testReloadDataSuccess() throws IOException {
        File tempFile = File.createTempFile("data", ".json");
        tempFile.deleteOnExit();

        String validJson = """
        {
            "persons": [{"firstName": "John", "lastName": "Doe"}],
            "firestations": [{"address": "123 Main St", "station": "1"}],
            "medicalrecords": [{"firstName": "John", "lastName": "Doe", "birthdate": "01/01/2000", "medications": [], "allergies": []}]
        }
        """;
        Files.writeString(tempFile.toPath(), validJson);

        jsonDataService.setFileForTest(tempFile);

        // Appelez reloadData()
        jsonDataService.reloadData();

        // Vérifiez les données rechargées
        assertEquals(1, jsonDataService.getPersons().size());
        assertEquals(1, jsonDataService.getFireStations().size());
        assertTrue(jsonDataService.getMedicalRecord("John", "Doe").isPresent());
    }

    @Test
    void testReloadDataIOException() throws IOException {
        objectMapper = mock(ObjectMapper.class);
        when(objectMapper.readValue(any(File.class), eq(JsonWrapper.class))).thenThrow(IOException.class);

        jsonDataService = new JsonDataService(objectMapper, testFile.getAbsolutePath(), null);

        assertThrows(IllegalStateException.class, jsonDataService::reloadData);
    }
}
