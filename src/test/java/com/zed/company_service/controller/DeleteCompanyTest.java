package com.zed.company_service.controller;

import com.zed.company_service.exception.NotFoundException;
import com.zed.company_service.service.CompanyService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
class DeleteCompanyTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CompanyService companyService;

    @Test
    void deleteCompany_ValidId_ReturnsNoContent() throws Exception {
        Long id = 1L;

        // Мокируем сервис: удаление проходит успешно
        doNothing().when(companyService).deleteCompany(id);

        mockMvc.perform(delete("/companies/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteCompany_InvalidId_ReturnsBadRequest() throws Exception {
        Long id = 0L; // Невалидный ID

        mockMvc.perform(delete("/companies/{id}", id))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("ID must be a positive number and not less than 1"));
    }

    @Test
    void deleteCompany_NotFound_ReturnsNotFound() throws Exception {
        Long id = 999L;

        // Мокируем сервис: выбрасываем исключение, если компания не найдена
        doThrow(new NotFoundException("Company not found with id: " + id))
                .when(companyService).deleteCompany(id);

        mockMvc.perform(delete("/companies/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Company not found with id: " + id));
    }
}
