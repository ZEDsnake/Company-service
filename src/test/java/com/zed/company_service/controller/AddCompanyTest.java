package com.zed.company_service.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zed.company_service.dto.CompanyDTO;
import com.zed.company_service.dto.CreateCompanyDTO;
import com.zed.company_service.exception.GlobalExceptionHandler;
import com.zed.company_service.exception.NotFoundException;
import com.zed.company_service.service.CompanyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;

@ExtendWith(MockitoExtension.class)
class AddCompanyTest {

    private MockMvc mockMvc;

    @Mock
    private CompanyService companyService;

    @InjectMocks
    private CompanyController companyController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final CreateCompanyDTO request = new CreateCompanyDTO();

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(companyController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        request.setName("Valid Name");
        request.setBudget(BigDecimal.valueOf(1000));
    }

    @Test
    void addCompany_ValidRequest_ReturnsCreated() throws Exception {
        CompanyDTO response = new CompanyDTO();
        response.setName(request.getName());
        response.setBudget(request.getBudget());

        // Мокируем сервис
        when(companyService.addCompany(any(CreateCompanyDTO.class))).thenReturn(response);

        // Выполняем запрос и проверяем результат
        mockMvc.perform(post("/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(request.getName()))
                .andExpect(jsonPath("$.budget").value(request.getBudget().doubleValue()));
    }

    @Test
    void addCompany_InvalidName_ReturnsBadRequest() throws Exception {
        request.setName("A"); // Нарушение @Size(min = 3, max = 100)

        mockMvc.perform(post("/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("Name must be between 3 and 100 characters"));
    }

    @Test
    void addCompany_EmptyName_ReturnsBadRequest() throws Exception {
        request.setName(" ");// пустая строка

        mockMvc.perform(post("/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("Name is required"));
    }

    @Test
    void addCompany_NameLength3_ReturnsSuccess() throws Exception {
        request.setName("ABC"); // Минимальная допустимая длина (3 символа)

        CompanyDTO response = new CompanyDTO();
        response.setName(request.getName());
        response.setBudget(request.getBudget());

        when(companyService.addCompany(any(CreateCompanyDTO.class))).thenReturn(response);

        mockMvc.perform(post("/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(request.getName()))
                .andExpect(jsonPath("$.budget").value(request.getBudget().doubleValue()));
    }

    @Test
    void addCompany_NameLength10_ReturnsSuccess() throws Exception {
        request.setName("A".repeat(10)); // 10 символов

        CompanyDTO response = new CompanyDTO();
        response.setName(request.getName());
        response.setBudget(request.getBudget());

        when(companyService.addCompany(any(CreateCompanyDTO.class))).thenReturn(response);

        mockMvc.perform(post("/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(request.getName()))
                .andExpect(jsonPath("$.budget").value(request.getBudget().doubleValue()));
    }

    @Test
    void addCompany_NameLength60_ReturnsSuccess() throws Exception {
        request.setName("A".repeat(60)); // 60 символов

        CompanyDTO response = new CompanyDTO();
        response.setName(request.getName());
        response.setBudget(request.getBudget());

        when(companyService.addCompany(any(CreateCompanyDTO.class))).thenReturn(response);

        mockMvc.perform(post("/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(request.getName()))
                .andExpect(jsonPath("$.budget").value(request.getBudget().doubleValue()));
    }

    @Test
    void addCompany_NameLength100_ReturnsSuccess() throws Exception {
        request.setName("A".repeat(100)); // Максимальная допустимая длина (100 символов)

        CompanyDTO response = new CompanyDTO();
        response.setName(request.getName());
        response.setBudget(request.getBudget());

        when(companyService.addCompany(any(CreateCompanyDTO.class))).thenReturn(response);

        mockMvc.perform(post("/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(request.getName()))
                .andExpect(jsonPath("$.budget").value(request.getBudget().doubleValue()));
    }

    @Test
    void addCompany_NameLength101_ReturnsBadRequest() throws Exception {
        request.setName("A".repeat(101)); // Превышение максимальной длины (101 символ)

        mockMvc.perform(post("/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("Name must be between 3 and 100 characters"));
    }


    @Test
    void addCompany_NullBudget_ReturnsBadRequest() throws Exception {
        request.setBudget(null); // Нарушение @NotNull

        // Выполняем запрос и проверяем результат
        mockMvc.perform(post("/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.budget").value("Budget is required"));
    }

    @Test
    void addCompany_NegativeBudget_ReturnsBadRequest() throws Exception {
        request.setBudget(BigDecimal.valueOf(-100)); // Отрицательный бюджет

        mockMvc.perform(post("/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.budget").value("Budget must be positive or zero"));
    }

    @Test
    void addCompany_NotFound_ReturnsNotFound() throws Exception {
        // Мокируем сервис, чтобы он выбросил исключение
        when(companyService.addCompany(any(CreateCompanyDTO.class)))
                .thenThrow(new NotFoundException("Company not found"));

        // Выполняем запрос и проверяем результат
        mockMvc.perform(post("/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Company not found"));
    }
}
