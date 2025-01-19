package com.safetynet.safetynet_alerts.model;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a person's medical record, including birth date,
 * medications, and allergies.
 */
public class MedicalRecord {

    private String firstName;
    private String lastName;
    private String birthdate;
    private List<String> medications;
    private List<String> allergies;

    /**
     * Default constructor required for serialization/deserialization.
     */
    public MedicalRecord() {}

    /**
     * Constructs a MedicalRecord with all fields.
     *
     * @param firstName   The person's first name.
     * @param lastName    The person's last name.
     * @param birthdate   The person's birth date in MM/dd/yyyy format.
     * @param medications A list of medications.
     * @param allergies   A list of allergies.
     */
    public MedicalRecord(
            String firstName,
            String lastName,
            String birthdate,
            List<String> medications,
            List<String> allergies
    ) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthdate = birthdate;
        this.medications = medications;
        this.allergies = allergies;
    }

    /**
     * @return The person's first name.
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Sets the person's first name.
     *
     * @param firstName The first name to set.
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * @return The person's last name.
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Sets the person's last name.
     *
     * @param lastName The last name to set.
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * @return The person's birth date in MM/dd/yyyy format.
     */
    public String getBirthdate() {
        return birthdate;
    }

    /**
     * Sets the person's birth date.
     *
     * @param birthdate The birth date to set (MM/dd/yyyy).
     */
    public void setBirthdate(String birthdate) {
        this.birthdate = birthdate;
    }

    /**
     * @return An unmodifiable list of the person's medications.
     */
    public List<String> getMedications() {
        return medications == null ? List.of() : Collections.unmodifiableList(medications);
    }

    /**
     * Sets the person's medications.
     *
     * @param medications A list of medication descriptions.
     */
    public void setMedications(List<String> medications) {
        this.medications = (medications == null) ? List.of() : medications;
    }

    /**
     * @return An unmodifiable list of the person's allergies.
     */
    public List<String> getAllergies() {
        return allergies == null ? List.of() : Collections.unmodifiableList(allergies);
    }

    /**
     * Sets the person's allergies.
     *
     * @param allergies A list of allergy descriptions.
     */
    public void setAllergies(List<String> allergies) {
        this.allergies = (allergies == null) ? List.of() : allergies;
    }

    /**
     * Calculates the person's age based on the birth date.
     *
     * @return The person's age, or -1 if the date is invalid.
     */
    public int getAge() {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            LocalDate birthDate = LocalDate.parse(birthdate, formatter);
            return Period.between(birthDate, LocalDate.now()).getYears();
        } catch (DateTimeParseException | NullPointerException e) {
            return -1;
        }
    }

    /**
     * Compares this MedicalRecord with another object for equality,
     * using firstName, lastName, and birthdate as key identifiers.
     *
     * @param o The object to compare with.
     * @return True if they represent the same medical record, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MedicalRecord)) return false;
        MedicalRecord that = (MedicalRecord) o;
        return Objects.equals(firstName, that.firstName)
            && Objects.equals(lastName, that.lastName)
            && Objects.equals(birthdate, that.birthdate);
    }

    /**
     * Generates a hash code based on firstName, lastName, and birthdate.
     *
     * @return An integer hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(firstName, lastName, birthdate);
    }

    /**
     * @return A string representation of the MedicalRecord.
     */
    @Override
    public String toString() {
        return "MedicalRecord{" +
               "firstName='" + firstName + '\'' +
               ", lastName='" + lastName + '\'' +
               ", birthdate='" + birthdate + '\'' +
               ", medications=" + medications +
               ", allergies=" + allergies +
               '}';
    }
}
