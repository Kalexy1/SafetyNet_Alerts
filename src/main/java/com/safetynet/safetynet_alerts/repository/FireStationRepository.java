package com.safetynet.safetynet_alerts.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.safetynet.safetynet_alerts.model.FireStation;
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
 * Repository responsible for loading, managing, and persisting
 * {@link FireStation} data from a JSON file.
 */
@Repository
public class FireStationRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(FireStationRepository.class);

    private final ObjectMapper objectMapper;
    private final String sourceJsonFilePath;
    private final String targetJsonFilePath;
    private List<FireStation> fireStations;

    /**
     * Constructs a FireStationRepository with the required dependencies,
     * ensures the target JSON file exists, then loads the FireStation data.
     *
     * @param objectMapper        The ObjectMapper for JSON parsing.
     * @param sourceJsonFilePath  The non-modifiable source JSON file path.
     * @param targetJsonFilePath  The modifiable target JSON file path.
     */
    public FireStationRepository(
            ObjectMapper objectMapper,
            @Value("${data.file.path:data.json}") String sourceJsonFilePath,
            @Value("${modifiable.data.path:target/classes/data.json}") String targetJsonFilePath
    ) {
        this.objectMapper = objectMapper;
        this.sourceJsonFilePath = sourceJsonFilePath;
        this.targetJsonFilePath = targetJsonFilePath;
        ensureTargetFile();
        this.fireStations = new CopyOnWriteArrayList<>(loadData());
    }

    /**
     * Ensures the target JSON file exists by copying the source file
     * if it does not already exist. Logs errors if the source file cannot
     * be found or copied.
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
     * Loads FireStation data from the target JSON file.
     * If an error occurs, returns an empty list instead.
     *
     * @return A list of loaded {@link FireStation} objects.
     */
    private List<FireStation> loadData() {
        File file = new File(targetJsonFilePath);
        try {
            LOGGER.info("Loading data from JSON file: {}", file.getAbsolutePath());
            Map<String, List<FireStation>> data =
                    objectMapper.readValue(file, new TypeReference<>() {});
            return data.getOrDefault("firestations", Collections.emptyList());
        } catch (IOException e) {
            LOGGER.error("Error loading FireStation JSON data: ", e);
            return Collections.emptyList();
        }
    }

    /**
     * Saves the current FireStation list back to the target JSON file.
     * Logs an error if an I/O exception occurs.
     */
    private void saveData() {
        File file = new File(targetJsonFilePath);
        try {
            Map<String, List<FireStation>> dataToSave = new HashMap<>();
            dataToSave.put("firestations", new ArrayList<>(fireStations));
            objectMapper.writeValue(file, dataToSave);
            LOGGER.info("FireStationRepository: Data saved successfully to {}", file.getAbsolutePath());
        } catch (IOException e) {
            LOGGER.error("Error saving FireStation data: ", e);
        }
    }

    /**
     * Retrieves an unmodifiable list of all FireStations.
     *
     * @return A list of all FireStation objects.
     */
    public List<FireStation> getAllFireStations() {
        return Collections.unmodifiableList(fireStations);
    }

    /**
     * Finds a FireStation by its address.
     *
     * @param address The address to look up.
     * @return An Optional containing the FireStation if found, otherwise empty.
     */
    public Optional<FireStation> getFireStationByAddress(String address) {
        return fireStations.stream()
                .filter(fs -> fs.getAddress() != null && fs.getAddress().equalsIgnoreCase(address))
                .findFirst();
    }

    /**
     * Adds a new FireStation or updates an existing one if the address already exists.
     *
     * @param fireStation The FireStation object to add or update.
     */
    public void addFireStation(FireStation fireStation) {
        Optional<FireStation> existing = getFireStationByAddress(fireStation.getAddress());
        existing.ifPresent(fireStations::remove);
        fireStations.add(fireStation);
        saveData();
    }

    /**
     * Deletes a FireStation based on the given address.
     *
     * @param address The address corresponding to the FireStation to remove.
     * @return True if a FireStation was deleted, false otherwise.
     */
    public boolean deleteFireStation(String address) {
        boolean removed = fireStations.removeIf(fs ->
                fs.getAddress() != null && fs.getAddress().equalsIgnoreCase(address));
        if (removed) {
            saveData();
        }
        return removed;
    }

    /**
     * Updates the station number for a FireStation at the specified address.
     *
     * @param address          The address of the existing FireStation.
     * @param newStationNumber The new station number to assign.
     * @return True if the station was updated, false otherwise.
     */
    public boolean updateFireStation(String address, String newStationNumber) {
        Optional<FireStation> existing = getFireStationByAddress(address);
        if (existing.isPresent()) {
            FireStation fireStation = existing.get();
            fireStation.setStation(newStationNumber);
            saveData();
            LOGGER.info("FireStation updated successfully: {}", fireStation);
            return true;
        } else {
            LOGGER.warn("No FireStation found for address: {}", address);
            return false;
        }
    }

    /**
     * Reloads the FireStation data from the JSON file, replacing the current in-memory list.
     */
    public void reloadData() {
        this.fireStations = new CopyOnWriteArrayList<>(loadData());
        LOGGER.info("FireStationRepository: Data reloaded successfully.");
    }
}
