package com.safetynet.safetynet_alerts.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.safetynet.safetynet_alerts.model.MedicalRecord;
import com.safetynet.safetynet_alerts.repository.MedicalRecordRepository;

/**
 * Business logic for managing MedicalRecord entities.
 */
@Service
public class MedicalRecordService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MedicalRecordService.class);

    private final MedicalRecordRepository medicalRecordRepository;

    /**
     * Constructs the MedicalRecordService with the required repository.
     *
     * @param medicalRecordRepository The repository for managing medical records.
     */
    public MedicalRecordService(MedicalRecordRepository medicalRecordRepository) {
        this.medicalRecordRepository = medicalRecordRepository;
    }

    /**
     * Retrieves all medical records from the repository.
     *
     * @return A list of all MedicalRecord objects.
     */
    public List<MedicalRecord> getAllMedicalRecords() {
        LOGGER.debug("Fetching all medical records.");
        return medicalRecordRepository.getAllMedicalRecords();
    }

    /**
     * Retrieves a medical record by the person's first and last names.
     *
     * @param firstName The person's first name.
     * @param lastName  The person's last name.
     * @return An Optional containing the MedicalRecord if found, otherwise empty.
     */
    public Optional<MedicalRecord> getMedicalRecordByName(String firstName, String lastName) {
        return medicalRecordRepository.getMedicalRecordByName(firstName, lastName);
    }

    /**
     * Adds or updates a medical record in the repository.
     *
     * @param mr The MedicalRecord object to add or update.
     */
    public void addOrUpdateMedicalRecord(MedicalRecord mr) {
        LOGGER.info("Adding/updating medical record: {}", mr);
        medicalRecordRepository.addOrUpdateMedicalRecord(mr);
    }

    /**
     * Deletes a medical record identified by the person's first and last names.
     *
     * @param firstName The person's first name.
     * @param lastName  The person's last name.
     */
    public void deleteMedicalRecord(String firstName, String lastName) {
        LOGGER.info("Deleting medical record for: {} {}", firstName, lastName);
        medicalRecordRepository.deleteMedicalRecord(firstName, lastName);
    }
}
