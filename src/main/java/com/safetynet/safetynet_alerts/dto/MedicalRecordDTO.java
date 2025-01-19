package com.safetynet.safetynet_alerts.dto;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Data Transfer Object (DTO) representing a person's medical information,
 * typically used in API responses.
 * <p>
 * Contains birthdate, a list of medications, and allergies.
 * Provides a utility method {@link #calculateAge()} to compute the person's age based on the birthdate.
 */
public class MedicalRecordDTO {

    /**
     * The person's birthdate in the format "MM/dd/yyyy" (e.g., "03/06/1984").
     */
    private String birthdate;

    /**
     * A list of medications the person is taking, including dosage information.
     */
    private List<String> medications;

    /**
     * A list of known allergies.
     */
    private List<String> allergies;

    /**
     * Constructs a new MedicalRecordDTO with the specified details.
     *
     * @param birthdate   The person's birthdate in the format "MM/dd/yyyy".
     * @param medications A list of medications the person is taking.
     * @param allergies   A list of known allergies.
     */
    public MedicalRecordDTO(String birthdate, List<String> medications, List<String> allergies) {
        this.birthdate = birthdate;
        this.medications = medications;
        this.allergies = allergies;
    }

    /**
     * @return The person's birthdate in the format "MM/dd/yyyy".
     */
    public String getBirthdate() {
        return birthdate;
    }

    /**
     * Sets the person's birthdate.
     *
     * @param birthdate The birthdate in the format "MM/dd/yyyy".
     */
    public void setBirthdate(String birthdate) {
        this.birthdate = birthdate;
    }

    /**
     * @return A list of medications the person is taking.
     */
    public List<String> getMedications() {
        return medications;
    }

    /**
     * Sets the list of medications the person is taking.
     *
     * @param medications A list of medications.
     */
    public void setMedications(List<String> medications) {
        this.medications = medications;
    }

    /**
     * @return A list of the person's known allergies.
     */
    public List<String> getAllergies() {
        return allergies;
    }

    /**
     * Sets the list of known allergies.
     *
     * @param allergies A list of allergies.
     */
    public void setAllergies(List<String> allergies) {
        this.allergies = allergies;
    }

    /**
     * Calculates the person's age based on their birthdate.
     *
     * @return The person's age in years, or -1 if the birthdate is invalid.
     */
    public int calculateAge() {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            LocalDate birthDate = LocalDate.parse(this.birthdate, formatter);
            return Period.between(birthDate, LocalDate.now()).getYears();
        } catch (DateTimeParseException | NullPointerException e) {
            return -1;
        }
    }
}
