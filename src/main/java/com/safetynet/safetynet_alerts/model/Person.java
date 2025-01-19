package com.safetynet.safetynet_alerts.model;

/**
 * Represents a person with basic personal information,
 * including name, address, contact data, and city details.
 */
public class Person {

    private String firstName;
    private String lastName;
    private String address;
    private String city;
    private String zip;
    private String phone;
    private String email;

    /**
     * Default constructor required for serialization/deserialization.
     */
    public Person() {}

    /**
     * Constructs a Person with all fields.
     *
     * @param firstName The person's first name.
     * @param lastName  The person's last name.
     * @param address   The person's address.
     * @param city      The city where the person resides.
     * @param zip       The ZIP or postal code.
     * @param phone     The phone number.
     * @param email     The email address.
     */
    public Person(String firstName, String lastName, String address, String city,
                  String zip, String phone, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.city = city;
        this.zip = zip;
        this.phone = phone;
        this.email = email;
    }

    /**
     * @return The first name of the person.
     */
    public String getFirstName() { return firstName; }

    /**
     * Sets the first name of the person.
     *
     * @param firstName The first name to set.
     */
    public void setFirstName(String firstName) { this.firstName = firstName; }

    /**
     * @return The last name of the person.
     */
    public String getLastName() { return lastName; }

    /**
     * Sets the last name of the person.
     *
     * @param lastName The last name to set.
     */
    public void setLastName(String lastName) { this.lastName = lastName; }

    /**
     * @return The address of the person.
     */
    public String getAddress() { return address; }

    /**
     * Sets the address of the person.
     *
     * @param address The address to set.
     */
    public void setAddress(String address) { this.address = address; }

    /**
     * @return The city where the person resides.
     */
    public String getCity() { return city; }

    /**
     * Sets the city where the person resides.
     *
     * @param city The city to set.
     */
    public void setCity(String city) { this.city = city; }

    /**
     * @return The ZIP or postal code of the person.
     */
    public String getZip() { return zip; }

    /**
     * Sets the ZIP or postal code of the person.
     *
     * @param zip The postal code to set.
     */
    public void setZip(String zip) { this.zip = zip; }

    /**
     * @return The phone number of the person.
     */
    public String getPhone() { return phone; }

    /**
     * Sets the phone number of the person.
     *
     * @param phone The phone number to set.
     */
    public void setPhone(String phone) { this.phone = phone; }

    /**
     * @return The email address of the person.
     */
    public String getEmail() { return email; }

    /**
     * Sets the email address of the person.
     *
     * @param email The email to set.
     */
    public void setEmail(String email) { this.email = email; }

    /**
     * Compares this person to another object for equality.
     * Uses firstName, lastName, and address as identifiers.
     *
     * @param o The object to compare with.
     * @return True if the objects are equal, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Person)) return false;
        Person person = (Person) o;
        return java.util.Objects.equals(firstName, person.firstName)
            && java.util.Objects.equals(lastName, person.lastName)
            && java.util.Objects.equals(address, person.address);
    }

    /**
     * Generates a hash code based on firstName, lastName, and address.
     *
     * @return An integer hash code.
     */
    @Override
    public int hashCode() {
        return java.util.Objects.hash(firstName, lastName, address);
    }

    /**
     * @return A string representation of the person, including all fields.
     */
    @Override
    public String toString() {
        return "Person{" +
               "firstName='" + firstName + '\'' +
               ", lastName='" + lastName + '\'' +
               ", address='" + address + '\'' +
               ", city='" + city + '\'' +
               ", zip='" + zip + '\'' +
               ", phone='" + phone + '\'' +
               ", email='" + email + '\'' +
               '}';
    }
}
