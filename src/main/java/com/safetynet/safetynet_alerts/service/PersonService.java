package com.safetynet.safetynet_alerts.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.safetynet.safetynet_alerts.model.MedicalRecord;
import com.safetynet.safetynet_alerts.model.Person;
import com.safetynet.safetynet_alerts.repository.FireStationRepository;
import com.safetynet.safetynet_alerts.repository.MedicalRecordRepository;
import com.safetynet.safetynet_alerts.repository.PersonRepository;

/**
 * Business logic related to Person operations.
 */
@Service
public class PersonService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PersonService.class);

    private final PersonRepository personRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final FireStationRepository fireStationRepository;

    /**
     * Constructs the PersonService with required repositories.
     *
     * @param personRepository        Repository for Person data.
     * @param medicalRecordRepository Repository for MedicalRecord data.
     * @param fireStationRepository   Repository for FireStation data.
     */
    public PersonService(
            PersonRepository personRepository,
            MedicalRecordRepository medicalRecordRepository,
            FireStationRepository fireStationRepository
    ) {
        this.personRepository = personRepository;
        this.medicalRecordRepository = medicalRecordRepository;
        this.fireStationRepository = fireStationRepository;
    }

    /**
     * Retrieves all persons from the repository.
     *
     * @return A list of all Person objects.
     */
    public List<Person> getAllPersons() {
        LOGGER.info("Fetching all persons.");
        return personRepository.getAllPersons();
    }

    /**
     * Adds a new person to the repository or updates them if they already exist.
     *
     * @param person The Person object to add.
     */
    public void addPerson(Person person) {
        LOGGER.info("Adding person: {}", person);
        personRepository.addOrUpdatePerson(person);
    }

    /**
     * Updates an existing person if found in the repository.
     *
     * @param person The updated Person object.
     * @return An Optional containing the updated Person if found; otherwise, empty.
     */
    public Optional<Person> updatePerson(Person person) {
        return personRepository.getPersonByName(person.getFirstName(), person.getLastName())
                .map(existingPerson -> {
                    LOGGER.info("Updating person: {}", person);
                    personRepository.addOrUpdatePerson(person);
                    return person;
                });
    }

    /**
     * Deletes a person by their first and last name.
     *
     * @param firstName The person's first name.
     * @param lastName  The person's last name.
     * @return True if the deletion was successful; false otherwise.
     */
    public boolean deletePerson(String firstName, String lastName) {
        LOGGER.info("Deleting person: {} {}", firstName, lastName);
        return personRepository.deletePerson(firstName, lastName);
    }

    /**
     * Retrieves child information for a given address (childAlert endpoint).
     *
     * @param address The address to search for children.
     * @return A list of strings describing children and other household members.
     */
    public List<String> getChildrenByAddress(String address) {
        LOGGER.debug("Searching children at address: {}", address);
        List<Person> people = personRepository.findByAddress(address);
        List<String> result = new ArrayList<>();

        for (Person p : people) {
            Optional<MedicalRecord> mr = medicalRecordRepository.getMedicalRecordByName(p.getFirstName(), p.getLastName());
            if (mr.isPresent()) {
                int age = mr.get().getAge();
                if (age <= 18) {
                    String info = "Child: " + p.getFirstName() + " " + p.getLastName() +
                                  ", Age: " + age +
                                  ", Other members: " + people.stream()
                                    .filter(o -> !o.equals(p))
                                    .map(o -> o.getFirstName() + " " + o.getLastName())
                                    .collect(Collectors.joining(", "));
                    result.add(info);
                }
            }
        }
        return result;
    }

    /**
     * Retrieves phone numbers for persons covered by a specific fire station (phoneAlert endpoint).
     *
     * @param stationNumber The station number to filter by.
     * @return A list of distinct phone numbers of covered persons.
     */
    public List<String> getPhoneNumbersByStation(String stationNumber) {
        LOGGER.debug("Searching phone numbers for station {}", stationNumber);
        List<String> addresses = fireStationRepository.getAllFireStations().stream()
                .filter(fs -> fs.getStation().equals(stationNumber))
                .map(fs -> fs.getAddress())
                .collect(Collectors.toList());

        return personRepository.getAllPersons().stream()
                .filter(p -> addresses.contains(p.getAddress()))
                .map(Person::getPhone)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Retrieves persons living at a given address, including their medical records, and the associated fire station number.
     * (fire endpoint).
     *
     * @param address The address to search.
     * @return A map containing 'firestationNumber' and a list of 'residents' details.
     */
    public Map<String, Object> getPersonsByAddress(String address) {
        LOGGER.debug("Searching persons at address={} along with station info", address);
        String stationNumber = fireStationRepository.getAllFireStations().stream()
                .filter(fs -> fs.getAddress().equalsIgnoreCase(address))
                .map(fs -> fs.getStation())
                .findFirst()
                .orElse("N/A");

        List<Person> persons = personRepository.findByAddress(address);
        List<Map<String, Object>> personsDetails = new ArrayList<>();

        for (Person p : persons) {
            Map<String, Object> details = new HashMap<>();
            details.put("firstName", p.getFirstName());
            details.put("lastName", p.getLastName());
            details.put("phone", p.getPhone());

            Optional<MedicalRecord> mr = medicalRecordRepository.getMedicalRecordByName(p.getFirstName(), p.getLastName());
            if (mr.isPresent()) {
                details.put("age", mr.get().getAge());
                details.put("medications", mr.get().getMedications());
                details.put("allergies", mr.get().getAllergies());
            } else {
                details.put("age", null);
                details.put("medications", List.of());
                details.put("allergies", List.of());
            }
            personsDetails.add(details);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("firestationNumber", stationNumber);
        response.put("residents", personsDetails);
        return response;
    }

    /**
     * Retrieves person information (age, email, medical data) for all who match the specified last name (personInfo endpoint).
     *
     * @param lastName The last name to filter persons by.
     * @return A list of maps, each containing info about a person.
     */
    public List<Map<String, Object>> getPersonInfoByLastName(String lastName) {
        LOGGER.debug("Searching for person info by lastName={}", lastName);
        List<Person> matched = personRepository.getAllPersons().stream()
                .filter(p -> p.getLastName().equalsIgnoreCase(lastName))
                .collect(Collectors.toList());

        List<Map<String, Object>> result = new ArrayList<>();
        for (Person p : matched) {
            Map<String, Object> info = new HashMap<>();
            info.put("firstName", p.getFirstName());
            info.put("lastName", p.getLastName());
            info.put("address", p.getAddress());
            info.put("email", p.getEmail());

            Optional<MedicalRecord> mr = medicalRecordRepository.getMedicalRecordByName(p.getFirstName(), p.getLastName());
            if (mr.isPresent()) {
                info.put("age", mr.get().getAge());
                info.put("medications", mr.get().getMedications());
                info.put("allergies", mr.get().getAllergies());
            } else {
                info.put("age", null);
                info.put("medications", List.of());
                info.put("allergies", List.of());
            }
            result.add(info);
        }
        return result;
    }

    /**
     * Retrieves email addresses of people living in a specific city (communityEmail endpoint).
     *
     * @param city The city to filter persons by.
     * @return A list of emails of the persons found.
     */
    public List<String> getEmailsByCity(String city) {
        LOGGER.debug("Fetching emails for city: {}", city);
        return personRepository.getAllPersons().stream()
                .filter(person -> person.getCity() != null && person.getCity().equalsIgnoreCase(city))
                .map(Person::getEmail)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }
}
