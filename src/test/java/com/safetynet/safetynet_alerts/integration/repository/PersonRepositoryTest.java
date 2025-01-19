package com.safetynet.safetynet_alerts.integration.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.safetynet.safetynet_alerts.model.Person;
import com.safetynet.safetynet_alerts.repository.PersonRepository;

class PersonRepositoryTest {

    private PersonRepository personRepository;
    private ObjectMapper mockObjectMapper;

    @TempDir
    Path tempDir;

    private File tempFile;

    @BeforeEach
    void setUp() throws IOException {
        mockObjectMapper = mock(ObjectMapper.class);
        tempFile = tempDir.resolve("data.json").toFile();
        personRepository = new PersonRepository(mockObjectMapper, "data.json", tempFile.getAbsolutePath());
    }

    @Test
    void getAllPersons_ShouldReturnAllPersons() {
        Person person1 = new Person("John", "Doe", "123 Main St", "City", "12345", "123-456-7890", "john.doe@email.com");
        Person person2 = new Person("Jane", "Smith", "456 Elm St", "Town", "67890", "987-654-3210", "jane.smith@email.com");
        personRepository.addOrUpdatePerson(person1);
        personRepository.addOrUpdatePerson(person2);

        List<Person> persons = personRepository.getAllPersons();

        assertEquals(2, persons.size());
        assertTrue(persons.contains(person1));
        assertTrue(persons.contains(person2));
    }

    @Test
    void getPersonByName_ShouldReturnPerson_WhenPersonExists() {
        Person person = new Person("John", "Doe", "123 Main St", "City", "12345", "123-456-7890", "john.doe@email.com");
        personRepository.addOrUpdatePerson(person);

        Optional<Person> result = personRepository.getPersonByName("John", "Doe");

        assertTrue(result.isPresent());
        assertEquals(person, result.get());
    }

    @Test
    void getPersonByName_ShouldReturnEmpty_WhenPersonDoesNotExist() {
        Optional<Person> result = personRepository.getPersonByName("Nonexistent", "Person");

        assertFalse(result.isPresent());
    }

    @Test
    void addOrUpdatePerson_ShouldAddNewPerson() {
        Person person = new Person("John", "Doe", "123 Main St", "City", "12345", "123-456-7890", "john.doe@email.com");
        personRepository.addOrUpdatePerson(person);

        Optional<Person> result = personRepository.getPersonByName("John", "Doe");

        assertTrue(result.isPresent());
        assertEquals(person, result.get());
    }

    @Test
    void addOrUpdatePerson_ShouldUpdateExistingPerson() {
        Person person = new Person("John", "Doe", "123 Main St", "City", "12345", "123-456-7890", "john.doe@email.com");
        personRepository.addOrUpdatePerson(person);

        Person updatedPerson = new Person("John", "Doe", "456 Elm St", "Town", "67890", "987-654-3210", "john.doe@newemail.com");
        personRepository.addOrUpdatePerson(updatedPerson);

        Optional<Person> result = personRepository.getPersonByName("John", "Doe");

        assertTrue(result.isPresent());
        assertEquals(updatedPerson, result.get());
    }

    @Test
    void deletePerson_ShouldRemovePerson_WhenPersonExists() {
        Person person = new Person("John", "Doe", "123 Main St", "City", "12345", "123-456-7890", "john.doe@email.com");
        personRepository.addOrUpdatePerson(person);

        boolean deleted = personRepository.deletePerson("John", "Doe");

        assertTrue(deleted);
        Optional<Person> result = personRepository.getPersonByName("John", "Doe");
        assertFalse(result.isPresent());
    }

    @Test
    void deletePerson_ShouldReturnFalse_WhenPersonDoesNotExist() {
        boolean deleted = personRepository.deletePerson("Nonexistent", "Person");

        assertFalse(deleted);
    }

    @Test
    void findByAddress_ShouldReturnPersonsAtGivenAddress() {
        Person person1 = new Person("John", "Doe", "123 Main St", "City", "12345", "123-456-7890", "john.doe@email.com");
        Person person2 = new Person("Jane", "Smith", "123 Main St", "City", "67890", "987-654-3210", "jane.smith@email.com");
        personRepository.addOrUpdatePerson(person1);
        personRepository.addOrUpdatePerson(person2);

        List<Person> personsAtAddress = personRepository.findByAddress("123 Main St");

        assertEquals(2, personsAtAddress.size());
        assertTrue(personsAtAddress.contains(person1));
        assertTrue(personsAtAddress.contains(person2));
    }

    @Test
    void testLoadDataFileDoesNotExist() throws StreamReadException, DatabindException, IOException {
        File nonExistentFile = new File("nonexistent.json");
        personRepository = new PersonRepository(mockObjectMapper, "source.json", nonExistentFile.getAbsolutePath());

        personRepository.reloadData();

        verify(mockObjectMapper, never()).readValue(any(File.class), any(TypeReference.class));
        assertTrue(personRepository.getAllPersons().isEmpty());
    }

    @Test
    void testLoadData_FileExistsAndValid() throws IOException {
        String jsonContent = "{\"persons\":[{\"firstName\":\"John\",\"lastName\":\"Doe\",\"address\":\"123 Main St\",\"city\":\"City\",\"zip\":\"12345\",\"phone\":\"123-456-7890\",\"email\":\"john.doe@email.com\"}]}";
        Files.writeString(tempFile.toPath(), jsonContent);

        when(mockObjectMapper.readValue(any(File.class), any(TypeReference.class)))
                .thenReturn(Map.of("persons", List.of(new Person("John", "Doe", "123 Main St", "City", "12345", "123-456-7890", "john.doe@email.com"))));

        personRepository.reloadData();
        List<Person> persons = personRepository.getAllPersons();

        assertEquals(1, persons.size());
        assertEquals("John", persons.get(0).getFirstName());
    }

    @Test
    void testLoadData_InvalidJson_ShouldLogError() throws IOException {
        Files.writeString(tempFile.toPath(), "Invalid JSON content");

        when(mockObjectMapper.readValue(any(File.class), any(TypeReference.class))).thenThrow(IOException.class);

        personRepository.reloadData();

        assertTrue(personRepository.getAllPersons().isEmpty(), "Person list should be empty if JSON is invalid");
    }

}
