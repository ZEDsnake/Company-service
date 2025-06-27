package com.zed.user_service.service;

import com.zed.user_service.dto.CompanyInfoDto;
import com.zed.user_service.feign.CompanyClient;
import feign.FeignException;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "eureka.client.enabled=false",
                "spring.cloud.config.enabled=false",
                "spring.cloud.openfeign.enabled=false"
        })
@Testcontainers
@Sql(scripts = {"/schema.sql", "/cleanup.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class UserServiceIntegrationTest {

    @LocalServerPort
    private int port;

    @Container
    @ServiceConnection
    public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @MockBean
    private CompanyClient companyClient;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    private final String PHONE = "+79998887766";
    private final String PHONE2 = "+79991112233";
    private final Long COMPANY_ID = 1L;
    private final Long NEW_COMPANY_ID = 2L;
    private final Long INVALID_ID = 999L;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost:" + port;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        CompanyInfoDto company = new CompanyInfoDto(COMPANY_ID, "Test Company", new BigDecimal("1000.00"));
        CompanyInfoDto newCompany = new CompanyInfoDto(NEW_COMPANY_ID, "New Company", new BigDecimal("2000.00"));

        when(companyClient.getCompanyById(COMPANY_ID)).thenReturn(company);
        when(companyClient.getCompanyById(NEW_COMPANY_ID)).thenReturn(newCompany);
        when(companyClient.getCompanyById(INVALID_ID)).thenThrow(FeignException.NotFound.class);
        doNothing().when(companyClient).addEmployee(anyLong(), anyLong());
        doNothing().when(companyClient).removeEmployee(anyLong(), anyLong());
    }

    @Test
    void createUser_ShouldReturnUserResponse_WhenValid() {
        String json = """
                {
                    "firstName": "Alice",
                    "lastName": "Smith",
                    "phoneNumber": "%s",
                    "companyId": %d
                }
                """.formatted(PHONE, COMPANY_ID);

        given()
                .contentType(ContentType.JSON)
                .body(json)
                .when()
                .post("/users")
                .then()
                .statusCode(201)
                .body("id", equalTo(1))
                .body("firstName", equalTo("Alice"))
                .body("lastName", equalTo("Smith"))
                .body("phoneNumber", equalTo(PHONE))
                .body("company.id", equalTo(COMPANY_ID.intValue()))
                .body("company.name", equalTo("Test Company"));

        verify(companyClient, times(2)).getCompanyById(COMPANY_ID);
        verify(companyClient).addEmployee(COMPANY_ID, 1L);
    }

    @Test
    void createUser_ShouldReturnConflict_WhenPhoneExists() {
        String json = """
                {
                    "firstName": "Alice",
                    "lastName": "Smith",
                    "phoneNumber": "%s",
                    "companyId": %d
                }
                """.formatted(PHONE, COMPANY_ID);

        given().contentType(ContentType.JSON).body(json).post("/users");

        given()
                .contentType(ContentType.JSON)
                .body(json)
                .when()
                .post("/users")
                .then()
                .statusCode(409)
                .body("message", containsString("Phone number already exists"));
    }

    @Test
    void createUser_ShouldReturnNotFound_WhenCompanyNotFound() {
        String json = """
                {
                    "firstName": "Alice",
                    "lastName": "Smith",
                    "phoneNumber": "%s",
                    "companyId": %d
                }
                """.formatted(PHONE, INVALID_ID);

        given()
                .contentType(ContentType.JSON)
                .body(json)
                .when()
                .post("/users")
                .then()
                .statusCode(404)
                .body("message", containsString("Company not found"));
    }

    @Test
    void createUser_FailValidation_NullCompanyId() {
        String json = """
                {
                    "firstName": "Alice",
                    "lastName": "Smith",
                    "phoneNumber": "%s",
                    "companyId": null
                }
                """.formatted(PHONE);

        given()
                .contentType(ContentType.JSON)
                .body(json)
                .when()
                .post("/users")
                .then()
                .statusCode(400)
                .body("companyId", equalTo("Company ID is required"));
    }

    @Test
    void updateUser_ShouldReturnUpdatedUser_WhenValid() {
        String createJson = """
                {
                    "firstName": "Alice",
                    "lastName": "Smith",
                    "phoneNumber": "%s",
                    "companyId": %d
                }
                """.formatted(PHONE, COMPANY_ID);
        given().contentType(ContentType.JSON).body(createJson).post("/users");

        String updateJson = """
                {
                    "firstName": "Bob",
                    "lastName": "Brown",
                    "phoneNumber": "%s",
                    "companyId": %d
                }
                """.formatted(PHONE2, COMPANY_ID);

        given()
                .contentType(ContentType.JSON)
                .body(updateJson)
                .when()
                .put("/users/1")
                .then()
                .statusCode(200)
                .body("id", equalTo(1))
                .body("firstName", equalTo("Bob"))
                .body("lastName", equalTo("Brown"))
                .body("phoneNumber", equalTo(PHONE2))
                .body("company.id", equalTo(COMPANY_ID.intValue()))
                .body("company.name", equalTo("Test Company"));

        verify(companyClient, times(4)).getCompanyById(COMPANY_ID);
    }

    @Test
    void updateUser_ShouldReturnNotFound_WhenUserNotFound() {
        String updateJson = """
                {
                    "firstName": "Bob",
                    "lastName": "Brown",
                    "phoneNumber": "%s",
                    "companyId": %d
                }
                """.formatted(PHONE, COMPANY_ID);

        given()
                .contentType(ContentType.JSON)
                .body(updateJson)
                .when()
                .put("/users/" + INVALID_ID)
                .then()
                .statusCode(404)
                .body("message", containsString("User not found"));
    }

    @Test
    void updateUser_ShouldReturnConflict_WhenPhoneExists() {
        String createJson1 = """
                {
                    "firstName": "Alice",
                    "lastName": "Smith",
                    "phoneNumber": "%s",
                    "companyId": %d
                }
                """.formatted(PHONE, COMPANY_ID);
        String createJson2 = """
                {
                    "firstName": "Bob",
                    "lastName": "Brown",
                    "phoneNumber": "%s",
                    "companyId": %d
                }
                """.formatted(PHONE2, COMPANY_ID);
        given().contentType(ContentType.JSON).body(createJson1).post("/users");
        given().contentType(ContentType.JSON).body(createJson2).post("/users");

        String updateJson = """
                {
                    "firstName": "Bob",
                    "lastName": "Brown",
                    "phoneNumber": "%s",
                    "companyId": %d
                }
                """.formatted(PHONE, COMPANY_ID);

        given()
                .contentType(ContentType.JSON)
                .body(updateJson)
                .when()
                .put("/users/2")
                .then()
                .statusCode(409)
                .body("message", containsString("Phone number already exists"));
    }

    @Test
    void updateUser_ShouldReturnNotFound_WhenCompanyNotFound() {
        String createJson = """
                {
                    "firstName": "Alice",
                    "lastName": "Smith",
                    "phoneNumber": "%s",
                    "companyId": %d
                }
                """.formatted(PHONE, COMPANY_ID);
        given().contentType(ContentType.JSON).body(createJson).post("/users");

        String updateJson = """
                {
                    "firstName": "Bob",
                    "lastName": "Brown",
                    "phoneNumber": "%s",
                    "companyId": %d
                }
                """.formatted(PHONE, INVALID_ID);

        given()
                .contentType(ContentType.JSON)
                .body(updateJson)
                .when()
                .put("/users/1")
                .then()
                .statusCode(404)
                .body("message", containsString("Company not found"));
    }

    @Test
    void updateUser_ShouldSyncCompanyChange_WhenCompanyIdChanged() {
        String createJson = """
                {
                    "firstName": "Alice",
                    "lastName": "Smith",
                    "phoneNumber": "%s",
                    "companyId": %d
                }
                """.formatted(PHONE, COMPANY_ID);
        given().contentType(ContentType.JSON).body(createJson).post("/users");

        String updateJson = """
                {
                    "firstName": "Bob",
                    "lastName": "Brown",
                    "phoneNumber": "%s",
                    "companyId": %d
                }
                """.formatted(PHONE2, NEW_COMPANY_ID);

        given()
                .contentType(ContentType.JSON)
                .body(updateJson)
                .when()
                .put("/users/1")
                .then()
                .statusCode(200)
                .body("id", equalTo(1))
                .body("firstName", equalTo("Bob"))
                .body("lastName", equalTo("Brown"))
                .body("phoneNumber", equalTo(PHONE2))
                .body("company.id", equalTo(NEW_COMPANY_ID.intValue()))
                .body("company.name", equalTo("New Company"));

        verify(companyClient, times(2)).getCompanyById(NEW_COMPANY_ID);
        verify(companyClient).removeEmployee(COMPANY_ID, 1L);
        verify(companyClient).addEmployee(NEW_COMPANY_ID, 1L);
    }

    @Test
    void patchUser_ShouldReturnPatchedUser_WhenValid() {
        String createJson = """
                {
                    "firstName": "Alice",
                    "lastName": "Smith",
                    "phoneNumber": "%s",
                    "companyId": %d
                }
                """.formatted(PHONE, COMPANY_ID);
        given().contentType(ContentType.JSON).body(createJson).post("/users");

        String patchJson = """
                {
                    "firstName": "Bob"
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(patchJson)
                .when()
                .patch("/users/1")
                .then()
                .statusCode(200)
                .body("id", equalTo(1))
                .body("firstName", equalTo("Bob"))
                .body("lastName", equalTo("Smith"))
                .body("phoneNumber", equalTo(PHONE))
                .body("company.id", equalTo(COMPANY_ID.intValue()))
                .body("company.name", equalTo("Test Company"));

        verify(companyClient, times(3)).getCompanyById(COMPANY_ID);
    }

    @Test
    void patchUser_ShouldReturnNotFound_WhenUserNotFound() {
        String patchJson = """
                {
                    "firstName": "Bob"
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(patchJson)
                .when()
                .patch("/users/" + INVALID_ID)
                .then()
                .statusCode(404)
                .body("message", containsString("User not found"));
    }

    @Test
    void patchUser_ShouldReturnConflict_WhenPhoneExists() {
        String createJson1 = """
                {
                    "firstName": "Alice",
                    "lastName": "Smith",
                    "phoneNumber": "%s",
                    "companyId": %d
                }
                """.formatted(PHONE, COMPANY_ID);
        String createJson2 = """
                {
                    "firstName": "Bob",
                    "lastName": "Brown",
                    "phoneNumber": "%s",
                    "companyId": %d
                }
                """.formatted(PHONE2, COMPANY_ID);
        given().contentType(ContentType.JSON).body(createJson1).post("/users");
        given().contentType(ContentType.JSON).body(createJson2).post("/users");

        String patchJson = """
                {
                    "phoneNumber": "%s"
                }
                """.formatted(PHONE);

        given()
                .contentType(ContentType.JSON)
                .body(patchJson)
                .when()
                .patch("/users/2")
                .then()
                .statusCode(409)
                .body("message", containsString("Phone number already exists"));
    }

    @Test
    void patchUser_ShouldReturnNotFound_WhenCompanyNotFound() {
        String createJson = """
                {
                    "firstName": "Alice",
                    "lastName": "Smith",
                    "phoneNumber": "%s",
                    "companyId": %d
                }
                """.formatted(PHONE, COMPANY_ID);
        given().contentType(ContentType.JSON).body(createJson).post("/users");

        String patchJson = """
                {
                    "companyId": %d
                }
                """.formatted(INVALID_ID);

        given()
                .contentType(ContentType.JSON)
                .body(patchJson)
                .when()
                .patch("/users/1")
                .then()
                .statusCode(404)
                .body("message", containsString("Company not found"));
    }

    @Test
    void patchUser_ShouldHandleNullFields() {
        String createJson = """
                {
                    "firstName": "Alice",
                    "lastName": "Smith",
                    "phoneNumber": "%s",
                    "companyId": %d
                }
                """.formatted(PHONE, COMPANY_ID);
        given().contentType(ContentType.JSON).body(createJson).post("/users");

        String patchJson = "{}";

        given()
                .contentType(ContentType.JSON)
                .body(patchJson)
                .when()
                .patch("/users/1")
                .then()
                .statusCode(200)
                .body("id", equalTo(1))
                .body("firstName", equalTo("Alice"))
                .body("lastName", equalTo("Smith"))
                .body("phoneNumber", equalTo(PHONE))
                .body("company.id", equalTo(COMPANY_ID.intValue()))
                .body("company.name", equalTo("Test Company"));

        verify(companyClient, times(3)).getCompanyById(COMPANY_ID); // Учитываем вызовы при создании и патче
    }

    @Test
    void patchUser_ShouldSyncCompanyChange_WhenCompanyIdChanged() {
        String createJson = """
                {
                    "firstName": "Alice",
                    "lastName": "Smith",
                    "phoneNumber": "%s",
                    "companyId": %d
                }
                """.formatted(PHONE, COMPANY_ID);
        given().contentType(ContentType.JSON).body(createJson).post("/users");

        String patchJson = """
                {
                    "companyId": %d
                }
                """.formatted(NEW_COMPANY_ID);

        given()
                .contentType(ContentType.JSON)
                .body(patchJson)
                .when()
                .patch("/users/1")
                .then()
                .statusCode(200)
                .body("id", equalTo(1))
                .body("firstName", equalTo("Alice"))
                .body("lastName", equalTo("Smith"))
                .body("phoneNumber", equalTo(PHONE))
                .body("company.id", equalTo(NEW_COMPANY_ID.intValue()))
                .body("company.name", equalTo("New Company"));

        verify(companyClient, times(2)).getCompanyById(NEW_COMPANY_ID);
        verify(companyClient).removeEmployee(COMPANY_ID, 1L);
        verify(companyClient).addEmployee(NEW_COMPANY_ID, 1L);
    }

    @Test
    void deleteUser_ShouldDelete_WhenUserExists() {
        String createJson = """
                {
                    "firstName": "Alice",
                    "lastName": "Smith",
                    "phoneNumber": "%s",
                    "companyId": %d
                }
                """.formatted(PHONE, COMPANY_ID);
        given().contentType(ContentType.JSON).body(createJson).post("/users");

        given()
                .when()
                .delete("/users/1")
                .then()
                .statusCode(204);

        given()
                .when()
                .get("/users/1")
                .then()
                .statusCode(404)
                .body("message", containsString("User not found"));

        verify(companyClient).removeEmployee(COMPANY_ID, 1L);
    }

    @Test
    void deleteUser_ShouldReturnNotFound_WhenUserNotFound() {
        given()
                .when()
                .delete("/users/" + INVALID_ID)
                .then()
                .statusCode(404)
                .body("message", containsString("User not found"));
    }

    @Test
    void deleteUser_FailValidation_NullCompanyId() {
        String createJson = """
                {
                    "firstName": "Alice",
                    "lastName": "Smith",
                    "phoneNumber": "%s",
                    "companyId": null
                }
                """.formatted(PHONE);

        given()
                .contentType(ContentType.JSON)
                .body(createJson)
                .when()
                .post("/users")
                .then()
                .statusCode(400)
                .body("companyId", equalTo("Company ID is required"));

        given()
                .when()
                .delete("/users/1")
                .then()
                .statusCode(404)
                .body("message", containsString("User not found"));
    }

    @Test
    void getUserById_ShouldReturnUser_WhenExists() {
        String createJson = """
                {
                    "firstName": "Alice",
                    "lastName": "Smith",
                    "phoneNumber": "%s",
                    "companyId": %d
                }
                """.formatted(PHONE, COMPANY_ID);
        given().contentType(ContentType.JSON).body(createJson).post("/users");

        given()
                .when()
                .get("/users/1")
                .then()
                .statusCode(200)
                .body("id", equalTo(1))
                .body("firstName", equalTo("Alice"))
                .body("lastName", equalTo("Smith"))
                .body("phoneNumber", equalTo(PHONE))
                .body("company.id", equalTo(COMPANY_ID.intValue()))
                .body("company.name", equalTo("Test Company"));

        verify(companyClient, times(3)).getCompanyById(COMPANY_ID);
    }

    @Test
    void getUserById_ShouldReturnNotFound_WhenUserNotFound() {
        given()
                .when()
                .get("/users/" + INVALID_ID)
                .then()
                .statusCode(404)
                .body("message", containsString("User not found"));
    }

    @Test
    void getUserById_FailValidation_NullCompanyId() {
        String createJson = """
                {
                    "firstName": "Alice",
                    "lastName": "Smith",
                    "phoneNumber": "%s",
                    "companyId": null
                }
                """.formatted(PHONE);

        given()
                .contentType(ContentType.JSON)
                .body(createJson)
                .when()
                .post("/users")
                .then()
                .statusCode(400)
                .body("companyId", equalTo("Company ID is required"));

        given()
                .when()
                .get("/users/1")
                .then()
                .statusCode(404)
                .body("message", containsString("User not found"));
    }

    @Test
    void getUserInfoById_ShouldReturnUserInfo_WhenExists() {
        String createJson = """
                {
                    "firstName": "Alice",
                    "lastName": "Smith",
                    "phoneNumber": "%s",
                    "companyId": %d
                }
                """.formatted(PHONE, COMPANY_ID);
        given().contentType(ContentType.JSON).body(createJson).post("/users");

        given()
                .contentType(ContentType.JSON)
                .body(List.of(1L))
                .when()
                .post("/users/by-ids")
                .then()
                .statusCode(200)
                .body("size()", equalTo(1))
                .body("[0].id", equalTo(1))
                .body("[0].firstName", equalTo("Alice"))
                .body("[0].lastName", equalTo("Smith"))
                .body("[0].phoneNumber", equalTo(PHONE));

        verify(companyClient, times(2)).getCompanyById(COMPANY_ID);
    }

    @Test
    void getUserInfoById_ShouldReturnNotFound_WhenUserNotFound() {
        given()
                .contentType(ContentType.JSON)
                .body(List.of(INVALID_ID))
                .when()
                .post("/users/by-ids")
                .then()
                .statusCode(200)
                .body("size()", equalTo(0));
    }

    @Test
    void getAllUsers_ShouldReturnPagedUsers_WhenValid() {
        String createJson = """
                {
                    "firstName": "Alice",
                    "lastName": "Smith",
                    "phoneNumber": "%s",
                    "companyId": %d
                }
                """.formatted(PHONE, COMPANY_ID);
        given().contentType(ContentType.JSON).body(createJson).post("/users");

        given()
                .when()
                .get("/users?page=0&size=10")
                .then()
                .statusCode(200)
                .body("users.size()", equalTo(1))
                .body("users[0].firstName", equalTo("Alice"))
                .body("users[0].company.id", equalTo(COMPANY_ID.intValue()))
                .body("page", equalTo(0))
                .body("totalPages", equalTo(1))
                .body("totalElements", equalTo(1));

        verify(companyClient, times(3)).getCompanyById(COMPANY_ID);
    }

    @Test
    void getAllUsers_ShouldReturnEmptyPage_WhenNoUsers() {
        given()
                .when()
                .get("/users?page=0&size=10")
                .then()
                .statusCode(200)
                .body("users.size()", equalTo(0))
                .body("page", equalTo(0))
                .body("totalPages", equalTo(0))
                .body("totalElements", equalTo(0));
    }

    @Test
    void getAllUsers_FailValidation_NullCompanyId() {
        String createJson = """
                {
                    "firstName": "Alice",
                    "lastName": "Smith",
                    "phoneNumber": "%s",
                    "companyId": null
                }
                """.formatted(PHONE);

        given()
                .contentType(ContentType.JSON)
                .body(createJson)
                .when()
                .post("/users")
                .then()
                .statusCode(400)
                .body("companyId", equalTo("Company ID is required"));

        given()
                .when()
                .get("/users?page=0&size=10")
                .then()
                .statusCode(200)
                .body("users.size()", equalTo(0))
                .body("page", equalTo(0))
                .body("totalPages", equalTo(0))
                .body("totalElements", equalTo(0));
    }

    @Test
    void getUsersByIds_ShouldReturnUsers_WhenIdsValid() {
        String createJson = """
                {
                    "firstName": "Alice",
                    "lastName": "Smith",
                    "phoneNumber": "%s",
                    "companyId": %d
                }
                """.formatted(PHONE, COMPANY_ID);
        given().contentType(ContentType.JSON).body(createJson).post("/users");

        given()
                .contentType(ContentType.JSON)
                .body(List.of(1L))
                .when()
                .post("/users/by-ids")
                .then()
                .statusCode(200)
                .body("size()", equalTo(1))
                .body("[0].id", equalTo(1))
                .body("[0].firstName", equalTo("Alice"))
                .body("[0].lastName", equalTo("Smith"))
                .body("[0].phoneNumber", equalTo(PHONE));
    }

    @Test
    void getUsersByIds_ShouldReturnEmptyList_WhenIdsEmpty() {
        given()
                .contentType(ContentType.JSON)
                .body(List.of())
                .when()
                .post("/users/by-ids")
                .then()
                .statusCode(200)
                .body("size()", equalTo(0));
    }

    @Test
    void getUsersByIds_ShouldReturnPartialList_WhenSomeIdsNotFound() {
        String createJson = """
                {
                    "firstName": "Alice",
                    "lastName": "Smith",
                    "phoneNumber": "%s",
                    "companyId": %d
                }
                """.formatted(PHONE, COMPANY_ID);
        given().contentType(ContentType.JSON).body(createJson).post("/users");

        given()
                .contentType(ContentType.JSON)
                .body(List.of(1L, INVALID_ID))
                .when()
                .post("/users/by-ids")
                .then()
                .statusCode(200)
                .body("size()", equalTo(1))
                .body("[0].id", equalTo(1))
                .body("[0].firstName", equalTo("Alice"));
    }

    @Test
    void deleteUsersByCompanyId_ShouldDelete_WhenUsersExist() {
        String createJson = """
                {
                    "firstName": "Alice",
                    "lastName": "Smith",
                    "phoneNumber": "%s",
                    "companyId": %d
                }
                """.formatted(PHONE, COMPANY_ID);
        given().contentType(ContentType.JSON).body(createJson).post("/users");

        given()
                .when()
                .delete("/users/by-company/" + COMPANY_ID)
                .then()
                .statusCode(204);

        given()
                .when()
                .get("/users/1")
                .then()
                .statusCode(404);
    }

    @Test
    void deleteUsersByCompanyId_ShouldReturnNoContent_WhenNoUsers() {
        given()
                .when()
                .delete("/users/by-company/" + COMPANY_ID)
                .then()
                .statusCode(204);
    }

    @Test
    void updateUserCompany_ShouldUpdate_WhenValid() {
        String createJson = """
                {
                    "firstName": "Alice",
                    "lastName": "Smith",
                    "phoneNumber": "%s",
                    "companyId": %d
                }
                """.formatted(PHONE, COMPANY_ID);
        given().contentType(ContentType.JSON).body(createJson).post("/users");

        given()
                .contentType(ContentType.JSON)
                .queryParam("companyId", NEW_COMPANY_ID)
                .when()
                .post("/users/1/company")
                .then()
                .statusCode(204);

        given()
                .when()
                .get("/users/1")
                .then()
                .statusCode(200)
                .body("company.id", equalTo(NEW_COMPANY_ID.intValue()))
                .body("company.name", equalTo("New Company"));
    }

    @Test
    void updateUserCompany_ShouldReturnNotFound_WhenUserNotFound() {
        given()
                .contentType(ContentType.JSON)
                .queryParam("companyId", NEW_COMPANY_ID)
                .when()
                .post("/users/" + INVALID_ID + "/company")
                .then()
                .statusCode(404)
                .body("message", containsString("User not found"));
    }

    @Test
    void updateUserCompany_ShouldHandleNullCompanyId() {
        String createJson = """
                {
                    "firstName": "Alice",
                    "lastName": "Smith",
                    "phoneNumber": "%s",
                    "companyId": %d
                }
                """.formatted(PHONE, COMPANY_ID);
        given().contentType(ContentType.JSON).body(createJson).post("/users");

        given()
                .contentType(ContentType.JSON)
                .when()
                .post("/users/1/company")
                .then()
                .statusCode(204);

        given()
                .when()
                .get("/users/1")
                .then()
                .statusCode(200)
                .body("company", nullValue());
    }
}