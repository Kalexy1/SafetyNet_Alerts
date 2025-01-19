package com.safetynet.safetynet_alerts.integration.service;

import com.safetynet.safetynet_alerts.dto.MedicalRecordDTO;
import com.safetynet.safetynet_alerts.dto.PersonDTO;
import com.safetynet.safetynet_alerts.model.FireStation;
import com.safetynet.safetynet_alerts.model.MedicalRecord;
import com.safetynet.safetynet_alerts.model.Person;
import com.safetynet.safetynet_alerts.repository.FireStationRepository;
import com.safetynet.safetynet_alerts.repository.MedicalRecordRepository;
import com.safetynet.safetynet_alerts.repository.PersonRepository;
import com.safetynet.safetynet_alerts.service.FireStationService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FireStationServiceTest {

    private FireStationRepository fireStationRepository;
    private PersonRepository personRepository;
    private MedicalRecordRepository medicalRecordRepository;
    private FireStationService fireStationService;

    @BeforeEach
    void setUp() {
        fireStationRepository = mock(FireStationRepository.class);
        personRepository = mock(PersonRepository.class);
        medicalRecordRepository = mock(MedicalRecordRepository.class);
        fireStationService = new FireStationService(fireStationRepository, personRepository, medicalRecordRepository);
    }
    
    @Test
    void testAddFireStation() {
        FireStation fireStation = new FireStation("1509 Culver St", "3");
        doNothing().when(fireStationRepository).addFireStation(fireStation);

        fireStationService.addFireStation(fireStation);

        verify(fireStationRepository, times(1)).addFireStation(fireStation);
    }

    @Test
    void testDeleteFireStation_WhenFireStationExists() {
        when(fireStationRepository.deleteFireStation("1509 Culver St")).thenReturn(true);

        boolean result = fireStationService.deleteFireStation("1509 Culver St");

        assertTrue(result);
        verify(fireStationRepository, times(1)).deleteFireStation("1509 Culver St");
    }

    @Test
    void testDeleteFireStation_WhenFireStationDoesNotExist() {
        when(fireStationRepository.deleteFireStation("Unknown Address")).thenReturn(false);

        boolean result = fireStationService.deleteFireStation("Unknown Address");

        assertFalse(result);
        verify(fireStationRepository, times(1)).deleteFireStation("Unknown Address");
    }

    @Test
    void testUpdateFireStation() {
        FireStation fireStation = new FireStation("1509 Culver St", "4");
        when(fireStationRepository.updateFireStation("1509 Culver St", "4")).thenReturn(true);

        boolean result = fireStationService.updateFireStation(fireStation.getAddress(), fireStation.getStation());

        assertTrue(result);
        verify(fireStationRepository, times(1)).updateFireStation("1509 Culver St", "4");
    }

    @Test
    void testGetPeopleCoveredByFireStation() {
        when(fireStationRepository.getAllFireStations()).thenReturn(List.of(
                new FireStation("1509 Culver St", "3"),
                new FireStation("29 15th St", "3")
        ));

        when(personRepository.getAllPersons()).thenReturn(List.of(
                new Person("John", "Boyd", "1509 Culver St", "Culver", "97451", "841-874-6512", "john@email.com"),
                new Person("Jacob", "Boyd", "29 15th St", "Culver", "97451", "841-874-6513", "jacob@email.com")
        ));

        when(medicalRecordRepository.getMedicalRecordByName("John", "Boyd"))
                .thenReturn(Optional.of(new MedicalRecord("John", "Boyd", "03/06/1984", List.of(), List.of())));

        when(medicalRecordRepository.getMedicalRecordByName("Jacob", "Boyd"))
                .thenReturn(Optional.of(new MedicalRecord("Jacob", "Boyd", "03/06/2012", List.of(), List.of())));

        Map<String, Object> result = fireStationService.getPeopleCoveredByFireStation("3");

        assertEquals(2, ((List<?>) result.get("persons")).size());
        assertEquals(1, result.get("adultCount"));
        assertEquals(1, result.get("childCount"));
        verify(fireStationRepository, times(1)).getAllFireStations();
        verify(personRepository, times(1)).getAllPersons();
    }

    @Test
    void testGetFloodStationsInfo() {
        when(fireStationRepository.getAllFireStations()).thenReturn(List.of(
                new FireStation("1509 Culver St", "3"),
                new FireStation("29 15th St", "2"),
                new FireStation("834 Binoc Ave", "3")
        ));

        when(personRepository.findByAddress("1509 Culver St")).thenReturn(List.of(
                new Person("John", "Boyd", "1509 Culver St", "Culver", "97451", "841-874-6512", "john@email.com")
        ));
        when(personRepository.findByAddress("29 15th St")).thenReturn(List.of(
                new Person("Jacob", "Boyd", "29 15th St", "Culver", "97451", "841-874-6513", "jacob@email.com")
        ));
        when(personRepository.findByAddress("834 Binoc Ave")).thenReturn(List.of(
                new Person("Tessa", "Boyd", "834 Binoc Ave", "Culver", "97451", "841-874-6514", "tessa@email.com")
        ));

        when(medicalRecordRepository.getMedicalRecordByName("John", "Boyd"))
                .thenReturn(Optional.of(new MedicalRecord("John", "Boyd", "03/06/1984", List.of("med1"), List.of("allergy1"))));
        when(medicalRecordRepository.getMedicalRecordByName("Jacob", "Boyd"))
                .thenReturn(Optional.of(new MedicalRecord("Jacob", "Boyd", "03/06/2012", List.of(), List.of())));
        when(medicalRecordRepository.getMedicalRecordByName("Tessa", "Boyd"))
                .thenReturn(Optional.of(new MedicalRecord("Tessa", "Boyd", "02/18/2012", List.of("med2"), List.of("allergy2"))));

        Map<String, List<Map<String, Object>>> result = fireStationService.getFloodStationsInfo(List.of(3, 2));

        assertEquals(3, result.size());
        assertTrue(result.containsKey("1509 Culver St"));
        assertTrue(result.containsKey("29 15th St"));
        assertTrue(result.containsKey("834 Binoc Ave"));

        verify(fireStationRepository, times(1)).getAllFireStations();
        verify(personRepository, times(3)).findByAddress(anyString());
        verify(medicalRecordRepository, times(3)).getMedicalRecordByName(anyString(), anyString());
    }



}
