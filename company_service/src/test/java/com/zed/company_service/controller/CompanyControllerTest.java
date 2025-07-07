package com.zed.company_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zed.company_service.dto.CompanyResponseDto;
import com.zed.company_service.dto.CreateCompanyDto;
import com.zed.company_service.dto.PagedCompanyResponseDto;
import com.zed.company_service.dto.PatchCompanyDto;
import com.zed.company_service.dto.UpdateCompanyDto;
import com.zed.company_service.dto.UserInfoDto;
import com.zed.company_service.exception.AlreadyExistsException;
import com.zed.company_service.exception.NotFoundException;
import com.zed.company_service.service.CompanyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CompanyController.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = {"spring.cloud.config.enabled=false"})
class CompanyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CompanyService companyService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final Long VALID_ID = 1L;

    private static CompanyResponseDto sampleCompanyResponse() {
        return new CompanyResponseDto(
                1L,
                "Valid Company",
                new BigDecimal("1000.00"),
                List.of(new UserInfoDto(1L, "John", "Doe", "+79123456789"))
        );
    }

    @Test
    void getCompanyById_ShouldReturnCompany_WhenExists() throws Exception {
        when(companyService.getCompanyById(VALID_ID)).thenReturn(sampleCompanyResponse());

        mockMvc.perform(get("/companies/{id}", VALID_ID))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(sampleCompanyResponse())));
    }

    @Test
    void getCompanyById_ShouldReturn404_WhenNotFound() throws Exception {
        when(companyService.getCompanyById(999L))
                .thenThrow(new NotFoundException("Company not found with id: 999"));

        mockMvc.perform(get("/companies/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Company not found with id: 999"));
    }

    @Test
    void getCompanyById_ShouldReturn400_WhenIdInvalid() throws Exception {
        mockMvc.perform(get("/companies/{id}", 0))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.id").value("Id must be positive and greater than 0"));
    }

    @Test
    void addCompany_ShouldReturn201_WhenValid() throws Exception {
        CreateCompanyDto request = new CreateCompanyDto("Valid Company", new BigDecimal("1000.00"), List.of(1L, 2L));

        when(companyService.createCompany(any())).thenReturn(sampleCompanyResponse());

        mockMvc.perform(post("/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().json(objectMapper.writeValueAsString(sampleCompanyResponse())));
    }

    @Test
    void addCompany_ShouldReturn400_WhenNameBlank() throws Exception {
        CreateCompanyDto request = new CreateCompanyDto("", new BigDecimal("1000.00"), List.of(1L));

        mockMvc.perform(post("/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value(anyOf(
                        is("Name is required"),
                        is("Name must be between 3 and 100 characters")
                )));
    }

    @Test
    void addCompany_ShouldReturn400_WhenNameTooShort() throws Exception {
        CreateCompanyDto request = new CreateCompanyDto("AB", new BigDecimal("1000.00"), List.of(1L));

        mockMvc.perform(post("/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("Name must be between 3 and 100 characters"));
    }

    @Test
    void addCompany_ShouldReturn400_WhenNameTooLong() throws Exception {
        String longName = "A".repeat(101);
        CreateCompanyDto request = new CreateCompanyDto(longName, new BigDecimal("1000.00"), List.of(1L));

        mockMvc.perform(post("/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("Name must be between 3 and 100 characters"));
    }

    @Test
    void addCompany_ShouldReturn400_WhenBudgetIsNull() throws Exception {
        CreateCompanyDto request = new CreateCompanyDto("Valid Name", null, List.of(1L));

        mockMvc.perform(post("/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.budget").value("Budget is required"));
    }

    @Test
    void addCompany_ShouldReturn400_WhenBudgetIsNegative() throws Exception {
        CreateCompanyDto request = new CreateCompanyDto("Valid Name", new BigDecimal("-1.00"), List.of(1L));

        mockMvc.perform(post("/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.budget").value("Budget must be positive or zero"));
    }

    @Test
    void addCompany_ShouldReturn409_WhenNameAlreadyExists() throws Exception {
        CreateCompanyDto request = new CreateCompanyDto("Duplicate Company", new BigDecimal("1000.00"), List.of(1L));

        when(companyService.createCompany(any())).thenThrow(new AlreadyExistsException("Company name already exists"));

        mockMvc.perform(post("/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Company name already exists"));
    }

    @Test
    void updateCompany_ShouldReturn200_WhenValid() throws Exception {
        UpdateCompanyDto request = new UpdateCompanyDto("Updated Name", new BigDecimal("1000.00"), List.of(1L));

        when(companyService.updateCompany(eq(VALID_ID), any())).thenReturn(sampleCompanyResponse());

        mockMvc.perform(put("/companies/{id}", VALID_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(sampleCompanyResponse())));
    }

    @Test
    void updateCompany_ShouldReturn400_WhenNameBlank() throws Exception {
        UpdateCompanyDto request = new UpdateCompanyDto("", new BigDecimal("1000.00"), List.of(1L));

        mockMvc.perform(put("/companies/{id}", VALID_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value(anyOf(
                        is("Name is required"),
                        is("Name must be between 3 and 100 characters")
                )));
    }

    @Test
    void updateCompany_ShouldReturn400_WhenNameTooShort() throws Exception {
        UpdateCompanyDto request = new UpdateCompanyDto("AB", new BigDecimal("1000.00"), List.of(1L));

        mockMvc.perform(put("/companies/{id}", VALID_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("Name must be between 3 and 100 characters"));
    }

    @Test
    void updateCompany_ShouldReturn400_WhenNameTooLong() throws Exception {
        String longName = "A".repeat(101);
        UpdateCompanyDto request = new UpdateCompanyDto(longName, new BigDecimal("1000.00"), List.of(1L));

        mockMvc.perform(put("/companies/{id}", VALID_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("Name must be between 3 and 100 characters"));
    }

    @Test
    void updateCompany_ShouldReturn400_WhenBudgetIsNull() throws Exception {
        UpdateCompanyDto request = new UpdateCompanyDto("Valid Name", null, List.of(1L));

        mockMvc.perform(put("/companies/{id}", VALID_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.budget").value("Budget is required"));
    }

    @Test
    void updateCompany_ShouldReturn400_WhenBudgetIsNegative() throws Exception {
        UpdateCompanyDto request = new UpdateCompanyDto("Valid Name", new BigDecimal("-1.00"), List.of(1L));

        mockMvc.perform(put("/companies/{id}", VALID_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.budget").value("Budget must be positive or zero"));
    }

    @Test
    void updateCompany_ShouldReturn400_WhenInvalidId() throws Exception {
        UpdateCompanyDto request = new UpdateCompanyDto("Valid Name", new BigDecimal("1000.00"), List.of(1L));

        mockMvc.perform(put("/companies/{id}", 0L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.id").value("Id must be positive and greater than 0"));
    }

    @Test
    void updateCompany_ShouldReturn404_WhenNotFound() throws Exception {
        UpdateCompanyDto request = new UpdateCompanyDto("Valid Name", new BigDecimal("1000.00"), List.of(1L));

        when(companyService.updateCompany(eq(999L), any())).thenThrow(new NotFoundException("Company not found"));

        mockMvc.perform(put("/companies/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Company not found"));
    }

    @Test
    void patchCompany_ShouldReturn200_WhenValid() throws Exception {
        PatchCompanyDto request = new PatchCompanyDto("Updated Name", new BigDecimal("1000.00"), List.of(1L));

        when(companyService.patchCompany(eq(VALID_ID), any())).thenReturn(sampleCompanyResponse());

        mockMvc.perform(patch("/companies/{id}", VALID_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(sampleCompanyResponse())));
    }

    @Test
    void patchCompany_ShouldReturn400_WhenNameTooShort() throws Exception {
        PatchCompanyDto request = new PatchCompanyDto("AB", null, null);

        mockMvc.perform(patch("/companies/{id}", VALID_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("Name must be between 3 and 100 characters"));
    }

    @Test
    void patchCompany_ShouldReturn400_WhenNameTooLong() throws Exception {
        String longName = "A".repeat(101);
        PatchCompanyDto request = new PatchCompanyDto(longName, null, null);

        mockMvc.perform(patch("/companies/{id}", VALID_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("Name must be between 3 and 100 characters"));
    }

    @Test
    void patchCompany_ShouldReturn400_WhenBudgetIsNegative() throws Exception {
        PatchCompanyDto request = new PatchCompanyDto(null, new BigDecimal("-1.00"), null);

        mockMvc.perform(patch("/companies/{id}", VALID_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.budget").value("Budget must be positive or zero"));
    }

    @Test
    void patchCompany_ShouldReturn400_WhenInvalidId() throws Exception {
        PatchCompanyDto request = new PatchCompanyDto("Valid Name", new BigDecimal("1000.00"), List.of(1L));

        mockMvc.perform(patch("/companies/{id}", 0L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.id").value("Id must be positive and greater than 0"));
    }

    @Test
    void patchCompany_ShouldReturn404_WhenNotFound() throws Exception {
        PatchCompanyDto request = new PatchCompanyDto("Valid Name", new BigDecimal("1000.00"), List.of(1L));

        when(companyService.patchCompany(eq(999L), any())).thenThrow(new NotFoundException("Company not found"));

        mockMvc.perform(patch("/companies/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Company not found"));
    }

    @Test
    void deleteCompany_ShouldReturn204_WhenValid() throws Exception {
        doNothing().when(companyService).deleteCompany(VALID_ID);

        mockMvc.perform(delete("/companies/{id}", VALID_ID))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteCompany_ShouldReturn400_WhenInvalidId() throws Exception {
        mockMvc.perform(delete("/companies/{id}", 0L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.id").value("Id must be positive and greater than 0"));
    }

    @Test
    void deleteCompany_ShouldReturn404_WhenNotFound() throws Exception {
        doThrow(new NotFoundException("Company not found with id: 999"))
                .when(companyService).deleteCompany(999L);

        mockMvc.perform(delete("/companies/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Company not found with id: 999"));
    }

    @Test
    void getAllCompanies_ShouldReturnList_WhenValid() throws Exception {
        PagedCompanyResponseDto response = new PagedCompanyResponseDto(List.of(sampleCompanyResponse()), 0, 1, 1);

        when(companyService.getAllCompanies(0, 10)).thenReturn(response);

        mockMvc.perform(get("/companies").param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    void getAllCompanies_ShouldReturn400_WhenPageIsNegative() throws Exception {
        mockMvc.perform(get("/companies").param("page", "-1").param("size", "10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.page").value("Page must be positive and greater than 0"));
    }

    @Test
    void getAllCompanies_ShouldReturn400_WhenSizeIsLessThanOne() throws Exception {
        mockMvc.perform(get("/companies").param("page", "0").param("size", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.size").value("Size must be positive and greater than 1"));
    }

    @Test
    void addEmployee_ShouldReturn204_WhenValid() throws Exception {
        doNothing().when(companyService).addEmployeeToCompany(1L, 2L);

        mockMvc.perform(post("/companies/{companyId}/employees/add", 1L).param("employeeId", "2"))
                .andExpect(status().isNoContent());
    }

    @Test
    void addEmployee_ShouldReturn400_WhenInvalidCompanyId() throws Exception {
        mockMvc.perform(post("/companies/{companyId}/employees/add", 0L).param("employeeId", "2"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.companyId").value("Id must be positive and greater than 0"));
    }

    @Test
    void addEmployee_ShouldReturn400_WhenInvalidEmployeeId() throws Exception {
        mockMvc.perform(post("/companies/{companyId}/employees/add", 1L).param("employeeId", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.employeeId").value("Id must be positive and greater than 0"));
    }

    @Test
    void addEmployee_ShouldReturn404_WhenNotFound() throws Exception {
        doThrow(new NotFoundException("Company or employee not found"))
                .when(companyService).addEmployeeToCompany(1L, 2L);

        mockMvc.perform(post("/companies/{companyId}/employees/add", 1L).param("employeeId", "2"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Company or employee not found"));
    }

    @Test
    void removeEmployee_ShouldReturn204_WhenValid() throws Exception {
        doNothing().when(companyService).removeEmployeeFromCompany(1L, 2L);

        mockMvc.perform(post("/companies/{companyId}/employees/remove", 1L).param("employeeId", "2"))
                .andExpect(status().isNoContent());
    }

    @Test
    void removeEmployee_ShouldReturn400_WhenInvalidCompanyId() throws Exception {
        mockMvc.perform(post("/companies/{companyId}/employees/remove", 0L).param("employeeId", "2"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.companyId").value("Id must be positive and greater than 0"));
    }

    @Test
    void removeEmployee_ShouldReturn400_WhenInvalidEmployeeId() throws Exception {
        mockMvc.perform(post("/companies/{companyId}/employees/remove", 1L).param("employeeId", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.employeeId").value("Id must be positive and greater than 0"));
    }

    @Test
    void removeEmployee_ShouldReturn404_WhenNotFound() throws Exception {
        doThrow(new NotFoundException("Company or employee not found"))
                .when(companyService).removeEmployeeFromCompany(1L, 2L);

        mockMvc.perform(post("/companies/{companyId}/employees/remove", 1L).param("employeeId", "2"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Company or employee not found"));
    }
}