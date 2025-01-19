package com.safetynet.safetynet_alerts.integration.repository;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.safetynet.safetynet_alerts.model.MedicalRecord;
import com.safetynet.safetynet_alerts.repository.MedicalRecordRepository;

class MedicalRecordRepositoryTest {

    private MedicalRecordRepository medicalRecordRepository;

    @Mock
    private ObjectMapper mockObjectMapper;

    @TempDir
    Path tempDir;

    private File tempFile;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        tempFile = tempDir.resolve("data.json").toFile();
        medicalRecordRepository = new MedicalRecordRepository(mockObjectMapper, "source.json", tempFile.getAbsolutePath());
    }

    @Test
    void testEnsureTargetFile_CreatesFileIfNotExists() throws IOException {
        File sourceFile = tempDir.resolve("source.json").toFile();
        Files.writeString(sourceFile.toPath(), "{\"medicalrecords\":[]}");

        when(mockObjectMapper.readValue(any(File.class), any(TypeReference.class))).thenReturn(Map.of("medicalrecords", List.of()));

        MedicalRecordRepository repository = new MedicalRecordRepository(mockObjectMapper, sourceFile.getAbsolutePath(), tempFile.getAbsolutePath());

        assertTrue(tempFile.exists(), "The target file should have been created.");
    }

    @Test
    void testLoadData_FileExistsAndValid() throws IOException {
        Files.writeString(tempFile.toPath(), "{\"medicalrecords\":[{\"firstName\":\"John\",\"lastName\":\"Doe\",\"birthdate\":\"01/01/2000\",\"medications\":[],\"allergies\":[]}]}\n");

        when(mockObjectMapper.readValue(any(File.class), any(TypeReference.class))).thenReturn(Map.of(
            "medicalrecords", List.of(new MedicalRecord("John", "Doe", "01/01/2000", List.of(), List.of()))
        ));

        medicalRecordRepository.reloadData();
        List<MedicalRecord> records = medicalRecordRepository.getAllMedicalRecords();

        assertEquals(1, records.size());
        assertEquals("John", records.get(0).getFirstName());
    }

    @Test
    void testLoadData_FileNotExists() {
        assertDoesNotThrow(() -> medicalRecordRepository.reloadData());
        List<MedicalRecord> records = medicalRecordRepository.getAllMedicalRecords();
        assertTrue(records.isEmpty());
    }

    @Test
    void testSaveData() throws IOException {
        String validJsonContent = "{\"medicalrecords\":[]}";
        Files.writeString(tempFile.toPath(), validJsonContent);

        doNothing().when(mockObjectMapper).writeValue(any(File.class), any(Map.class));

        MedicalRecord record = new MedicalRecord("Jane", "Doe", "02/02/1990", List.of("med1"), List.of("allergy1"));
        medicalRecordRepository.addOrUpdateMedicalRecord(record);

        verify(mockObjectMapper, times(1)).writeValue(any(File.class), any(Map.class));
    }

    @Test
    void testSaveData_FileNotWritable() {
        tempFile.setWritable(false);

        MedicalRecord record = new MedicalRecord("John", "Smith", "03/03/1980", List.of(), List.of());

        assertDoesNotThrow(() -> medicalRecordRepository.addOrUpdateMedicalRecord(record));

        assertFalse(tempFile.canWrite(), "The file should not be writable.");
        assertFalse(medicalRecordRepository.getAllMedicalRecords().isEmpty(), "The record should still exist in memory.");
    }

    @Test
    void testGetAllMedicalRecords() {
        MedicalRecord record1 = new MedicalRecord("John", "Doe", "01/01/2000", List.of(), List.of());
        MedicalRecord record2 = new MedicalRecord("Jane", "Smith", "02/02/1990", List.of(), List.of());

        medicalRecordRepository.addOrUpdateMedicalRecord(record1);
        medicalRecordRepository.addOrUpdateMedicalRecord(record2);

        List<MedicalRecord> records = medicalRecordRepository.getAllMedicalRecords();
        assertEquals(2, records.size());
        assertTrue(records.contains(record1));
        assertTrue(records.contains(record2));
    }

    @Test
    void testGetMedicalRecordByName() {
        MedicalRecord record = new MedicalRecord("John", "Doe", "01/01/2000", List.of(), List.of());
        medicalRecordRepository.addOrUpdateMedicalRecord(record);

        Optional<MedicalRecord> result = medicalRecordRepository.getMedicalRecordByName("John", "Doe");
        assertTrue(result.isPresent());
        assertEquals("John", result.get().getFirstName());
    }

    @Test
    void testEnsureTargetFile_SourceFileMissing() throws IOException {
        File sourceFile = new File("missing-source.json");
        if (sourceFile.exists()) {
            sourceFile.delete();
        }

        MedicalRecordRepository repository = new MedicalRecordRepository(mockObjectMapper, sourceFile.getAbsolutePath(), tempFile.getAbsolutePath());

        assertFalse(tempFile.exists(), "The target file should not be created when the source file is missing.");
    }

    @Test
    void testLoadData_FileNotFound() {
        if (tempFile.exists()) {
            tempFile.delete();
        }

        assertDoesNotThrow(() -> medicalRecordRepository.reloadData());

        assertTrue(medicalRecordRepository.getAllMedicalRecords().isEmpty(), "No records should be loaded if the file does not exist.");
    }

    @Test
    void testLoadData_CorruptedJson() throws IOException {
        Files.writeString(tempFile.toPath(), "INVALID_JSON");

        when(mockObjectMapper.readValue(any(File.class), any(TypeReference.class))).thenThrow(IOException.class);

        assertDoesNotThrow(() -> medicalRecordRepository.reloadData());

        assertTrue(medicalRecordRepository.getAllMedicalRecords().isEmpty(), "Corrupted JSON should result in no records being loaded.");
    }

    @Test
    void testSaveData_WithIOException() throws IOException {
        assertTrue(tempFile.createNewFile(), "The target file should exist.");

        tempFile.setWritable(true);

        doThrow(IOException.class).when(mockObjectMapper).writeValue(any(File.class), any(Map.class));

        assertDoesNotThrow(() -> medicalRecordRepository.saveData());

        verify(mockObjectMapper, times(1)).writeValue(any(File.class), any(Map.class));
    }

    @Test
    void testAddOrUpdateMedicalRecord_UpdatesExistingRecord() {
        MedicalRecord existing = new MedicalRecord("John", "Doe", "01/01/2000", List.of(), List.of());
        medicalRecordRepository.addOrUpdateMedicalRecord(existing);
        MedicalRecord updated = new MedicalRecord("John", "Doe", "02/02/2000", List.of("med1"), List.of("allergy1"));
        medicalRecordRepository.addOrUpdateMedicalRecord(updated);

        assertEquals(1, medicalRecordRepository.getAllMedicalRecords().size());
        assertEquals("02/02/2000", medicalRecordRepository.getMedicalRecordByName("John", "Doe").get().getBirthdate());
    }

    @Test
    void testDeleteMedicalRecord_RecordExists() {
        MedicalRecord record = new MedicalRecord("John", "Doe", "01/01/2000", List.of(), List.of());
        medicalRecordRepository.addOrUpdateMedicalRecord(record);
        boolean deleted = medicalRecordRepository.deleteMedicalRecord("John", "Doe");

        assertTrue(deleted);
        assertTrue(medicalRecordRepository.getAllMedicalRecords().isEmpty());
    }

    @Test
    void testDeleteMedicalRecord_RecordNotFound() {
        boolean deleted = medicalRecordRepository.deleteMedicalRecord("Nonexistent", "Person");
        assertFalse(deleted);
    }

}
