package com.zed.company_service.controller;

import com.zed.company_service.dto.CompanyDTO;
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
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
class GetCompanyByIdTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @MockBean
    private CompanyService companyService;

    @Autowired // <- Удаляем @InjectMocks и используем @Autowired
    private CompanyController companyController;

    CompanyDTO response = new CompanyDTO();

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .build();
    }

    @Test
    void getCompanyById_ValidId_ReturnsCompanyDTO() throws Exception {
        Long id = 1L;
        response.setId(id);
        response.setName("Test Company");
        response.setBudget(BigDecimal.valueOf(1000));

        when(companyService.getCompanyById(id)).thenReturn(response);

        mockMvc.perform(get("/companies/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value(response.getName()))
                .andExpect(jsonPath("$.budget").value(response.getBudget().doubleValue()));
    }

    @Test
    void getCompanyById_NotFound_ReturnsNotFound() throws Exception {
        Long id = 999L;

        when(companyService.getCompanyById(id)).thenThrow(new NotFoundException("Company not found with id: " + id));

        mockMvc.perform(get("/companies/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Company not found with id: " + id));
    }

    @Test
    void getCompanyById_InvalidId_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/companies/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("ID must be a positive number and not less than 1"));
    }
}

