package com.zed.company_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zed.company_service.dto.CompanyDTO;
import com.zed.company_service.dto.UpdateCompanyDTO;
import com.zed.company_service.exception.NotFoundException;
import com.zed.company_service.service.CompanyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
class UpdateCompanyTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CompanyService companyService;

    @Autowired
    private ObjectMapper objectMapper;

    private UpdateCompanyDTO updateCompanyDTO;
    private CompanyDTO companyDTO;

    @BeforeEach
    void setup() {
        updateCompanyDTO = new UpdateCompanyDTO();
        updateCompanyDTO.setName("Updated Company");
        updateCompanyDTO.setBudget(BigDecimal.valueOf(2000));

        companyDTO = new CompanyDTO();
        companyDTO.setId(1L);
        companyDTO.setName("Updated Company");
        companyDTO.setBudget(BigDecimal.valueOf(2000));
    }

    @Test
    void updateCompanyDTO_ValidIdAndBody_ReturnsUpdatedCompanyDTO() throws Exception {
        Long id = 1L;

        when(companyService.updateCompany(id, updateCompanyDTO)).thenReturn(companyDTO);

        mockMvc.perform(put("/companies/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateCompanyDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value(updateCompanyDTO.getName()))
                .andExpect(jsonPath("$.budget").value(updateCompanyDTO.getBudget().doubleValue()));
    }

    @Test
    void updateCompanyDTO_InvalidId_ReturnsBadRequest() throws Exception {
        Long id = 0L; // Невалидный ID

        mockMvc.perform(put("/companies/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateCompanyDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("ID must be a positive number and not less than 1"));
    }

    @Test
    void updateCompanyDTO_InvalidName_ReturnsBadRequest() throws Exception {
        Long id = 1L;
        updateCompanyDTO.setName("A"); // Нарушение @Size(min = 3, max = 100)

        mockMvc.perform(put("/companies/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateCompanyDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("Name must be between 3 and 100 characters"));
    }

    @Test
    void updateCompanyDTO_EmptyName_ReturnsBadRequest() throws Exception {
        Long id = 1L;
        updateCompanyDTO.setName(""); // Пустая строка

        mockMvc.perform(put("/companies/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateCompanyDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("Name must be between 3 and 100 characters"));
    }

    @Test
    void updateCompanyDTO_NameLength3_ReturnsSuccess() throws Exception {
        Long id = 1L;
        String name = "ABC";
        updateCompanyDTO.setName(name);

        CompanyDTO response = new CompanyDTO();
        response.setId(id);
        response.setName(name);
        response.setBudget(updateCompanyDTO.getBudget());

        when(companyService.updateCompany(id, updateCompanyDTO)).thenReturn(response);

        mockMvc.perform(put("/companies/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateCompanyDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(name));
    }

    @Test
    void updateCompanyDTO_NameLength10_ReturnsSuccess() throws Exception {
        Long id = 1L;
        String name = "A".repeat(10); // 10 символов
        updateCompanyDTO.setName(name);

        CompanyDTO response = new CompanyDTO();
        response.setId(id);
        response.setName(name);
        response.setBudget(updateCompanyDTO.getBudget());

        when(companyService.updateCompany(id, updateCompanyDTO)).thenReturn(response);

        mockMvc.perform(put("/companies/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateCompanyDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(name));
    }

    @Test
    void updateCompanyDTO_NameLength60_ReturnsSuccess() throws Exception {
        Long id = 1L;
        String name = "A".repeat(60); // 60 символов
        updateCompanyDTO.setName(name);

        CompanyDTO response = new CompanyDTO();
        response.setId(id);
        response.setName(name);
        response.setBudget(updateCompanyDTO.getBudget());

        when(companyService.updateCompany(id, updateCompanyDTO)).thenReturn(response);

        mockMvc.perform(put("/companies/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateCompanyDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(name));
    }

    @Test
    void updateCompanyDTO_NameLength100_ReturnsSuccess() throws Exception {
        Long id = 1L;
        String name = "A".repeat(100); // 100 символов
        updateCompanyDTO.setName(name);

        CompanyDTO response = new CompanyDTO();
        response.setId(id);
        response.setName(name);
        response.setBudget(updateCompanyDTO.getBudget());

        when(companyService.updateCompany(id, updateCompanyDTO)).thenReturn(response);

        mockMvc.perform(put("/companies/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateCompanyDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(name));
    }

    @Test
    void updateCompanyDTO_NameLength101_ReturnsBadRequest() throws Exception {
        Long id = 1L;
        updateCompanyDTO.setName("A".repeat(101)); // Превышение максимальной длины (101 символ)

        mockMvc.perform(put("/companies/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateCompanyDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("Name must be between 3 and 100 characters"));
    }
    @Test
    void updateCompanyDTO_NegativeBudget_ReturnsBadRequest() throws Exception {
        Long id = 1L;
        updateCompanyDTO.setBudget(BigDecimal.valueOf(-100)); // Отрицательный бюджет

        mockMvc.perform(put("/companies/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateCompanyDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.budget").value("Budget must be positive or zero"));
    }

    @Test
    void updateCompanyDTO_NotFound_ReturnsNotFound() throws Exception {
        Long id = 999L;

        when(companyService.updateCompany(id, updateCompanyDTO)).thenThrow(new NotFoundException("Company not found with id: " + id));

        mockMvc.perform(put("/companies/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateCompanyDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Company not found with id: " + id));
    }
}
