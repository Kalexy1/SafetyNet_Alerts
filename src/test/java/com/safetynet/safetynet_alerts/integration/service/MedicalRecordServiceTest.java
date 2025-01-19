package com.safetynet.safetynet_alerts.integration.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.safetynet.safetynet_alerts.model.MedicalRecord;
import com.safetynet.safetynet_alerts.repository.MedicalRecordRepository;
import com.safetynet.safetynet_alerts.service.MedicalRecordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

class MedicalRecordServiceTest {

    @Mock
    private MedicalRecordRepository medicalRecordRepository;

    @InjectMocks
    private MedicalRecordService medicalRecordService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllMedicalRecords() {
        List<MedicalRecord> mockRecords = List.of(
                new MedicalRecord("John", "Doe", "01/01/1980", List.of("med1"), List.of("allergy1")),
                new MedicalRecord("Jane", "Doe", "02/02/1990", List.of("med2"), List.of("allergy2"))
        );

        when(medicalRecordRepository.getAllMedicalRecords()).thenReturn(mockRecords);

        List<MedicalRecord> result = medicalRecordService.getAllMedicalRecords();

        assertEquals(2, result.size());
        assertEquals("John", result.get(0).getFirstName());
        verify(medicalRecordRepository, times(1)).getAllMedicalRecords();
    }

    @Test
    void testGetMedicalRecordByName() {
        MedicalRecord mockRecord = new MedicalRecord("John", "Doe", "01/01/1980", List.of("med1"), List.of("allergy1"));

        when(medicalRecordRepository.getMedicalRecordByName("John", "Doe")).thenReturn(Optional.of(mockRecord));

        Optional<MedicalRecord> result = medicalRecordService.getMedicalRecordByName("John", "Doe");

        assertTrue(result.isPresent());
        assertEquals("John", result.get().getFirstName());
        verify(medicalRecordRepository, times(1)).getMedicalRecordByName("John", "Doe");
    }

    @Test
    void testAddOrUpdateMedicalRecord() {
        MedicalRecord newRecord = new MedicalRecord("John", "Doe", "01/01/1980", List.of("med1"), List.of("allergy1"));

        doNothing().when(medicalRecordRepository).addOrUpdateMedicalRecord(newRecord);

        medicalRecordService.addOrUpdateMedicalRecord(newRecord);

        verify(medicalRecordRepository, times(1)).addOrUpdateMedicalRecord(newRecord);
    }
    
    @Test
    void testDeleteMedicalRecord() {
        String firstName = "John";
        String lastName = "Doe";

        when(medicalRecordRepository.deleteMedicalRecord(firstName, lastName)).thenReturn(true);

        medicalRecordService.deleteMedicalRecord(firstName, lastName);

        verify(medicalRecordRepository, times(1)).deleteMedicalRecord(firstName, lastName);
    }

}

