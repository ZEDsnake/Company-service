package com.zed.user_service.service;



import com.zed.user_service.dto.CompanyInfoDTO;
import com.zed.user_service.feign.CompanyClient;
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

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "eureka.client.enabled=false",
                "spring.cloud.config.enabled=false"
        })
@Testcontainers
@Sql(scripts = {"/schema.sql", "/cleanup.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class UserServiceIntegrationTest {

    @LocalServerPort
    private int port;

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @MockBean
    private CompanyClient companyClient;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost:" + port;

        CompanyInfoDTO companyInfo = new CompanyInfoDTO(1L, "Test Company");
        when(companyClient.getCompanyById(1L)).thenReturn(companyInfo);
    }

    @Test
    void testCreateAndGetUser() {
        String createUserJson = "{\"firstName\": \"Alice\", \"lastName\": \"Smith\", \"phoneNumber\": \"+79998887766\", \"companyId\": 1}";

        given()
                .contentType("application/json")
                .body(createUserJson)
                .when()
                .post("/users")
                .then()
                .statusCode(201)
                .body("firstName", equalTo("Alice"))
                .body("lastName", equalTo("Smith"))
                .body("phoneNumber", equalTo("+79998887766"));

        given()
                .when()
                .get("/users/1")
                .then()
                .statusCode(200)
                .body("firstName", equalTo("Alice"))
                .body("lastName", equalTo("Smith"));
    }

    @Test
    void testUpdateUser() {
        String createUserJson = "{\"firstName\": \"Alice\", \"lastName\": \"Smith\", \"phoneNumber\": \"+79998887766\", \"companyId\": 1}";
        given().contentType("application/json").body(createUserJson).post("/users");

        String updateUserJson = "{\"firstName\": \"Bob\", \"lastName\": \"Brown\", \"phoneNumber\": \"+79991112233\", \"companyId\": 1}";

        given()
                .contentType("application/json")
                .body(updateUserJson)
                .when()
                .put("/users/1")
                .then()
                .statusCode(200)
                .body("firstName", equalTo("Bob"))
                .body("lastName", equalTo("Brown"))
                .body("phoneNumber", equalTo("+79991112233"));
    }

    @Test
    void testDeleteUser() {
        String createUserJson = "{\"firstName\": \"Alice\", \"lastName\": \"Smith\", \"phoneNumber\": \"+79998887766\", \"companyId\": 1}";
        given().contentType("application/json").body(createUserJson).post("/users");

        given()
                .when()
                .delete("/users/1")
                .then()
                .statusCode(204);

        given()
                .when()
                .get("/users/1")
                .then()
                .statusCode(404);
    }


    @Test
    void testGetUserInfoById() {
        String createUserJson = "{\"firstName\": \"Alice\", \"lastName\": \"Smith\", \"phoneNumber\": \"+79998887766\", \"companyId\": 1}";
        given().contentType("application/json").body(createUserJson).post("/users");

        given()
                .when()
                .get("/users/1/info")
                .then()
                .statusCode(200)
                .body("firstName", equalTo("Alice"))
                .body("company.name", equalTo("Test Company"));
    }

    @Test
    void testGetAllUsers() {
        String createUserJson = "{\"firstName\": \"Alice\", \"lastName\": \"Smith\", \"phoneNumber\": \"+79998887766\", \"companyId\": 1}";
        given().contentType("application/json").body(createUserJson).post("/users");

        given()
                .when()
                .get("/users?page=0&size=10")
                .then()
                .statusCode(200)
                .body("size()", is(1))
                .body("[0].firstName", equalTo("Alice"));
    }
}
