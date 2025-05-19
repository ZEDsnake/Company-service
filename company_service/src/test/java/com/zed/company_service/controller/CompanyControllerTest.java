package com.zed.company_service.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import com.zed.company_service.dto.CompanyDTO;
import com.zed.company_service.dto.CreateCompanyDTO;
import com.zed.company_service.dto.UpdateCompanyDTO;
import com.zed.company_service.dto.UserInfoDTO;
import com.zed.company_service.exception.NotFoundException;
import com.zed.company_service.service.CompanyService;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;


@WebMvcTest(CompanyController.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.cloud.config.enabled=false"})

class CompanyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CompanyService companyService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getCompanyEmployees_ShouldReturnListOfUsers_WhenCompanyExists() throws Exception {
        long companyId = 1L;
        int page = 0;
        int size = 2;

        List<UserInfoDTO> users = List.of(
                new UserInfoDTO("John", "Doe", "+72345678984"),
                new UserInfoDTO("Jane", "Smith", "+78765432169")
        );

        when(companyService.getCompanyEmployees(companyId, page, size)).thenReturn(users);

        mockMvc.perform(get("/companies/{companyId}/employees", companyId)
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(users)));
    }

    @Test
    void getCompanyEmployees_ShouldReturn404_WhenCompanyNotFound() throws Exception {
        long companyId = 999L;
        int page = 0;
        int size = 10;

        when(companyService.getCompanyEmployees(companyId, page, size))
                .thenThrow(new NotFoundException("Company not found"));

        mockMvc.perform(get("/companies/{companyId}/employees", companyId)
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCompanyEmployees_ShouldReturn400_WhenPageIsNegative() throws Exception {
        mockMvc.perform(get("/companies/{companyId}/employees", 1L)
                        .param("page", "-1")
                        .param("size", "10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Page must be greater than or equal to 0"));
    }

    @Test
    void getCompanyEmployees_ShouldReturn400_WhenSizeIsLessThanOne() throws Exception {
        mockMvc.perform(get("/companies/{companyId}/employees", 1L)
                        .param("page", "0")
                        .param("size", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Size must be greater than or equal to 1"));
    }

    @Test
    void addCompany_ShouldReturn201_WhenValidRequest() throws Exception {
        CreateCompanyDTO createDto = new CreateCompanyDTO("Valid Company", BigDecimal.valueOf(1000));
        CompanyDTO expectedDto = new CompanyDTO(1L, "Valid Company", BigDecimal.valueOf(1000));

        when(companyService.addCompany(createDto)).thenReturn(expectedDto);

        mockMvc.perform(post("/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(content().json(objectMapper.writeValueAsString(expectedDto)));
    }

    @Test
    void addCompany_ShouldReturn400_WhenNameIsBlank() throws Exception {
        CreateCompanyDTO dto = new CreateCompanyDTO("  ", BigDecimal.valueOf(500));

        mockMvc.perform(post("/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(
                        anyOf(
                                containsString("Name is required"),
                                containsString("Name must be between 3 and 100 characters")
                        )
                ));
    }

    @Test
    void addCompany_ShouldReturn400_WhenNameTooShort() throws Exception {
        CreateCompanyDTO dto = new CreateCompanyDTO("AB", BigDecimal.valueOf(500));

        mockMvc.perform(post("/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Name must be between 3 and 100 characters")));
    }

    @Test
    void addCompany_ShouldReturn400_WhenNameTooLong() throws Exception {
        String longName = "A".repeat(101);
        CreateCompanyDTO dto = new CreateCompanyDTO(longName, BigDecimal.valueOf(500));

        mockMvc.perform(post("/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Name must be between 3 and 100 characters")));
    }

    @Test
    void addCompany_ShouldReturn400_WhenBudgetIsNull() throws Exception {
        CreateCompanyDTO dto = new CreateCompanyDTO("Company", null);

        mockMvc.perform(post("/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Budget is required")));
    }

    @Test
    void addCompany_ShouldReturn400_WhenBudgetIsNegative() throws Exception {
        CreateCompanyDTO dto = new CreateCompanyDTO("Company", BigDecimal.valueOf(-100));

        mockMvc.perform(post("/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Budget must be positive or zero")));
    }

    @Test
    void shouldReturn200_WhenValidUpdate() throws Exception {
        long id = 1L;
        UpdateCompanyDTO dto = new UpdateCompanyDTO("Valid Name", new BigDecimal("1000.00"));
        CompanyDTO response = new CompanyDTO(id, dto.getName(), dto.getBudget());

        when(companyService.updateCompany(eq(id), any(UpdateCompanyDTO.class))).thenReturn(response);

        mockMvc.perform(put("/companies/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    void shouldReturn400_WhenIdIsLessThan1() throws Exception {
        UpdateCompanyDTO dto = new UpdateCompanyDTO("Test", new BigDecimal("100.00"));

        mockMvc.perform(put("/companies/{id}", 0)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("ID must be a positive number and not less than 1")));
    }

    @Test
    void shouldReturn400_WhenNameTooShort() throws Exception {
        UpdateCompanyDTO dto = new UpdateCompanyDTO("ab", new BigDecimal("100.00"));

        mockMvc.perform(put("/companies/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Name must be between 3 and 100 characters")));
    }

    @Test
    void shouldReturn400_WhenNameTooLong() throws Exception {
        String longName = "A".repeat(101);
        UpdateCompanyDTO dto = new UpdateCompanyDTO(longName, new BigDecimal("100.00"));

        mockMvc.perform(put("/companies/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Name must be between 3 and 100 characters")));
    }

    @Test
    void shouldReturn200_WhenNameIsNull() throws Exception {
        long id = 1L;
        UpdateCompanyDTO dto = new UpdateCompanyDTO(null, new BigDecimal("1000.00"));
        CompanyDTO response = new CompanyDTO(id, "Old Name", dto.getBudget());

        when(companyService.updateCompany(eq(id), any(UpdateCompanyDTO.class))).thenReturn(response);

        mockMvc.perform(put("/companies/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn400_WhenBudgetIsNegative() throws Exception {
        UpdateCompanyDTO dto = new UpdateCompanyDTO("Valid Name", new BigDecimal("-10.00"));

        mockMvc.perform(put("/companies/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Budget must be positive or zero")));
    }

    @Test
    void shouldReturn200_WhenBudgetIsNull() throws Exception {
        long id = 1L;
        UpdateCompanyDTO dto = new UpdateCompanyDTO("Valid Name", null);
        CompanyDTO response = new CompanyDTO(id, dto.getName(), null);

        when(companyService.updateCompany(eq(id), any(UpdateCompanyDTO.class))).thenReturn(response);

        mockMvc.perform(put("/companies/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn204_WhenCompanySuccessfullyDeleted() throws Exception {
        long id = 1L;

        doNothing().when(companyService).deleteCompany(id);

        mockMvc.perform(delete("/companies/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn400_WhenIdIsInvalid() throws Exception {
        long invalidId = 0L;

        mockMvc.perform(delete("/companies/{id}", invalidId))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("ID must be a positive number and not less than 1")));
    }

    @Test
    void shouldReturn404_WhenCompanyNotFound() throws Exception {
        long id = 999L;

        doThrow(new NotFoundException("Company not found")).when(companyService).deleteCompany(id);

        mockMvc.perform(delete("/companies/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnListOfCompanies_WhenValidRequest() throws Exception {
        int page = 0;
        int size = 2;

        List<CompanyDTO> companies = List.of(
                new CompanyDTO(1L, "TechCorp", new BigDecimal("100000.00")),
                new CompanyDTO(2L, "BizGroup", new BigDecimal("200000.00"))
        );

        when(companyService.getCompanies(page, size)).thenReturn(companies);

        mockMvc.perform(get("/companies")
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(companies)));
    }

    @Test
    void shouldReturn400_WhenPageIsNegative() throws Exception {
        mockMvc.perform(get("/companies")
                        .param("page", "-1")
                        .param("size", "10"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Page must be greater than or equal to 0")));
    }

    @Test
    void shouldReturn400_WhenSizeIsLessThanOne() throws Exception {
        mockMvc.perform(get("/companies")
                        .param("page", "0")
                        .param("size", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Size must be greater than or equal to 1")));
    }

    @Test
    void getCompanyById_ShouldReturnCompany_WhenExists() throws Exception {
        long id = 1L;
        CompanyDTO dto = new CompanyDTO(id, "TestCompany", new BigDecimal("5000.00"));

        when(companyService.getCompanyById(id)).thenReturn(dto);

        mockMvc.perform(get("/companies/{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(dto)));
    }

    @Test
    void getCompanyById_ShouldReturn404_WhenCompanyNotFound() throws Exception {
        long id = 999L;

        when(companyService.getCompanyById(id)).thenThrow(new NotFoundException("Company not found"));

        mockMvc.perform(get("/companies/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCompanyById_ShouldReturn400_WhenIdIsLessThanOne() throws Exception {
        mockMvc.perform(get("/companies/{id}", 0))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("must be a positive number and not less than 1")));
    }
}