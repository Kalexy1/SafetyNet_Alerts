package com.safetynet.safetynet_alerts.dto;

/**
 * Data Transfer Object (DTO) representing a Person's information,
 * potentially enriched with a medical record (MedicalRecordDTO).
 * <p>
 * This DTO can be used to expose data in JSON format
 * via REST endpoints (e.g., /fire, /flood/stations, etc.).
 */
public class PersonDTO {

    /**
     * The person's first name.
     */
    private String firstName;

    /**
     * The person's last name.
     */
    private String lastName;

    /**
     * The person's postal address.
     */
    private String address;

    /**
     * The city where the person resides.
     */
    private String city;

    /**
     * The postal code.
     */
    private String zip;

    /**
     * The person's phone number.
     */
    private String phone;

    /**
     * The person's email address.
     */
    private String email;

    /**
     * The associated medical record, represented as a DTO.
     */
    private MedicalRecordDTO medicalRecord;

    /**
     * Full constructor for initializing all fields of the PersonDTO.
     *
     * @param firstName     The first name.
     * @param lastName      The last name.
     * @param address       The postal address.
     * @param city          The city.
     * @param zip           The postal code.
     * @param phone         The phone number.
     * @param email         The email address.
     * @param medicalRecord The medical record (as a DTO).
     */
    public PersonDTO(String firstName,
                     String lastName,
                     String address,
                     String city,
                     String zip,
                     String phone,
                     String email,
                     MedicalRecordDTO medicalRecord) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.city = city;
        this.zip = zip;
        this.phone = phone;
        this.email = email;
        this.medicalRecord = medicalRecord;
    }

    /**
     * @return The person's first name.
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * @param firstName Sets the person's first name.
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
     * @param lastName Sets the person's last name.
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * @return The person's postal address.
     */
    public String getAddress() {
        return address;
    }

    /**
     * @param address Sets the person's postal address.
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * @return The city where the person resides.
     */
    public String getCity() {
        return city;
    }

    /**
     * @param city Sets the city where the person resides.
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * @return The postal code.
     */
    public String getZip() {
        return zip;
    }

    /**
     * @param zip Sets the postal code.
     */
    public void setZip(String zip) {
        this.zip = zip;
    }

    /**
     * @return The person's phone number.
     */
    public String getPhone() {
        return phone;
    }

    /**
     * @param phone Sets the person's phone number.
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * @return The person's email address.
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param email Sets the person's email address.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @return The associated medical record (DTO).
     */
    public MedicalRecordDTO getMedicalRecord() {
        return medicalRecord;
    }

    /**
     * @param medicalRecord Sets the associated medical record (DTO).
     */
    public void setMedicalRecord(MedicalRecordDTO medicalRecord) {
        this.medicalRecord = medicalRecord;
    }
}
