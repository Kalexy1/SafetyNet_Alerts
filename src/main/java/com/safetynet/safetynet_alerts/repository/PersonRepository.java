package com.safetynet.safetynet_alerts.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.safetynet.safetynet_alerts.model.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Repository that handles storage and retrieval of {@link Person} objects.
 * Data is initially loaded from a JSON file and kept in memory for quick access.
 */
@Repository
public class PersonRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(PersonRepository.class);

    private final ObjectMapper objectMapper;
    private final String sourceJsonFilePath;
    private final String targetJsonFilePath;
    private final Map<String, Person> personsMap = new ConcurrentHashMap<>();

    /**
     * Constructs the PersonRepository, ensuring the target JSON file exists and then loading data into memory.
     *
     * @param objectMapper       The ObjectMapper used for JSON parsing.
     * @param sourceJsonFilePath The path to the source JSON file.
     * @param targetJsonFilePath The path to the target (modifiable) JSON file.
     */
    public PersonRepository(
            ObjectMapper objectMapper,
            @Value("${data.file.path:data.json}") String sourceJsonFilePath,
            @Value("${modifiable.data.path:target/data/data.json}") String targetJsonFilePath
    ) {
        this.objectMapper = objectMapper;
        this.sourceJsonFilePath = sourceJsonFilePath;
        this.targetJsonFilePath = targetJsonFilePath;
        ensureTargetFile();
        loadData();
    }

    /**
     * Ensures that the target file exists by copying it from the source if necessary.
     * If the source file cannot be found or copied, an error is logged.
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
     * Loads person data from the target JSON file into an in-memory map.
     * If the file doesn't exist or cannot be read, an error is logged.
     */
    public void loadData() {
        File file = new File(targetJsonFilePath);
        if (!file.exists()) {
            LOGGER.error("Target JSON file not found at: {}", targetJsonFilePath);
            return;
        }

        try {
            LOGGER.info("Loading data from file: {}", file.getAbsolutePath());
            Map<String, List<Person>> data = objectMapper.readValue(file, new TypeReference<>() {});
            List<Person> persons = data.getOrDefault("persons", Collections.emptyList());

            persons.forEach(p -> {
                String key = generateKey(p.getFirstName(), p.getLastName());
                personsMap.put(key, p);
            });

            LOGGER.info("PersonRepository: Successfully loaded {} persons.", personsMap.size());
        } catch (IOException e) {
            LOGGER.error("Error loading JSON data: ", e);
        }
    }

    /**
     * Saves all persons from the in-memory map back to the target JSON file.
     * If the file cannot be written, logs an error instead.
     */
    public void saveData() {
        File file = new File(targetJsonFilePath);
        try {
            if (!file.exists() || !file.canWrite()) {
                LOGGER.error("Cannot write to file: {}", file.getAbsolutePath());
                return;
            }

            Map<String, List<Person>> dataToSave = new HashMap<>();
            dataToSave.put("persons", new ArrayList<>(personsMap.values()));
            objectMapper.writeValue(file, dataToSave);

            LOGGER.info("PersonRepository: Data saved successfully to {}", file.getAbsolutePath());
        } catch (IOException e) {
            LOGGER.error("Error saving JSON data: ", e);
        }
    }

    /**
     * Generates a unique key based on a person's first and last name.
     *
     * @param firstName The person's first name.
     * @param lastName  The person's last name.
     * @return A string key in the format "firstname_lastname" (lowercase, trimmed).
     */
    private String generateKey(String firstName, String lastName) {
        return (firstName.trim().toLowerCase() + "_" + lastName.trim().toLowerCase());
    }

    /**
     * Retrieves all persons currently held in the repository.
     *
     * @return A list of {@link Person} objects.
     */
    public List<Person> getAllPersons() {
        return new ArrayList<>(personsMap.values());
    }

    /**
     * Retrieves a specific person by first and last name.
     *
     * @param firstName The person's first name.
     * @param lastName  The person's last name.
     * @return An {@link Optional} containing the matching Person if found, otherwise empty.
     */
    public Optional<Person> getPersonByName(String firstName, String lastName) {
        return Optional.ofNullable(personsMap.get(generateKey(firstName, lastName)));
    }

    /**
     * Adds a new person or updates an existing person in the repository, then saves the data to the JSON file.
     *
     * @param person The {@link Person} to add or update.
     */
    public void addOrUpdatePerson(Person person) {
        String key = generateKey(person.getFirstName(), person.getLastName());
        LOGGER.debug("Adding/updating person with key: {}", key);
        personsMap.put(key, person);
        saveData();
        LOGGER.info("Person added/updated: {}", person);
    }

    /**
     * Deletes a person identified by first and last name.
     *
     * @param firstName The person's first name.
     * @param lastName  The person's last name.
     * @return True if the person was found and deleted, false otherwise.
     */
    public boolean deletePerson(String firstName, String lastName) {
        String key = generateKey(firstName, lastName);
        LOGGER.debug("Attempting to delete person with key: {}", key);
        if (personsMap.containsKey(key)) {
            personsMap.remove(key);
            saveData();
            LOGGER.info("Person deleted: {} {}", firstName, lastName);
            return true;
        }
        LOGGER.warn("Person not found: {} {}", firstName, lastName);
        return false;
    }

    /**
     * Finds all persons living at a specified address.
     *
     * @param address The address to match.
     * @return A list of {@link Person} objects living at the specified address.
     */
    public List<Person> findByAddress(String address) {
        List<Person> results = new ArrayList<>();
        for (Person p : personsMap.values()) {
            if (p.getAddress() != null && p.getAddress().equalsIgnoreCase(address)) {
                results.add(p);
            }
        }
        return results;
    }

    /**
     * Clears the in-memory data and reloads it from the JSON file.
     */
    public void reloadData() {
        personsMap.clear();
        loadData();
        LOGGER.info("PersonRepository: Data reloaded successfully.");
    }
}
