package com.safetynet.safetynet_alerts.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.safetynet.safetynet_alerts.model.MedicalRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Repository handling {@link MedicalRecord} data, loaded from a JSON file
 * and stored in memory for quick access.
 */
@Repository
public class MedicalRecordRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(MedicalRecordRepository.class);

    private final ObjectMapper objectMapper;
    private final String sourceJsonFilePath;
    private final String targetJsonFilePath;
    private List<MedicalRecord> medicalRecords;

    /**
     * Constructs the MedicalRecordRepository, ensuring the target JSON file
     * exists, then loading all medical record data into memory.
     *
     * @param objectMapper       The ObjectMapper used for JSON parsing.
     * @param sourceJsonFilePath The path to the original source JSON file.
     * @param targetJsonFilePath The path to the modifiable (target) JSON file.
     */
    public MedicalRecordRepository(
            ObjectMapper objectMapper,
            @Value("${data.file.path:data.json}") String sourceJsonFilePath,
            @Value("${modifiable.data.path:target/data/data.json}") String targetJsonFilePath
    ) {
        this.objectMapper = objectMapper;
        this.sourceJsonFilePath = sourceJsonFilePath;
        this.targetJsonFilePath = targetJsonFilePath;
        ensureTargetFile();
        this.medicalRecords = new CopyOnWriteArrayList<>(loadData());
    }

    /**
     * Ensures the target JSON file exists, copying it from the source if necessary.
     * Logs an error if the source file is not found or cannot be copied.
     */
    private void ensureTargetFile() {
        File targetFile = new File(targetJsonFilePath);
        if (!targetFile.exists()) {
            LOGGER.info("Target JSON file not found. Creating a copy from source.");
            try {
                File sourceFile = new File(sourceJsonFilePath);
                if (sourceFile.exists()) {
                    targetFile.getParentFile().mkdirs();
                    Files.copy(sourceFile.toPath(), targetFile.toPath());
                    LOGGER.info("Copied source JSON to target location: {}", targetFile.getAbsolutePath());
                } else {
                    LOGGER.error("Source JSON file not found. Cannot create target file.");
                }
            } catch (IOException e) {
                LOGGER.error("Failed to copy source JSON to target location.", e);
            }
        }
    }

    /**
     * Loads medical record data from the target JSON file.
     * Returns an empty list if the file is not found or cannot be read.
     *
     * @return A list of {@link MedicalRecord} objects.
     */
    private List<MedicalRecord> loadData() {
        File file = new File(targetJsonFilePath);
        if (!file.exists()) {
            LOGGER.error("Target JSON file not found at: {}", targetJsonFilePath);
            return Collections.emptyList();
        }

        try {
            LOGGER.info("Loading MedicalRecord data from file: {}", file.getAbsolutePath());
            Map<String, List<MedicalRecord>> data = objectMapper.readValue(file, new TypeReference<>() {});
            return data.getOrDefault("medicalrecords", Collections.emptyList());
        } catch (IOException e) {
            LOGGER.error("Error loading MedicalRecord JSON data: ", e);
            return Collections.emptyList();
        }
    }

    /**
     * Saves the current list of medical records back to the target JSON file.
     * Logs an error if the file cannot be written.
     */
    public void saveData() {
        File file = new File(targetJsonFilePath);
        try {
            if (!file.exists() || !file.canWrite()) {
                LOGGER.error("Cannot write to file: {}", file.getAbsolutePath());
                return;
            }

            Map<String, List<MedicalRecord>> dataToSave = Map.of("medicalrecords", medicalRecords);
            objectMapper.writeValue(file, dataToSave);
            LOGGER.info("MedicalRecordRepository: data saved successfully to {}", file.getAbsolutePath());
        } catch (IOException e) {
            LOGGER.error("Error saving MedicalRecord JSON data: ", e);
        }
    }

    /**
     * Retrieves all medical records from the repository.
     *
     * @return A list of all {@link MedicalRecord} objects.
     */
    public List<MedicalRecord> getAllMedicalRecords() {
        return new ArrayList<>(medicalRecords);
    }

    /**
     * Retrieves a medical record by the specified first and last name.
     *
     * @param firstName The person's first name.
     * @param lastName  The person's last name.
     * @return An {@link Optional} containing the matching record if found, otherwise empty.
     */
    public Optional<MedicalRecord> getMedicalRecordByName(String firstName, String lastName) {
        return medicalRecords.stream()
                .filter(mr -> mr.getFirstName().equalsIgnoreCase(firstName)
                           && mr.getLastName().equalsIgnoreCase(lastName))
                .findFirst();
    }

    /**
     * Adds or updates a medical record. If a record with the same first and last name
     * exists, it is removed before adding the new one.
     *
     * @param mr The {@link MedicalRecord} to add or update.
     */
    public void addOrUpdateMedicalRecord(MedicalRecord mr) {
        medicalRecords.removeIf(m -> m.getFirstName().equalsIgnoreCase(mr.getFirstName())
                && m.getLastName().equalsIgnoreCase(mr.getLastName()));
        medicalRecords.add(mr);
        saveData();
    }

    /**
     * Deletes a medical record identified by first and last name.
     *
     * @param firstName The person's first name.
     * @param lastName  The person's last name.
     * @return True if a record was found and deleted, false otherwise.
     */
    public boolean deleteMedicalRecord(String firstName, String lastName) {
        LOGGER.info("Attempting to delete medical record: {} {}", firstName, lastName);
        boolean removed = medicalRecords.removeIf(m ->
                m.getFirstName().equalsIgnoreCase(firstName)
             && m.getLastName().equalsIgnoreCase(lastName));
        if (removed) {
            saveData();
            LOGGER.info("Successfully deleted medical record: {} {}", firstName, lastName);
        } else {
            LOGGER.warn("No matching medical record found for: {} {}", firstName, lastName);
        }
        return removed;
    }

    /**
     * Reloads the list of medical records by clearing the current data and
     * re-reading the JSON file.
     */
    public void reloadData() {
        this.medicalRecords = new CopyOnWriteArrayList<>(loadData());
        LOGGER.info("MedicalRecordRepository: data reloaded successfully.");
    }
}
