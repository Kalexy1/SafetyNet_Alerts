package com.safetynet.safetynet_alerts.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.safetynet.safetynet_alerts.model.FireStation;
import com.safetynet.safetynet_alerts.model.MedicalRecord;
import com.safetynet.safetynet_alerts.model.Person;
import com.safetynet.safetynet_alerts.service.FireStationService;
import com.safetynet.safetynet_alerts.service.MedicalRecordService;
import com.safetynet.safetynet_alerts.service.PersonService;

/**
 * Main controller regrouping endpoints to manage persons, fire stations,
 * medical records, as well as specific requests (childAlert, phoneAlert, etc.).
 */
@RestController
public class AlertController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AlertController.class);

    private final PersonService personService;
    private final FireStationService fireStationService;
    private final MedicalRecordService medicalRecordService;

    /**
     * Constructs the AlertController with all required services.
     *
     * @param personService         The service handling Person operations.
     * @param fireStationService    The service handling FireStation operations.
     * @param medicalRecordService  The service handling MedicalRecord operations.
     */
    public AlertController(
            PersonService personService,
            FireStationService fireStationService,
            MedicalRecordService medicalRecordService
    ) {
        this.personService = personService;
        this.fireStationService = fireStationService;
        this.medicalRecordService = medicalRecordService;
    }

    /**
     * Retrieves information about people covered by a specific fire station number.
     * Endpoint: GET /firestation?stationNumber=...
     *
     * @param stationNumber The station number used to filter covered persons.
     * @return A map containing persons' details and the count of adults/children.
     */
    @GetMapping("/firestation")
    public ResponseEntity<Map<String, Object>> getFireStationInfo(@RequestParam String stationNumber) {
        LOGGER.info("GET /firestation?stationNumber={}", stationNumber);
        Map<String, Object> response = fireStationService.getPeopleCoveredByFireStation(stationNumber);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves a list of children (and other household members) living at a specific address.
     * Endpoint: GET /childAlert?address=...
     *
     * @param address The address used to filter the children.
     * @return A list of string data about the children at the given address.
     */
    @GetMapping("/childAlert")
    public ResponseEntity<List<String>> getChildrenByAddress(@RequestParam String address) {
        LOGGER.info("GET /childAlert?address={}", address);
        List<String> childrenInfo = personService.getChildrenByAddress(address);
        return ResponseEntity.ok(childrenInfo);
    }

    /**
     * Retrieves phone numbers of people covered by a specific fire station.
     * Endpoint: GET /phoneAlert?firestation=...
     *
     * @param firestation The station number.
     * @return A list of phone numbers associated with that station.
     */
    @GetMapping("/phoneAlert")
    public ResponseEntity<List<String>> getPhoneNumbersByFireStation(@RequestParam("firestation") String firestation) {
        LOGGER.info("GET /phoneAlert?firestation={}", firestation);
        List<String> phones = personService.getPhoneNumbersByStation(firestation);
        return ResponseEntity.ok(phones);
    }

    /**
     * Retrieves persons living at a specific address, including their medical record info,
     * and the associated fire station.
     * Endpoint: GET /fire?address=...
     *
     * @param address The address used to filter.
     * @return A map with persons' details and station info.
     */
    @GetMapping("/fire")
    public ResponseEntity<Map<String, Object>> getPersonsByAddressWithMedicalRecord(@RequestParam("address") String address) {
        LOGGER.info("GET /fire?address={}", address);
        Map<String, Object> result = personService.getPersonsByAddress(address);
        return ResponseEntity.ok(result);
    }

    /**
     * Retrieves a list of households by station numbers, including persons' medical info for each.
     * Endpoint: GET /flood/stations?stations=...
     *
     * @param stations A list of station numbers.
     * @return A map associating each address with a list of persons' data.
     */
    @GetMapping("/flood/stations")
    public ResponseEntity<Map<String, List<Map<String, Object>>>> getFloodStationsInfo(@RequestParam List<Integer> stations) {
        LOGGER.info("GET /flood/stations?stations={}", stations);
        Map<String, List<Map<String, Object>>> result = fireStationService.getFloodStationsInfo(stations);
        return ResponseEntity.ok(result);
    }

    /**
     * Retrieves person information by the last name.
     * Endpoint: GET /personInfo?lastName=...
     *
     * @param lastName The last name used to filter persons.
     * @return A list of detailed info (age, email, medical data) for matching persons.
     */
    @GetMapping("/personInfo")
    public ResponseEntity<List<Map<String, Object>>> getPersonsByLastName(@RequestParam("lastName") String lastName) {
        LOGGER.info("GET /personInfo?lastName={}", lastName);
        List<Map<String, Object>> info = personService.getPersonInfoByLastName(lastName);
        return ResponseEntity.ok(info);
    }

    /**
     * Retrieves email addresses of people living in a specific city.
     * Endpoint: GET /communityEmail?city=...
     *
     * @param city The city used to filter persons.
     * @return A list of emails.
     */
    @GetMapping("/communityEmail")
    public ResponseEntity<List<String>> getEmailsByCity(@RequestParam String city) {
        LOGGER.info("GET /communityEmail?city={}", city);
        List<String> emails = personService.getEmailsByCity(city);
        return ResponseEntity.ok(emails);
    }

    /**
     * Adds a new person.
     * Endpoint: POST /person
     *
     * @param person The person object to add.
     * @return A ResponseEntity with a success message and HTTP status 201.
     */
    @PostMapping("/person")
    public ResponseEntity<String> addPerson(@RequestBody Person person) {
        LOGGER.info("POST /person -> Adding person: {}", person);
        personService.addPerson(person);
        return ResponseEntity.status(HttpStatus.CREATED).body("Person added successfully.");
    }

    /**
     * Updates an existing person.
     * Endpoint: PUT /person
     *
     * @param person The updated person object.
     * @return A ResponseEntity with a success message if updated, or an error if not found.
     */
    @PutMapping("/person")
    public ResponseEntity<String> updatePerson(@RequestBody Person person) {
        LOGGER.info("PUT /person -> Updating person: {}", person);
        Optional<Person> updatedPerson = personService.updatePerson(person);
        if (updatedPerson.isPresent()) {
            return ResponseEntity.ok("Person updated successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Person not found.");
        }
    }

    /**
     * Deletes a person by first and last name.
     * Endpoint: DELETE /person
     *
     * @param firstName The first name of the person.
     * @param lastName  The last name of the person.
     * @return A ResponseEntity indicating success or failure (404 if not found).
     */
    @DeleteMapping("/person")
    public ResponseEntity<String> deletePerson(
            @RequestParam String firstName,
            @RequestParam String lastName
    ) {
        LOGGER.info("DELETE /person -> Deleting person: {} {}", firstName, lastName);
        boolean isDeleted = personService.deletePerson(firstName, lastName);
        if (isDeleted) {
            return ResponseEntity.ok("Person deleted successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Person not found.");
        }
    }

    /**
     * Adds a new FireStation mapping.
     * Endpoint: POST /firestation
     *
     * @param fs The FireStation object containing address and station number.
     * @return A ResponseEntity with a success message and HTTP status 201.
     */
    @PostMapping("/firestation")
    public ResponseEntity<String> addFireStation(@RequestBody FireStation fs) {
        LOGGER.info("POST /firestation -> Adding fire station: {}", fs);
        fireStationService.addFireStation(fs);
        return ResponseEntity.status(HttpStatus.CREATED).body("FireStation added successfully.");
    }

    /**
     * Updates an existing FireStation mapping by address and station.
     * Endpoint: PUT /firestation
     *
     * @param address The address of the FireStation to update.
     * @param station The new station number.
     * @return A ResponseEntity with a success or error message.
     */
    @PutMapping("/firestation")
    public ResponseEntity<String> updateFireStation(
            @RequestParam String address,
            @RequestParam String station
    ) {
        LOGGER.info("PUT /firestation -> Updating fire station for address={} to station={}", address, station);
        boolean updated = fireStationService.updateFireStation(address, station);
        if (updated) {
            return ResponseEntity.ok("FireStation updated successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("FireStation not found for the given address.");
        }
    }

    /**
     * Deletes a FireStation mapping by address.
     * Endpoint: DELETE /firestation
     *
     * @param address The address of the FireStation to delete.
     * @return A ResponseEntity indicating success or failure (404 if not found).
     */
    @DeleteMapping("/firestation")
    public ResponseEntity<String> deleteFireStation(@RequestParam String address) {
        LOGGER.info("DELETE /firestation -> Deleting fire station for address={}", address);
        boolean removed = fireStationService.deleteFireStation(address);
        if (removed) {
            return ResponseEntity.ok("FireStation deleted successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("FireStation not found.");
        }
    }

    /**
     * Adds a new MedicalRecord.
     * Endpoint: POST /medicalRecord
     *
     * @param mr The MedicalRecord object to add.
     * @return A ResponseEntity with a success message and HTTP status 201.
     */
    @PostMapping("/medicalRecord")
    public ResponseEntity<String> addMedicalRecord(@RequestBody MedicalRecord mr) {
        LOGGER.info("POST /medicalRecord -> Adding MedicalRecord: {}", mr);
        medicalRecordService.addOrUpdateMedicalRecord(mr);
        return ResponseEntity.status(HttpStatus.CREATED).body("MedicalRecord added successfully.");
    }

    /**
     * Updates an existing MedicalRecord.
     * Endpoint: PUT /medicalRecord
     *
     * @param mr The updated MedicalRecord object.
     * @return A ResponseEntity with a success message.
     */
    @PutMapping("/medicalRecord")
    public ResponseEntity<String> updateMedicalRecord(@RequestBody MedicalRecord mr) {
        LOGGER.info("PUT /medicalRecord -> Updating MedicalRecord: {}", mr);
        medicalRecordService.addOrUpdateMedicalRecord(mr);
        return ResponseEntity.ok("MedicalRecord updated successfully.");
    }

    /**
     * Deletes a MedicalRecord by first and last name.
     * Endpoint: DELETE /medicalRecord
     *
     * @param firstName The first name of the person.
     * @param lastName  The last name of the person.
     * @return A ResponseEntity with a success message.
     */
    @DeleteMapping("/medicalRecord")
    public ResponseEntity<String> deleteMedicalRecord(
            @RequestParam String firstName,
            @RequestParam String lastName
    ) {
        LOGGER.info("DELETE /medicalRecord -> Deleting MedicalRecord: {} {}", firstName, lastName);
        medicalRecordService.deleteMedicalRecord(firstName, lastName);
        return ResponseEntity.ok("MedicalRecord deleted successfully.");
    }

    /**
     * Handles JSON parse exceptions.
     *
     * @param ex The HttpMessageNotReadableException thrown when the request payload is invalid.
     * @return A 400 BAD_REQUEST response with an error message.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest().body("Invalid JSON payload: " + ex.getMessage());
    }
}
