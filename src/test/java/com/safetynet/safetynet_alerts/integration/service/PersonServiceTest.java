package com.safetynet.safetynet_alerts.integration.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.safetynet.safetynet_alerts.model.FireStation;
import com.safetynet.safetynet_alerts.model.MedicalRecord;
import com.safetynet.safetynet_alerts.model.Person;
import com.safetynet.safetynet_alerts.repository.FireStationRepository;
import com.safetynet.safetynet_alerts.repository.MedicalRecordRepository;
import com.safetynet.safetynet_alerts.repository.PersonRepository;
import com.safetynet.safetynet_alerts.service.PersonService;

class PersonServiceTest {

    private PersonRepository personRepository;
    private MedicalRecordRepository medicalRecordRepository;
    private FireStationRepository fireStationRepository;
    private PersonService personService;

    @BeforeEach
    void setUp() {
        personRepository = mock(PersonRepository.class);
        medicalRecordRepository = mock(MedicalRecordRepository.class);
        fireStationRepository = mock(FireStationRepository.class);
        personService = new PersonService(personRepository, medicalRecordRepository, fireStationRepository);
    }
    
    @Test
    void testGetAllPersons() {
        List<Person> mockPersons = List.of(new Person("John", "Boyd", "1509 Culver St", "Culver", "97451", "841-874-6512", "jaboyd@email.com"));
        when(personRepository.getAllPersons()).thenReturn(mockPersons);

        List<Person> result = personService.getAllPersons();

        assertEquals(1, result.size());
        assertEquals("John", result.get(0).getFirstName());
        verify(personRepository, times(1)).getAllPersons();
    }

    @Test
    void testAddPerson() {
        Person person = new Person("John", "Boyd", "1509 Culver St", "Culver", "97451", "841-874-6512", "jaboyd@email.com");
        doNothing().when(personRepository).addOrUpdatePerson(person);

        personService.addPerson(person);

        verify(personRepository, times(1)).addOrUpdatePerson(person);
    }

    @Test
    void testUpdatePerson_WhenPersonExists() {
        Person person = new Person("John", "Boyd", "1509 Culver St", "Culver", "97451", "841-874-6512", "jaboyd@email.com");
        when(personRepository.getPersonByName("John", "Boyd")).thenReturn(Optional.of(person));

        Optional<Person> updatedPerson = personService.updatePerson(person);

        assertTrue(updatedPerson.isPresent());
        assertEquals("John", updatedPerson.get().getFirstName());
        verify(personRepository, times(1)).addOrUpdatePerson(person);
    }

    @Test
    void testUpdatePerson_WhenPersonDoesNotExist() {
        Person person = new Person("John", "Doe", "123 Main St", "Culver", "97451", "123-456-7890", "jdoe@email.com");
        when(personRepository.getPersonByName("John", "Doe")).thenReturn(Optional.empty());

        Optional<Person> updatedPerson = personService.updatePerson(person);

        assertFalse(updatedPerson.isPresent());
        verify(personRepository, never()).addOrUpdatePerson(any(Person.class));
    }

    @Test
    void testDeletePerson_WhenPersonExists() {
        when(personRepository.deletePerson("John", "Boyd")).thenReturn(true);

        boolean result = personService.deletePerson("John", "Boyd");

        assertTrue(result);
        verify(personRepository, times(1)).deletePerson("John", "Boyd");
    }

    @Test
    void testDeletePerson_WhenPersonDoesNotExist() {
        when(personRepository.deletePerson("John", "Doe")).thenReturn(false);

        boolean result = personService.deletePerson("John", "Doe");

        assertFalse(result);
        verify(personRepository, times(1)).deletePerson("John", "Doe");
    }

    @Test
    void testGetChildrenByAddress() {
        List<Person> mockPersons = List.of(
                new Person("John", "Boyd", "1509 Culver St", "Culver", "97451", "841-874-6512", "jaboyd@email.com"),
                new Person("Tenley", "Boyd", "1509 Culver St", "Culver", "97451", "841-874-6512", "tenley@email.com")
        );

        MedicalRecord childRecord = new MedicalRecord("Tenley", "Boyd", "02/18/2012", List.of(), List.of());
        MedicalRecord adultRecord = new MedicalRecord("John", "Boyd", "03/06/1984", List.of(), List.of());

        when(personRepository.findByAddress("1509 Culver St")).thenReturn(mockPersons);
        when(medicalRecordRepository.getMedicalRecordByName("Tenley", "Boyd")).thenReturn(Optional.of(childRecord));
        when(medicalRecordRepository.getMedicalRecordByName("John", "Boyd")).thenReturn(Optional.of(adultRecord));

        List<String> result = personService.getChildrenByAddress("1509 Culver St");

        System.out.println("Result: " + result);

        assertEquals(1, result.size(), "Expected one child in the result.");
    }

    @Test
    void testGetPhoneNumbersByStation() {
        when(fireStationRepository.getAllFireStations()).thenReturn(List.of(
                new FireStation("1509 Culver St", "3"),
                new FireStation("29 15th St", "3")
        ));

        when(personRepository.getAllPersons()).thenReturn(List.of(
                new Person("John", "Boyd", "1509 Culver St", "Culver", "97451", "841-874-6512", "jaboyd@email.com"),
                new Person("Jacob", "Boyd", "29 15th St", "Culver", "97451", "841-874-6513", "jboyd@email.com")
        ));

        List<String> result = personService.getPhoneNumbersByStation("3");

        assertEquals(2, result.size());
        assertTrue(result.contains("841-874-6512"));
        assertTrue(result.contains("841-874-6513"));
    }

