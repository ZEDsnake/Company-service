package com.zed.company_service.service;

import com.zed.company_service.dto.UserInfoDto;
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
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "eureka.client.enabled=false",
                "spring.cloud.config.enabled=false"
        })
@Testcontainers
@Sql(scripts = {"/schema.sql", "/cleanup.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class CompanyServiceIntegrationTest {

    @LocalServerPort
    private int port;

    @Container
    public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @MockBean
    private UserClient userClient;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost:" + port;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        UserInfoDto user = new UserInfoDto(1L, "John", "Doe", "+79123456789");

        when(userClient.getUsersByIds(anyList()))
                .thenReturn(List.of(user));

        doNothing().when(userClient).deleteUsersByCompanyId(anyLong());
        doNothing().when(userClient).updateUserCompany(anyLong(), any());
    }

    @Test
    void createCompany_Success() {
        String json = """
                {
                    "name": "Test Company",
                    "budget": 1000.00,
                    "employeeIds": [1]
                }
                """;

        given()
                .contentType("application/json")
                .body(json)
                .when()
                .post("/companies")
                .then()
                .statusCode(201)
                .body("name", equalTo("Test Company"))
                .body("budget", equalTo(1000.00f))
                .body("employees.size()", equalTo(1))
                .body("employees[0].firstName", equalTo("John"));
    }

    @Test
    void createCompany_FailValidation_EmptyName() {
        String json = """
                {
                    "name": "  ",
                    "budget": 1000.00,
                    "employeeIds": [1]
                }
                """;

        given()
                .contentType("application/json")
                .body(json)
                .when()
                .post("/companies")
                .then()
                .statusCode(400)
                .body("name", anyOf(equalTo("Name is required"), equalTo("Name must be between 3 and 100 characters")));
    }

    @Test
    void createCompany_FailValidation_NegativeBudget() {
        String json = """
                {
                    "name": "Valid Name",
                    "budget": -10,
                    "employeeIds": [1]
                }
                """;

        given()
                .contentType("application/json")
                .body(json)
                .when()
                .post("/companies")
                .then()
                .statusCode(400)
                .body("budget", equalTo("Budget must be positive or zero"));
    }

    @Test
    void createCompany_FailValidation_NullEmployeeIds() {
        String json = """
                {
                    "name": "Valid Name",
                    "budget": 1000.00,
                    "employeeIds": null
                }
                """;

        given()
                .contentType("application/json")
                .body(json)
                .when()
                .post("/companies")
                .then()
                .statusCode(400)
                .body("employeeIds", equalTo("Employee IDs cannot be null"));
    }

    @Test
    void createCompany_FailDuplicateName() {
        String json = """
                {
                    "name": "Duplicate",
                    "budget": 1000.00,
                    "employeeIds": [1]
                }
                """;

        given()
                .contentType("application/json")
                .body(json)
                .when()
                .post("/companies")
                .then()
                .statusCode(201);

        given()
                .contentType("application/json")
                .body(json)
                .when()
                .post("/companies")
                .then()
                .statusCode(409)
                .body("message", containsString("already exists"));
    }

    @Test
    void getCompanyById_Success() {
        createCompany_Success();

        given()
                .when()
                .get("/companies/1")
                .then()
                .statusCode(200)
                .body("name", equalTo("Test Company"));
    }

    @Test
    void getCompanyById_NotFound() {
        given()
                .when()
                .get("/companies/9999")
                .then()
                .statusCode(404)
                .body("message", containsString("not found"));
    }

    @Test
    void updateCompany_Success() {
        createCompany_Success();

        String updateJson = """
                {
                    "name": "Updated Company",
                    "budget": 2000.00,
                    "employeeIds": [1]
                }
                """;

        given()
                .contentType("application/json")
                .body(updateJson)
                .when()
                .put("/companies/1")
                .then()
                .statusCode(200)
                .body("name", equalTo("Updated Company"))
                .body("budget", equalTo(2000.00f));
    }

    @Test
    void updateCompany_FailValidation_InvalidName() {
        createCompany_Success();

        String updateJson = """
                {
                    "name": "ab",
                    "budget": 2000.00,
                    "employeeIds": [1]
                }
                """;

        given()
                .contentType("application/json")
                .body(updateJson)
                .when()
                .put("/companies/1")
                .then()
                .statusCode(400)
                .body("name", equalTo("Name must be between 3 and 100 characters"));
    }

    @Test
    void patchCompany_Success() {
        createCompany_Success();

        String patchJson = """
                {
                    "budget": 3000.00
                }
                """;

        given()
                .contentType("application/json")
                .body(patchJson)
                .when()
                .patch("/companies/1")
                .then()
                .statusCode(200)
                .body("budget", equalTo(3000.00f));
    }

    @Test
    void deleteCompany_Success() {
        createCompany_Success();

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
    void getCompaniesWithPagination_Success() {
        for (int i = 1; i <= 3; i++) {
            String json = String.format("""
                    {
                        "name": "Company %d",
                        "budget": 1000.00,
                        "employeeIds": [1]
                    }
                    """, i);

            given()
                    .contentType("application/json")
                    .body(json)
                    .when()
                    .post("/companies")
                    .then()
                    .statusCode(201);
        }

        given()
                .when()
                .get("/companies?page=0&size=2")
                .then()
                .statusCode(200)
                .body("companies.size()", equalTo(2));

        given()
                .when()
                .get("/companies?page=1&size=2")
                .then()
                .statusCode(200)
                .body("companies.size()", equalTo(1));
    }
}