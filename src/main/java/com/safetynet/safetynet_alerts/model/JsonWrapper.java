package com.safetynet.safetynet_alerts.model;

import java.util.List;

/**
 * Represents the root structure of the JSON file that contains
 * lists of {@link Person}, {@link FireStation}, and {@link MedicalRecord}.
 * <p>
 * This class allows deserialization of the JSON content by Jackson,
 * grouping:
 * <ul>
 *   <li>{@link #persons}: the list of persons</li>
 *   <li>{@link #firestations}: the list of fire stations</li>
 *   <li>{@link #medicalrecords}: the list of medical records</li>
 * </ul>
 * Each section corresponds to a JSON field and can be used to
 * initialize your application data.
 */
public class JsonWrapper {

    /**
     * The list of persons ({@link Person}).
     * <p>
     * Corresponds to the "persons" key in the JSON.
     */
    private List<Person> persons;

    /**
     * The list of fire stations ({@link FireStation}).
     * <p>
     * Corresponds to the "firestations" key in the JSON.
     */
    private List<FireStation> firestations;

    /**
     * The list of medical records ({@link MedicalRecord}).
     * <p>
     * Corresponds to the "medicalrecords" key in the JSON.
     */
    private List<MedicalRecord> medicalrecords;

    /**
     * @return The list of persons ({@link Person}).
     */
    public List<Person> getPersons() {
        return persons;
    }

    /**
     * Sets the list of persons from the JSON file.
     *
     * @param persons A list of {@link Person} objects.
     */
    public void setPersons(List<Person> persons) {
        this.persons = persons;
    }

    /**
     * @return The list of fire stations ({@link FireStation}).
     */
    public List<FireStation> getFirestations() {
        return firestations;
    }

    /**
     * Sets the list of fire stations from the JSON file.
     *
     * @param firestations A list of {@link FireStation} objects.
     */
    public void setFirestations(List<FireStation> firestations) {
        this.firestations = firestations;
    }

    /**
     * @return The list of medical records ({@link MedicalRecord}).
     */
    public List<MedicalRecord> getMedicalrecords() {
        return medicalrecords;
    }

    /**
     * Sets the list of medical records from the JSON file.
     *
     * @param medicalrecords A list of {@link MedicalRecord} objects.
     */
    public void setMedicalrecords(List<MedicalRecord> medicalrecords) {
        this.medicalrecords = medicalrecords;
    }
}
