package SpringTestContainer.demo.controller;

import SpringTestContainer.demo.entity.Customer;
import SpringTestContainer.demo.repository.CustomerRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;


// We have annotated the test class with the @SpringBootTest annotation together with the webEnvironment config,
// so that the test will run by starting the entire application on a random available port.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CustomerControllerTest {

    @LocalServerPort  // annotation is used in integration tests for Spring Boot applications to inject the port number that the embedded web server is running on into the test class.
    private Integer port;



    // We have created an instance of PostgreSQLContainer using the postgres:15-alpine Docker image.
    // The Postgres container is started using JUnit 5 @BeforeAll callback method which gets executed before running any test method within a test instance.
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            "postgres:15-alpine"  //postgres:15-alpine refers to PostgreSQL version 15 running on the Alpine Linux distribution, and it's not the default configuration but a specific choice made for your integration tests. You can choose different versions and distributions based on your requirements.
    );

    @BeforeAll // annotation is used in JUnit 5 to indicate that the annotated method should be executed once before all the tests in the test class are run.
    static void beforeAll() {
        postgres.start();
        // @BeforeAll annotated method beforeAll() is used to start the PostgreSQL container once before running any tests in the test class, ensuring that the database is ready for use during the test execution
    }

    @AfterAll //@AfterAll annotated method afterAll() is used to stop the PostgreSQL container once after running all tests in the test class
    static void afterAll() {
        postgres.stop();
    }

