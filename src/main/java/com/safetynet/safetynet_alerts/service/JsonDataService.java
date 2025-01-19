package com.safetynet.safetynet_alerts.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.safetynet.safetynet_alerts.model.FireStation;
import com.safetynet.safetynet_alerts.model.JsonWrapper;
import com.safetynet.safetynet_alerts.model.MedicalRecord;
import com.safetynet.safetynet_alerts.model.Person;

/**
 * Service responsible for loading, validating, and providing access
 * to JSON data (persons, fire stations, and medical records).
 */
@Service
public class JsonDataService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonDataService.class);

    private final ObjectMapper objectMapper;
    private final String sourceJsonFilePath;
    private final String targetJsonFilePath;

    private File testFile;
    private List<Person> persons = Collections.emptyList();
    private List<FireStation> fireStations = Collections.emptyList();
    private Map<String, MedicalRecord> medicalRecordMap = new HashMap<>();

    /**
     * Constructs the JsonDataService with specified paths for the source and target JSON files.
     * It initializes the service by ensuring the target file exists and loading data from it.
     *
     * @param objectMapper       An ObjectMapper configured for JSON deserialization.
     * @param sourceJsonFilePath The path to the original source JSON file.
     * @param targetJsonFilePath The path to the modifiable (target) JSON file.
     */
    public JsonDataService(
            ObjectMapper objectMapper,
            @Value("${data.file.path:data.json}") String sourceJsonFilePath,
            @Value("${modifiable.data.path:target/classes/data.json}") String targetJsonFilePath
    ) {
        this.objectMapper = objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.sourceJsonFilePath = sourceJsonFilePath;
        this.targetJsonFilePath = targetJsonFilePath;
        try {
            ensureTargetFile();
            loadData();
        } catch (Exception e) {
            LOGGER.error("Failed to initialize JSON data from file: {}", sourceJsonFilePath, e);
            initializeEmptyData();
        }
    }

    /**
     * Ensures the target JSON file exists by copying it from the source if necessary.
     */
    private void ensureTargetFile() {
        File targetFile = new File(targetJsonFilePath);
        if (!targetFile.exists()) {
            LOGGER.info("Target JSON file not found. Creating a copy from source.");
            try {
                File sourceFile = getFileFromResource(sourceJsonFilePath);
                if (sourceFile != null) {
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
     * Loads data from the target JSON file into memory (persons, fire stations, and medical records).
     * If the file does not exist or is invalid, initializes empty data.
     */
    private void loadData() {
        File file = (testFile != null) ? testFile : new File(targetJsonFilePath);
        if (!file.exists()) {
            LOGGER.error("Target JSON file not found at: {}", targetJsonFilePath);
            initializeEmptyData();
            return;
        }

        try {
            LOGGER.info("Loading data from JSON file: {}", file.getAbsolutePath());
            JsonWrapper wrapper = objectMapper.readValue(file, JsonWrapper.class);
            validateAndInitializeData(wrapper);
            LOGGER.info("Data successfully loaded. Persons: {}, FireStations: {}, MedicalRecords: {}",
                    persons.size(), fireStations.size(), medicalRecordMap.size());
        } catch (JsonParseException | JsonMappingException e) {
            LOGGER.error("Invalid JSON format in file: {}", targetJsonFilePath, e);
            initializeEmptyData();
        } catch (IOException e) {
            LOGGER.error("Error reading JSON file: {}", targetJsonFilePath, e);
            initializeEmptyData();
        }
    }

    /**
     * Retrieves a File object from the classpath resource.
     *
     * @param filePath The path to the resource.
     * @return A File object if found; otherwise, null.
     */
    private File getFileFromResource(String filePath) {
        try {
            return new ClassPathResource(filePath).getFile();
        } catch (IOException e) {
            LOGGER.error("Failed to locate file: {}", filePath, e);
            return null;
        }
    }

    /**
     * Validates the deserialized JsonWrapper data and populates persons, fireStations, and medicalRecordMap.
     *
     * @param wrapper The JsonWrapper containing lists of Persons, FireStations, and MedicalRecords.
     */
    private void validateAndInitializeData(JsonWrapper wrapper) {
        if (wrapper == null) {
            LOGGER.warn("JSON wrapper is null after deserialization. Initializing with empty data.");
            initializeEmptyData();
            return;
        }

        this.persons = Optional.ofNullable(wrapper.getPersons()).orElseGet(Collections::emptyList);
        this.fireStations = Optional.ofNullable(wrapper.getFirestations()).orElseGet(Collections::emptyList);

        List<MedicalRecord> medicalRecords = Optional.ofNullable(wrapper.getMedicalrecords())
                .orElseGet(Collections::emptyList);

        this.medicalRecordMap = medicalRecords.stream()
                .collect(Collectors.toMap(
                        record -> record.getFirstName() + " " + record.getLastName(),
                        record -> record,
                        (existing, replacement) -> {
                            LOGGER.warn("Duplicate medical record found for key: {} {}",
                                    existing.getFirstName(), existing.getLastName());
                            return existing;
                        },
                        HashMap::new
                ));
    }

    /**
     * Initializes empty data for persons, fireStations, and medicalRecordMap.
     */
    private void initializeEmptyData() {
        this.persons = Collections.emptyList();
        this.fireStations = Collections.emptyList();
        this.medicalRecordMap = new HashMap<>();
        LOGGER.info("Initialized with empty data.");
    }

    /**
     * Retrieves an unmodifiable list of all Person entities loaded from JSON.
     *
     * @return A list of Person objects.
     */
    public List<Person> getPersons() {
        return Collections.unmodifiableList(persons);
    }

    /**
     * Retrieves an unmodifiable list of all FireStation entities loaded from JSON.
     *
     * @return A list of FireStation objects.
     */
    public List<FireStation> getFireStations() {
        return Collections.unmodifiableList(fireStations);
    }

    /**
     * Retrieves a MedicalRecord for the given first and last name.
     *
     * @param firstName The person's first name.
     * @param lastName  The person's last name.
     * @return An Optional containing the MedicalRecord if found, otherwise empty.
     */
    public Optional<MedicalRecord> getMedicalRecord(String firstName, String lastName) {
        String key = firstName + " " + lastName;
        return Optional.ofNullable(medicalRecordMap.get(key));
    }

    /**
     * Reloads the data by re-reading the JSON file.
     * If loading fails, throws an IllegalStateException.
     */
    public void reloadData() {
        try {
            LOGGER.info("Reloading data from JSON file...");
            loadData();
            LOGGER.info("Data successfully reloaded.");
        } catch (Exception e) {
            LOGGER.error("Failed to reload JSON data", e);
            throw new IllegalStateException("Failed to reload JSON data", e);
        }
    }

    /**
     * Sets a test file to be used instead of the default target JSON file.
     * This method is primarily for testing purposes.
     *
     * @param testFile The file to use for loading data.
     */
    public void setFileForTest(File testFile) {
        this.testFile = testFile;
    }
}