    @Test
    void testGetPersonsByAddress() {
        when(fireStationRepository.getAllFireStations()).thenReturn(List.of(
                new FireStation("1509 Culver St", "3")
        ));
        when(personRepository.findByAddress("1509 Culver St")).thenReturn(List.of(
                new Person("John", "Boyd", "1509 Culver St", "Culver", "97451", "841-874-6512", "jaboyd@email.com")
        ));
        when(medicalRecordRepository.getMedicalRecordByName("John", "Boyd")).thenReturn(Optional.of(
                new MedicalRecord("John", "Boyd", "03/06/1984", List.of("med1"), List.of("allergy1"))
        ));

        Map<String, Object> result = personService.getPersonsByAddress("1509 Culver St");

        assertEquals("3", result.get("firestationNumber"));
        assertEquals(1, ((List<?>) result.get("residents")).size());
    }

    @Test
    void testGetPersonInfoByLastName() {
        List<Person> persons = List.of(
                new Person("John", "Doe", "123 Main St", "City", "12345", "555-1234", "john.doe@example.com"),
                new Person("Jane", "Doe", "456 Elm St", "City", "12345", "555-5678", "jane.doe@example.com")
        );
        when(personRepository.getAllPersons()).thenReturn(persons);

        MedicalRecord johnRecord = new MedicalRecord("John", "Doe", "1980-01-01", List.of("Med1"), List.of("Allergy1"));
        MedicalRecord janeRecord = new MedicalRecord("Jane", "Doe", "1990-01-01", List.of("Med2"), List.of("Allergy2"));

        when(medicalRecordRepository.getMedicalRecordByName("John", "Doe")).thenReturn(Optional.of(johnRecord));
        when(medicalRecordRepository.getMedicalRecordByName("Jane", "Doe")).thenReturn(Optional.of(janeRecord));

        List<Map<String, Object>> result = personService.getPersonInfoByLastName("Doe");

        assertEquals(2, result.size());

        Map<String, Object> johnInfo = result.get(0);
        assertEquals("John", johnInfo.get("firstName"));
        assertEquals("Doe", johnInfo.get("lastName"));
        assertEquals("123 Main St", johnInfo.get("address"));
        assertEquals("john.doe@example.com", johnInfo.get("email"));
        assertEquals(johnRecord.getAge(), johnInfo.get("age"));
        assertEquals(johnRecord.getMedications(), johnInfo.get("medications"));
        assertEquals(johnRecord.getAllergies(), johnInfo.get("allergies"));

        Map<String, Object> janeInfo = result.get(1);
        assertEquals("Jane", janeInfo.get("firstName"));
        assertEquals("Doe", janeInfo.get("lastName"));
        assertEquals("456 Elm St", janeInfo.get("address"));
        assertEquals("jane.doe@example.com", janeInfo.get("email"));
        assertEquals(janeRecord.getAge(), janeInfo.get("age"));
        assertEquals(janeRecord.getMedications(), janeInfo.get("medications"));
        assertEquals(janeRecord.getAllergies(), janeInfo.get("allergies"));

        verify(personRepository, times(1)).getAllPersons();
        verify(medicalRecordRepository, times(1)).getMedicalRecordByName("John", "Doe");
        verify(medicalRecordRepository, times(1)).getMedicalRecordByName("Jane", "Doe");
    }

    @Test
    void testGetPersonInfoByLastName_NoMedicalRecord() {
        List<Person> persons = List.of(new Person("John", "Doe", "123 Main St", "City", "12345", "555-1234", "john.doe@example.com"));
        when(personRepository.getAllPersons()).thenReturn(persons);

        when(medicalRecordRepository.getMedicalRecordByName("John", "Doe")).thenReturn(Optional.empty());

        List<Map<String, Object>> result = personService.getPersonInfoByLastName("Doe");

        assertEquals(1, result.size());
        Map<String, Object> johnInfo = result.get(0);
        assertNull(johnInfo.get("age"));
        assertTrue(((List<?>) johnInfo.get("medications")).isEmpty());
        assertTrue(((List<?>) johnInfo.get("allergies")).isEmpty());

        verify(personRepository, times(1)).getAllPersons();
        verify(medicalRecordRepository, times(1)).getMedicalRecordByName("John", "Doe");
    }

    @Test
    void testGetEmailByCity() {
        List<Person> persons = List.of(
                new Person("John", "Doe", "123 Main St", "CityA", "12345", "555-1234", "john.doe@example.com"),
                new Person("Jane", "Smith", "456 Elm St", "CityA", "12345", "555-5678", "jane.smith@example.com"),
                new Person("Alice", "Brown", "789 Pine St", "CityB", "12345", "555-0000", "alice.brown@example.com")
        );
        when(personRepository.getAllPersons()).thenReturn(persons);

        List<String> emails = personService.getEmailsByCity("CityA");

        assertEquals(2, emails.size());
        assertTrue(emails.contains("john.doe@example.com"));
        assertTrue(emails.contains("jane.smith@example.com"));

        verify(personRepository, times(1)).getAllPersons();
    }

    @Test
    void testGetEmailByCity_NoMatches() {
        List<Person> persons = List.of(new Person("Alice", "Brown", "789 Pine St", "CityB", "12345", "555-0000", "alice.brown@example.com"));
        when(personRepository.getAllPersons()).thenReturn(persons);

        List<String> emails = personService.getEmailsByCity("CityA");

        assertTrue(emails.isEmpty());

        verify(personRepository, times(1)).getAllPersons();
    }
}
