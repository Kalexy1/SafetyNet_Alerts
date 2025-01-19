package com.safetynet.safetynet_alerts.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.safetynet.safetynet_alerts.model.FireStation;
import com.safetynet.safetynet_alerts.model.MedicalRecord;
import com.safetynet.safetynet_alerts.model.Person;
import com.safetynet.safetynet_alerts.repository.FireStationRepository;
import com.safetynet.safetynet_alerts.repository.MedicalRecordRepository;
import com.safetynet.safetynet_alerts.repository.PersonRepository;

/**
 * Provides business logic related to {@link FireStation}, such as
 * adding, updating, deleting fire station mappings, as well as
 * retrieving information about covered persons and flood-related data.
 */
@Service
public class FireStationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FireStationService.class);

    private final FireStationRepository fireStationRepository;
    private final PersonRepository personRepository;
    private final MedicalRecordRepository medicalRecordRepository;

    /**
     * Constructs the FireStationService with the required repositories.
     *
     * @param fireStationRepository   Repository managing fire station data.
     * @param personRepository        Repository managing person data.
     * @param medicalRecordRepository Repository managing medical record data.
     */
    public FireStationService(
            FireStationRepository fireStationRepository,
            PersonRepository personRepository,
            MedicalRecordRepository medicalRecordRepository
    ) {
        this.fireStationRepository = fireStationRepository;
        this.personRepository = personRepository;
        this.medicalRecordRepository = medicalRecordRepository;
    }

    /**
     * Adds a new {@link FireStation} mapping.
     *
     * @param fireStation The FireStation object to add.
     */
    public void addFireStation(FireStation fireStation) {
        LOGGER.info("Adding new fire station: {}", fireStation);
        fireStationRepository.addFireStation(fireStation);
    }

    /**
     * Deletes the {@link FireStation} mapping for the specified address.
     *
     * @param address The address of the FireStation to delete.
     * @return True if the FireStation was deleted, false otherwise.
     */
    public boolean deleteFireStation(String address) {
        LOGGER.info("Deleting fire station mapping for address: {}", address);
        return fireStationRepository.deleteFireStation(address);
    }

    /**
     * Updates the station number of an existing {@link FireStation} mapping.
     *
     * @param address          The address of the FireStation to update.
     * @param newStationNumber The new station number to assign.
     * @return True if the FireStation was updated, false otherwise.
     */
    public boolean updateFireStation(String address, String newStationNumber) {
        LOGGER.info("Updating fire station for address={} to station={}", address, newStationNumber);
        return fireStationRepository.updateFireStation(address, newStationNumber);
    }

    /**
     * Retrieves information about persons covered by a specific fire station.
     * Returns a map including details about each person, and counts of adults/children.
     *
     * @param stationNumber The fire station number to filter by.
     * @return A map containing 'persons' (list of person details),
     *         'adultCount', and 'childCount'.
     */
    public Map<String, Object> getPeopleCoveredByFireStation(String stationNumber) {
        LOGGER.debug("Fetching people covered by station number: {}", stationNumber);

        List<String> addresses = fireStationRepository.getAllFireStations().stream()
                .filter(fs -> fs.getStation().equals(stationNumber))
                .map(FireStation::getAddress)
                .collect(Collectors.toList());

        List<Person> coveredPersons = personRepository.getAllPersons().stream()
                .filter(person -> addresses.contains(person.getAddress()))
                .collect(Collectors.toList());

        List<Map<String, String>> personDetails = new ArrayList<>();
        int adultCount = 0;
        int childCount = 0;

        for (Person person : coveredPersons) {
            Map<String, String> details = new HashMap<>();
            details.put("firstName", person.getFirstName());
            details.put("lastName", person.getLastName());
            details.put("address", person.getAddress());
            details.put("phone", person.getPhone());

            Optional<MedicalRecord> medicalRecord =
                    medicalRecordRepository.getMedicalRecordByName(person.getFirstName(), person.getLastName());
            int age = medicalRecord.map(MedicalRecord::getAge).orElse(-1);

            if (age >= 0 && age <= 18) {
                childCount++;
            } else if (age > 18) {
                adultCount++;
            }
            personDetails.add(details);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("persons", personDetails);
        result.put("adultCount", adultCount);
        result.put("childCount", childCount);

        return result;
    }

    /**
     * Retrieves flood information for multiple fire stations, returning data
     * organized by address, and including each resident's basic and medical info.
     *
     * @param stations A list of station numbers (integers).
     * @return A map where the key is an address, and the value is a list of residents with associated info.
     */
    public Map<String, List<Map<String, Object>>> getFloodStationsInfo(List<Integer> stations) {
        LOGGER.debug("Fetching flood information for stations={}", stations);

        List<FireStation> allFireStations = fireStationRepository.getAllFireStations();
        Set<String> addresses = new HashSet<>();

        for (Integer station : stations) {
            String stationStr = station.toString();
            allFireStations.stream()
                    .filter(fs -> fs.getStation().equals(stationStr))
                    .map(FireStation::getAddress)
                    .forEach(addresses::add);
        }

        Map<String, List<Map<String, Object>>> result = new HashMap<>();

        for (String address : addresses) {
            List<Person> personsAtAddress = personRepository.findByAddress(address);

            List<Map<String, Object>> inhabitants = personsAtAddress.stream()
                    .map(person -> {
                        Map<String, Object> inhabitantInfo = new HashMap<>();
                        inhabitantInfo.put("firstName", person.getFirstName());
                        inhabitantInfo.put("lastName", person.getLastName());
                        inhabitantInfo.put("phone", person.getPhone());

                        medicalRecordRepository.getMedicalRecordByName(person.getFirstName(), person.getLastName())
                                .ifPresent(medicalRecord -> {
                                    inhabitantInfo.put("age", medicalRecord.getAge());
                                    inhabitantInfo.put("medications", medicalRecord.getMedications());
                                    inhabitantInfo.put("allergies", medicalRecord.getAllergies());
                                });
                        return inhabitantInfo;
                    })
                    .collect(Collectors.toList());

            result.put(address, inhabitants);
        }
        return result;
    }
}