//    @DynamicPropertySource //dynamically configure the datasource properties for your Spring application to connect to the PostgreSQL database running in a Testcontainers container.
//    static void configureProperties(DynamicPropertyRegistry registry) {
//        registry.add("spring.datasource.url", postgres::getJdbcUrl);  //This line adds a dynamic property source for the spring.datasource.url property. The value of this property is obtained by calling the getJdbcUrl() method on the postgres object, which returns the JDBC URL of the PostgreSQL database container.
//        registry.add("spring.datasource.username", postgres::getUsername); //This line adds a dynamic property source for the spring.datasource.username property. The value of this property is obtained by calling the getUsername() method on the postgres object, which returns the username used to authenticate with the PostgreSQL database container.
//        registry.add("spring.datasource.password", postgres::getPassword); //This line adds a dynamic property source for the spring.datasource.password property. The value of this property is obtained by calling the getPassword() method on the postgres object, which returns the password used to authenticate with the PostgreSQL database container.
//    }

    @Autowired
    CustomerRepository customerRepository;

    @BeforeEach // annotation is used in JUnit 5 to indicate that the annotated method should be executed before each test method in the test class
    void setUp() // setUp() method is annotated with @BeforeEach, indicating that it should be executed before each test method.
     {

        // Rest Assured is a Java library for testing RESTful APIs.
             RestAssured.baseURI = "http://localhost:" + port; // It specifies the base URL that RestAssured will use for making HTTP requests
        customerRepository.deleteAll(); //This line deletes all customers from the repository before each test. This ensures that each test starts with a clean slate, without any existing customers in the database. It helps isolate each test from one another and ensures that tests are not dependent on each other's state.
    }

    @Test //@Test annotation is used in JUnit to mark a method as a test method.
    void shouldGetAllCustomers() {
        // Given a list of customers to save to the database
        List<Customer> customers = List.of(
                new Customer(null, "Daya", "daya@yopmail.com"),
                new Customer(null, "Sanvi", "sanvi@yopmail.com")
        );
        // When: saving the customers to the repository
        customerRepository.saveAll(customers);

        // Set the content type of the request to JSON
        given()
                .contentType(ContentType.JSON)
                // Perform an HTTP GET request to the "/customers" endpoint
                .when()
                .get("/customers")
                // Assert that the response status code is 200 (OK)
                .then()
                .statusCode(200)
                // Assert that the response body contains a JSON array with size 2
                .body(".", hasSize(2));
    }

    @Test
    void shouldFailToGetAllCustomers() {
        // Given: a list of customers to save to the database
        List<Customer> customers = List.of(
                new Customer(null, "Daya123", "daya123@yopmail.com"),
                new Customer(null, "Sanvi123", "sanvi123@yopmail.com")
        );
        // When: saving the customers to the repository
        customerRepository.saveAll(customers);

        // Then: making a GET request to retrieve all customers
        given()
                // Setting the content type of the request to JSON
                .contentType(ContentType.JSON)
                .when()
                // Performing an HTTP GET request to the "/customers" endpoint
                .get("/customers")
                .then()
                // Asserting that the response status code is 404 (Not Found)
                .statusCode(404);
    }

    @Test
    void shouldGetCustomerById() {
        // Save a customer to the database
        Customer savedCustomer = customerRepository.save(new Customer(null, "Daya", "daya@yopmail.com"));

        // Make a GET request to /customers/{id} to retrieve the customer by their ID
        given()
                // Set the port to use in the request
                .port(port)
                // Set the content type of the request to JSON
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                // Perform an HTTP GET request to the "/customers/{id}" endpoint with the ID of the saved customer
                .get("/customers/{id}", savedCustomer.getId())
                .then()
                // Assert that the response status code is 200 (OK)
                .statusCode(HttpStatus.OK.value())
                // Assert that the content type of the response is JSON
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                // Assert that the response body contains the correct customer ID
                .body("id", equalTo(savedCustomer.getId().intValue()))
                // Assert that the response body contains the correct customer name
                .body("name", equalTo(savedCustomer.getName()))
                // Assert that the response body contains the correct customer email
                .body("email", equalTo(savedCustomer.getEmail()));
    }

    @Test
    void shouldReturnNotFoundForNonExistentCustomer() {
        // Make a GET request to /customers/{id} with a non-existent ID (e.g., 1000L)
        given()
                // Set the port to use in the request
                .port(port)
                // Set the content type of the request to JSON
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                // Perform an HTTP GET request to the "/customers/{id}" endpoint with a non-existent customer ID (e.g., 1000L)
                .get("/customers/{id}", 1000L) // Assuming customer with ID 1000 doesn't exist
                .then()
                // Assert that the response status code is 404 (Not Found)
                .statusCode(HttpStatus.NOT_FOUND.value());
    }


    @Test
    void shouldAddCustomer() {
        Customer customer = new Customer(null, "JOE GOLDBERG", "joe@example.com");

        given()
                .contentType(ContentType.JSON)
                .body(customer)
                .when()
                .post("/customers")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .contentType(ContentType.JSON)
                .body("name", equalTo(customer.getName()))
                .body("email", equalTo(customer.getEmail()));
    }


    @Test
    void shouldUpdateCustomer() {
        // Add a customer to the database
        Customer savedCustomer = customerRepository.save(new Customer(null, "Joe Goldberg", "joe@example.com"));

        // Create an updated customer object
        Customer updatedCustomer = new Customer(savedCustomer.getId(), "Joe Goldberg\"", "joe@example.com");

        // Send a PUT request to update the customer
        given()
                .contentType(ContentType.JSON)
                .body(updatedCustomer)
                .when()
                .put("/customers/{id}", savedCustomer.getId())
                .then()
                .statusCode(HttpStatus.OK.value())
                .contentType(ContentType.JSON)
                .body("id", equalTo(savedCustomer.getId().intValue())) // Check if the ID remains the same
                .body("name", equalTo(updatedCustomer.getName())) // Check if the name is updated
                .body("email", equalTo(updatedCustomer.getEmail())); // Check if the email is updated
    }

    @Test
    void shouldReturnNotFoundForNonExistentCustomerUpdate() {
        // Create an updated customer object for a non-existent customer
        Customer updatedCustomer = new Customer(1000L, "Joe Goldberg", "joe@example.com");

        // Send a PUT request to update the non-existent customer
        given()
                .contentType(ContentType.JSON)
                .body(updatedCustomer)
                .when()
                .put("/customers/{id}", updatedCustomer.getId())
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value()); // Expecting a 404 status code
    }


    @Test
    void shouldDeleteCustomer() {
        // Given a customer to be deleted
        Customer customer = new Customer(null, "John", "john@example.com");
        Long customerId = given()
                .contentType(ContentType.JSON)
                .body(customer)
                .post("/customers")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .jsonPath()
                .getLong("id");

        // When: sending a DELETE request to delete the customer
        given()
                .pathParam("id", customerId)
                .when()
                .delete("/customers/{id}")
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());

        // Then: verifying that the customer has been deleted
        given()
                .pathParam("id", customerId)
                .when()
                .get("/customers/{id}")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body(equalTo(nullValue()));
    }
}
