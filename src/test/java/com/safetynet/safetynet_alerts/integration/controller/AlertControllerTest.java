package com.safetynet.safetynet_alerts.integration.controller;

import com.safetynet.safetynet_alerts.controller.AlertController;
import com.safetynet.safetynet_alerts.dto.PersonDTO;
import com.safetynet.safetynet_alerts.model.FireStation;
import com.safetynet.safetynet_alerts.model.MedicalRecord;
import com.safetynet.safetynet_alerts.model.Person;
import com.safetynet.safetynet_alerts.service.FireStationService;
import com.safetynet.safetynet_alerts.service.MedicalRecordService;
import com.safetynet.safetynet_alerts.service.PersonService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AlertControllerTest {

    private AlertController alertController;
    private PersonService personService;
    private FireStationService fireStationService;
    private MedicalRecordService medicalRecordService;

    @BeforeEach
    void setUp() {
        personService = mock(PersonService.class);
        fireStationService = mock(FireStationService.class);
        medicalRecordService = mock(MedicalRecordService.class);
        alertController = new AlertController(personService, fireStationService, medicalRecordService);
    }
    
    @Test
    void testGetFireStationInfo() {
        String stationNumber = "1";
        Map<String, Object> mockResponse = Map.of(
            "persons", List.of(Map.of("firstName", "John", "lastName", "Doe")),
            "adultCount", 1,
            "childCount", 0
        );

        when(fireStationService.getPeopleCoveredByFireStation(stationNumber)).thenReturn(mockResponse);

        ResponseEntity<Map<String, Object>> response = alertController.getFireStationInfo(stationNumber);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockResponse, response.getBody());
        verify(fireStationService, times(1)).getPeopleCoveredByFireStation(stationNumber);
    }

    @Test
    void testGetChildrenByAddress() {
        String address = "1509 Culver St";
        List<String> mockChildren = List.of("Child: John Doe, Age: 10");

        when(personService.getChildrenByAddress(address)).thenReturn(mockChildren);

        ResponseEntity<List<String>> response = alertController.getChildrenByAddress(address);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockChildren, response.getBody());
        verify(personService, times(1)).getChildrenByAddress(address);
    }

    @Test
    void testGetPhoneNumbersByFireStation() {
        String firestation = "1";
        List<String> mockPhones = List.of("123-456-7890");

        when(personService.getPhoneNumbersByStation(firestation)).thenReturn(mockPhones);

        ResponseEntity<List<String>> response = alertController.getPhoneNumbersByFireStation(firestation);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockPhones, response.getBody());
        verify(personService, times(1)).getPhoneNumbersByStation(firestation);
    }

    @Test
    void testGetPersonsByAddressWithMedicalRecord() {
        String address = "1509 Culver St";
        Map<String, Object> mockResult = Map.of(
            "firestationNumber", "1",
            "residents", List.of(Map.of("firstName", "John", "lastName", "Doe"))
        );

        when(personService.getPersonsByAddress(address)).thenReturn(mockResult);

        ResponseEntity<Map<String, Object>> response = alertController.getPersonsByAddressWithMedicalRecord(address);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockResult, response.getBody());
        verify(personService, times(1)).getPersonsByAddress(address);
    }

    @Test
    void testGetFloodStationsInfo() {
        List<Integer> stations = List.of(1, 2);

        Map<String, List<Map<String, Object>>> mockResponse = Map.of(
            "1509 Culver St", List.of(
                Map.of(
                    "firstName", "John",
                    "lastName", "Doe",
                    "phone", "555-1234",
                    "age", 40,
                    "medications", List.of("med1", "med2"),
                    "allergies", List.of("allergy1")
                ),
                Map.of(
                    "firstName", "Jane",
                    "lastName", "Doe",
                    "phone", "555-5678",
                    "age", 35,
                    "medications", List.of("med3"),
                    "allergies", List.of()
                )
            ),
            "123 Main St", List.of(
                Map.of(
                    "firstName", "Alice",
                    "lastName", "Smith",
                    "phone", "555-0000",
                    "age", 25,
                    "medications", List.of(),
                    "allergies", List.of("peanut")
                )
            )
        );

        when(fireStationService.getFloodStationsInfo(stations)).thenReturn(mockResponse);

        ResponseEntity<Map<String, List<Map<String, Object>>>> response = alertController.getFloodStationsInfo(stations);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockResponse, response.getBody());

        verify(fireStationService, times(1)).getFloodStationsInfo(stations);
    }

    @Test
    void testGetPersonsByLastName() {
        String lastName = "Doe";
        List<Map<String, Object>> mockInfo = List.of(Map.of("firstName", "John", "lastName", "Doe"));

        when(personService.getPersonInfoByLastName(lastName)).thenReturn(mockInfo);

        ResponseEntity<List<Map<String, Object>>> response = alertController.getPersonsByLastName(lastName);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockInfo, response.getBody());
        verify(personService, times(1)).getPersonInfoByLastName(lastName);
    }

    @Test
    void testGetEmailByCity() {
        String city = "Culver";
        List<String> mockEmails = List.of("john.doe@example.com");

        when(personService.getEmailsByCity(city)).thenReturn(mockEmails);

        ResponseEntity<List<String>> response = alertController.getEmailsByCity(city);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockEmails, response.getBody());
        verify(personService, times(1)).getEmailsByCity(city);
    }
    
    @Test
    void testAddPerson() {
        Person mockPerson = new Person("John", "Doe", "1509 Culver St", "Culver", "12345", "123-456-7890", "john.doe@example.com");

        doNothing().when(personService).addPerson(mockPerson);

        ResponseEntity<String> response = alertController.addPerson(mockPerson);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Person added successfully.", response.getBody());
        verify(personService, times(1)).addPerson(mockPerson);
    }

    @Test
    void testUpdatePerson_Success() {
        Person person = new Person("John", "Doe", "123 Street", "City", "12345", "123-456", "email@example.com");
        when(personService.updatePerson(person)).thenReturn(Optional.of(person));

        ResponseEntity<?> response = alertController.updatePerson(person);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Person updated successfully.", response.getBody());
        verify(personService, times(1)).updatePerson(person);
    }

    @Test
    void testUpdatePerson_NotFound() {
        Person person = new Person("Unknown", "Person", null, null, null, null, null);
        when(personService.updatePerson(person)).thenReturn(Optional.empty());

        ResponseEntity<?> response = alertController.updatePerson(person);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Person not found.", response.getBody());
        verify(personService, times(1)).updatePerson(person);
    }

    @Test
    void testDeletePerson_Success() {
        String firstName = "John";
        String lastName = "Doe";
        when(personService.deletePerson(firstName, lastName)).thenReturn(true);

        ResponseEntity<String> response = alertController.deletePerson(firstName, lastName);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Person deleted successfully.", response.getBody());
        verify(personService, times(1)).deletePerson(firstName, lastName);
    }

    @Test
    void testDeletePerson_NotFound() {
        String firstName = "Unknown";
        String lastName = "Person";
        when(personService.deletePerson(firstName, lastName)).thenReturn(false);

        ResponseEntity<String> response = alertController.deletePerson(firstName, lastName);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Person not found.", response.getBody());
        verify(personService, times(1)).deletePerson(firstName, lastName);
    }

    @Test
    void testAddFireStation() {
        FireStation fireStation = new FireStation("123 Street", "1");

        ResponseEntity<String> response = alertController.addFireStation(fireStation);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("FireStation added successfully.", response.getBody());
        verify(fireStationService, times(1)).addFireStation(fireStation);
    }

    @Test
    void testUpdateFireStation() {
        FireStation fireStation = new FireStation("123 Street", "2");

        when(fireStationService.updateFireStation(fireStation.getAddress(), fireStation.getStation())).thenReturn(true);

        ResponseEntity<String> response = alertController.updateFireStation(fireStation.getAddress(), fireStation.getStation());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("FireStation updated successfully.", response.getBody());
        verify(fireStationService, times(1)).updateFireStation(fireStation.getAddress(), fireStation.getStation());
    }

    @Test
    void testDeleteFireStation_Success() {
        String address = "123 Street";
        when(fireStationService.deleteFireStation(address)).thenReturn(true);

        ResponseEntity<String> response = alertController.deleteFireStation(address);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("FireStation deleted successfully.", response.getBody());
        verify(fireStationService, times(1)).deleteFireStation(address);
    }

    @Test
    void testDeleteFireStation_NotFound() {
        String address = "Unknown Address";
        when(fireStationService.deleteFireStation(address)).thenReturn(false);

        ResponseEntity<String> response = alertController.deleteFireStation(address);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("FireStation not found.", response.getBody());
        verify(fireStationService, times(1)).deleteFireStation(address);
    }

    @Test
    void testAddMedicalRecord() {
        MedicalRecord medicalRecord = new MedicalRecord("John", "Doe", "01/01/1980", List.of("med1"), List.of("allergy1"));

        ResponseEntity<String> response = alertController.addMedicalRecord(medicalRecord);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("MedicalRecord added successfully.", response.getBody());
        verify(medicalRecordService, times(1)).addOrUpdateMedicalRecord(medicalRecord);
    }

    @Test
    void testUpdateMedicalRecord() {
        MedicalRecord medicalRecord = new MedicalRecord("John", "Doe", "01/01/1980", List.of("med1"), List.of("allergy1"));

        ResponseEntity<String> response = alertController.updateMedicalRecord(medicalRecord);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("MedicalRecord updated successfully.", response.getBody());
        verify(medicalRecordService, times(1)).addOrUpdateMedicalRecord(medicalRecord);
    }

    @Test
    void testDeleteMedicalRecord() {
        String firstName = "John";
        String lastName = "Doe";

        ResponseEntity<String> response = alertController.deleteMedicalRecord(firstName, lastName);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("MedicalRecord deleted successfully.", response.getBody());
        verify(medicalRecordService, times(1)).deleteMedicalRecord(firstName, lastName);
    }
    
    @Test
    void testHandleHttpMessageNotReadableException() {
        HttpMessageNotReadableException exception = new HttpMessageNotReadableException("Test exception message");

        ResponseEntity<String> response = alertController.handleHttpMessageNotReadableException(exception);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("Invalid JSON payload:"));
        assertTrue(response.getBody().contains("Test exception message"));
    }
    
}
