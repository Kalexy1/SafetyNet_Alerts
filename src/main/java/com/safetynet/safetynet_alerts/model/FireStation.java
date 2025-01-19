package com.safetynet.safetynet_alerts.model;

import java.util.Objects;

/**
 * Represents a mapping between a physical address and a fire station number.
 */
public class FireStation {

    private String address;
    private String station;

    /**
     * Default constructor required for serialization/deserialization.
     */
    public FireStation() {
    }

    /**
     * Constructs a FireStation with an address and a station number.
     *
     * @param address The address covered by the fire station.
     * @param station The station number (e.g., "1", "2", etc.).
     */
    public FireStation(String address, String station) {
        this.address = address;
        this.station = station;
    }

    /**
     * @return The address covered by the fire station.
     */
    public String getAddress() {
        return address;
    }

    /**
     * Sets the address for this FireStation mapping.
     *
     * @param address The address to assign.
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * @return The fire station number.
     */
    public String getStation() {
        return station;
    }

    /**
     * Sets the fire station number.
     *
     * @param station The station number to assign.
     */
    public void setStation(String station) {
        this.station = station;
    }

    /**
     * Compares this FireStation to another object for equality,
     * based on address and station fields.
     *
     * @param o The object to compare with.
     * @return True if both objects represent the same FireStation, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FireStation)) return false;
        FireStation that = (FireStation) o;
        return Objects.equals(address, that.address)
            && Objects.equals(station, that.station);
    }

    /**
     * Generates a hash code based on address and station fields.
     *
     * @return An integer hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(address, station);
    }

    /**
     * @return A string representation of this FireStation, including address and station number.
     */
    @Override
    public String toString() {
        return "FireStation{" +
               "address='" + address + '\'' +
               ", station='" + station + '\'' +
               '}';
    }
}
