package com.zed.company_service.controller;

import com.zed.company_service.dto.CompanyDTO;
import com.zed.company_service.exception.GlobalExceptionHandler;
import com.zed.company_service.exception.NotFoundException;
import com.zed.company_service.service.CompanyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class GetCompanyByIdTest {

    private MockMvc mockMvc;

    @Mock
    private CompanyService companyService;

    @InjectMocks
    private CompanyController companyController;

    CompanyDTO response = new CompanyDTO();

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(companyController)
                .setControllerAdvice(new GlobalExceptionHandler()) // Обработчик исключений
                .build();
    }

    @Test
    void getCompanyById_ValidId_ReturnsCompanyDTO() throws Exception {
        // Подготовка данных
        Long id = 1L;
        response.setId(id);
        response.setName("Test Company");
        response.setBudget(BigDecimal.valueOf(1000));

        // Мокируем сервис
        when(companyService.getCompanyById(id)).thenReturn(response);

        // Выполняем запрос и проверяем результат
        mockMvc.perform(get("/companies/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value(response.getName()))
                .andExpect(jsonPath("$.budget").value(response.getBudget().doubleValue()));
    }

//    @Test
//    void getCompanyById_InvalidId_ReturnsBadRequest() throws Exception {
//
//        mockMvc.perform(get("/companies/0"))
//                .andExpect(status().isBadRequest()) // Ожидаем статус 400 Bad Request
//                .andExpect(jsonPath("$.timestamp").exists()) // Проверяем наличие поля timestamp
//                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value())) // Проверяем статус
//                .andExpect(jsonPath("$.error").value("Bad Request")) // Проверяем тип ошибки
//                .andExpect(jsonPath("$.message").value("ID must be a positive number and not less than 1")); // Проверяем сообщение
//    }



    @Test
    void getCompanyById_NotFound_ReturnsNotFound() throws Exception {
        Long id = 999999L;

        when(companyService.getCompanyById(id)).thenThrow(new NotFoundException("Company not found with id: " + id));

        mockMvc.perform(get("/companies/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Company not found with id: " + id));
    }
}
