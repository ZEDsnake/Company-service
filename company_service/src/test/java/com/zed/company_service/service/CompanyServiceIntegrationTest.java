package com.zed.company_service.service;

import com.zed.company_service.dto.UserInfoDTO;
import com.zed.company_service.feign.UserClient;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "eureka.client.enabled=false",
                "spring.cloud.config.enabled=false"
        }
)
@Testcontainers
@Sql(scripts = {"/schema.sql", "/cleanup.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class CompanyServiceIntegrationTest {

    @LocalServerPort
    private int port;

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @MockBean
    private UserClient userClient;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost:" + port;

        UserInfoDTO user = new UserInfoDTO("John", "Doe", "+79123456789");
        when(userClient.getUsersByCompanyId(anyLong(), anyInt(), anyInt())).thenReturn(List.of(user));
        doNothing().when(userClient).deleteUsersByCompanyId(anyLong());
    }

    @Test
    void testCreateAndGetCompany() {
        String createCompanyJson = "{\"name\": \"Test Company\", \"budget\": 1000.00}";
        given()
                .contentType("application/json")
                .body(createCompanyJson)
                .when()
                .post("/companies")
                .then()
                .statusCode(201)
                .body("name", equalTo("Test Company"))
                .body("budget", equalTo(1000.00f));

        given()
                .when()
                .get("/companies/1")
                .then()
                .statusCode(200)
                .body("name", equalTo("Test Company"))
                .body("budget", equalTo(1000.00f));
    }

    @Test
    void testUpdateCompany() {
        String createCompanyJson = "{\"name\": \"Test Company\", \"budget\": 1000.00}";
        given()
                .contentType("application/json")
                .body(createCompanyJson)
                .post("/companies");

        String updateCompanyJson = "{\"name\": \"Updated Company\", \"budget\": 2000.00}";
        given()
                .contentType("application/json")
                .body(updateCompanyJson)
                .when()
                .put("/companies/1")
                .then()
                .statusCode(200)
                .body("name", equalTo("Updated Company"))
                .body("budget", equalTo(2000.00f));
    }

    @Test
    void testDeleteCompany() {
        String createCompanyJson = "{\"name\": \"Test Company\", \"budget\": 1000.00}";
        given()
                .contentType("application/json")
                .body(createCompanyJson)
                .post("/companies");

        given()
                .when()
                .delete("/companies/1")
                .then()
                .statusCode(204);

        given()
                .when()
                .get("/companies/1")
                .then()
                .statusCode(404);
    }

    @Test
    void testGetCompanyEmployees() {
        String createCompanyJson = "{\"name\": \"Test Company\", \"budget\": 1000.00}";
        given()
                .contentType("application/json")
                .body(createCompanyJson)
                .post("/companies");

        given()
                .when()
                .get("/companies/1/employees?page=0&size=10")
                .then()
                .statusCode(200)
                .body("[0].firstName", equalTo("John"))
                .body("[0].lastName", equalTo("Doe"))
                .body("[0].phoneNumber", equalTo("+79123456789"));
    }

    @Test
    void testGetCompanies() {
        String createCompanyJson = "{\"name\": \"Test Company\", \"budget\": 1000.00}";
        given()
                .contentType("application/json")
                .body(createCompanyJson)
                .post("/companies");

        given()
                .when()
                .get("/companies?page=0&size=10")
                .then()
                .statusCode(200)
                .body("[0].name", equalTo("Test Company"))
                .body("[0].budget", equalTo(1000.00f));
    }
}