package com.zed.company_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zed.company_service.dto.CompanyDTO;
import com.zed.company_service.service.CompanyService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
public class GetCompaniesTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CompanyService companyService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getCompanies_ValidPagination_ReturnsListOfCompanies() throws Exception {
        int page = 0;
        int size = 10;

        // Создаем список компаний для мок-ответа
        List<CompanyDTO> companies = List.of(
                new CompanyDTO() {{
                    setId(1L);
                    setName("Company A");
                    setBudget(BigDecimal.valueOf(1000));
                }},
                new CompanyDTO() {{
                    setId(2L);
                    setName("Company B");
                    setBudget(BigDecimal.valueOf(2000));
                }}
        );

        // Мокируем сервис
        when(companyService.getCompanies(page, size)).thenReturn(companies);

        mockMvc.perform(get("/companies")
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2))) // Проверяем количество компаний
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Company A"))
                .andExpect(jsonPath("$[0].budget").value(1000.0))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].name").value("Company B"))
                .andExpect(jsonPath("$[1].budget").value(2000.0));
    }

    @Test
    void getCompanies_InvalidPage_ReturnsBadRequest() throws Exception {
        int page = -1; // Невалидный page
        int size = 10;

        mockMvc.perform(get("/companies")
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Page must be greater than or equal to 0"));
    }

    @Test
    void getCompanies_InvalidSize_ReturnsBadRequest() throws Exception {
        int page = 0;
        int size = 0; // Невалидный size

        mockMvc.perform(get("/companies")
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Size must be greater than or equal to 1"));
    }

    @Test
    void getCompanies_EmptyList_ReturnsEmptyList() throws Exception {
        int page = 0;
        int size = 10;

        // Мокируем сервис: возвращаем пустой список
        when(companyService.getCompanies(page, size)).thenReturn(List.of());

        mockMvc.perform(get("/companies")
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0))); // Проверяем, что список пуст
    }
}