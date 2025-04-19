package com.zed.company_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zed.company_service.dto.CompanyDTO;
import com.zed.company_service.dto.CreateCompanyDTO;
import com.zed.company_service.dto.UpdateCompanyDTO;
import com.zed.company_service.dto.UserInfoDTO;
import com.zed.company_service.exception.NotFoundException;
import com.zed.company_service.service.CompanyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CompanyController.class)
class CompanyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CompanyService companyService;

    @Autowired
    private ObjectMapper objectMapper;

    private CreateCompanyDTO createCompanyDTO = new CreateCompanyDTO();
    private UpdateCompanyDTO updateCompanyDTO = new UpdateCompanyDTO();
    private CompanyDTO companyDTO = new CompanyDTO();
    private CompanyEmployeesDTO companyEmployeesDTO;
    private List<UserInfoDTO> employees;

    @BeforeEach
    void setup() {
        createCompanyDTO = new CreateCompanyDTO("Test Company", BigDecimal.valueOf(1000));
        updateCompanyDTO = new UpdateCompanyDTO("Updated Company", BigDecimal.valueOf(2000));
        companyDTO = new CompanyDTO(1L, "Test Company", BigDecimal.valueOf(1000));
        employees = List.of(
                new UserInfoDTO("Ivan", "Ivanov", "+79991234567"),
                new UserInfoDTO("Maria", "Petrova", "+79998765432")
        );
        companyEmployeesDTO = new CompanyEmployeesDTO(1L, "Test Company", BigDecimal.valueOf(1000), employees);
    }

    @Test
    void getCompanyWithEmployees_ValidId_ReturnsOk() throws Exception {
        when(companyService.getCompanyWithEmployees(1L)).thenReturn(companyEmployeesDTO);

        mockMvc.perform(get("/companies/{id}/employees", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(companyEmployeesDTO.getId()))
                .andExpect(jsonPath("$.name").value(companyEmployeesDTO.getName()))
                .andExpect(jsonPath("$.budget").value(companyEmployeesDTO.getBudget().doubleValue()))

                .andExpect(jsonPath("$.employees", hasSize(2)))

                .andExpect(jsonPath("$.employees[0].firstName").value(employees.get(0).getFirstName()))
                .andExpect(jsonPath("$.employees[0].lastName").value(employees.get(0).getLastName()))
                .andExpect(jsonPath("$.employees[0].phoneNumber").value(employees.get(0).getPhoneNumber()))
                
                .andExpect(jsonPath("$.employees[1].firstName").value(employees.get(1).getFirstName()))
                .andExpect(jsonPath("$.employees[1].lastName").value(employees.get(1).getLastName()))
                .andExpect(jsonPath("$.employees[1].phoneNumber").value(employees.get(1).getPhoneNumber()));
    }

    @Test
    void getCompanyWithEmployees_NotFound_ReturnsNotFound() throws Exception {
        Long id = 999L;
        when(companyService.getCompanyWithEmployees(id))
                .thenThrow(new NotFoundException("Company not found with id: " + id));

        mockMvc.perform(get("/companies/{id}/employees", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Company not found with id: " + id));
    }

    @Test
    void getCompanyWithEmployees_InvalidId_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/companies/0/employees"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("ID must be a positive number and not less than 1"));
    }

    @Test
    void addCompany_ValidRequest_ReturnsCreated() throws Exception {
        when(companyService.addCompany(any(CreateCompanyDTO.class))).thenReturn(companyDTO);

        mockMvc.perform(post("/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createCompanyDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(companyDTO.getId()))
                .andExpect(jsonPath("$.name").value(companyDTO.getName()))
                .andExpect(jsonPath("$.budget").value(companyDTO.getBudget().doubleValue()));
    }

    @Test
    void addCompany_InvalidName_ReturnsBadRequest() throws Exception {
        createCompanyDTO.setName("A");

        mockMvc.perform(post("/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createCompanyDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("Name must be between 3 and 100 characters"));
    }

    @Test
    void addCompany_NameMinLength_ReturnsCreated() throws Exception {
        createCompanyDTO.setName("abc");

        companyDTO.setName("abc");

        when(companyService.addCompany(any(CreateCompanyDTO.class))).thenReturn(companyDTO);

        mockMvc.perform(post("/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createCompanyDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("abc"));
    }

    @Test
    void addCompany_NameBelowMinLength_ReturnsBadRequest() throws Exception {
        createCompanyDTO.setName("ab");

        mockMvc.perform(post("/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createCompanyDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("Name must be between 3 and 100 characters"));
    }

    @Test
    void addCompany_NameMidLength_ReturnsCreated() throws Exception {
        createCompanyDTO.setName("a".repeat(50));

        companyDTO.setName("a".repeat(50));

        when(companyService.addCompany(any(CreateCompanyDTO.class))).thenReturn(companyDTO);

        mockMvc.perform(post("/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createCompanyDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("a".repeat(50)));
    }

    @Test
    void addCompany_NameMaxLength_ReturnsCreated() throws Exception {
        createCompanyDTO.setName("a".repeat(100));

        companyDTO.setName("a".repeat(100));

        when(companyService.addCompany(any(CreateCompanyDTO.class))).thenReturn(companyDTO);

        mockMvc.perform(post("/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createCompanyDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("a".repeat(100)));
    }

    @Test
    void addCompany_NameAboveMaxLength_ReturnsBadRequest() throws Exception {
        createCompanyDTO.setName("a".repeat(101));

        mockMvc.perform(post("/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createCompanyDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("Name must be between 3 and 100 characters"));
    }

    @Test
    void addCompany_EmptyName_ReturnsBadRequest() throws Exception {
        createCompanyDTO.setName("   "); // Три пробела, чтобы проверить @NotBlank

        mockMvc.perform(post("/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createCompanyDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("Name is required"));
    }

    @Test
    void addCompany_NullBudget_ReturnsBadRequest() throws Exception {
        createCompanyDTO.setBudget(null);

        mockMvc.perform(post("/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createCompanyDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.budget").value("Budget is required"));
    }

    @Test
    void addCompany_NegativeBudget_ReturnsBadRequest() throws Exception {
        createCompanyDTO.setBudget(BigDecimal.valueOf(-100));

        mockMvc.perform(post("/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createCompanyDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.budget").value("Budget must be positive or zero"));
    }

    @Test
    void getCompanyById_ValidId_ReturnsOk() throws Exception {
        when(companyService.getCompanyById(1L)).thenReturn(companyDTO);

        mockMvc.perform(get("/companies/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(companyDTO.getId()))
                .andExpect(jsonPath("$.name").value(companyDTO.getName()))
                .andExpect(jsonPath("$.budget").value(companyDTO.getBudget().doubleValue()));
    }

    @Test
    void getCompanyById_InvalidId_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/companies/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("ID must be a positive number and not less than 1"));
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
    void updateCompanyDTO_ValidRequest_ReturnsOk() throws Exception {
        when(companyService.updateCompany(anyLong(), any(UpdateCompanyDTO.class))).thenReturn(companyDTO);

        mockMvc.perform(put("/companies/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateCompanyDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(companyDTO.getId()))
                .andExpect(jsonPath("$.name").value(companyDTO.getName()))
                .andExpect(jsonPath("$.budget").value(companyDTO.getBudget().doubleValue()));
    }

    @Test
    void updateCompanyDTO_InvalidId_ReturnsBadRequest() throws Exception {
        mockMvc.perform(put("/companies/0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateCompanyDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("ID must be a positive number and not less than 1"));
    }

    @Test
    void updateCompany_NotFound_ReturnsNotFound() throws Exception {
        Long id = 999L;

        when(companyService.updateCompany(id, updateCompanyDTO)).thenThrow(new NotFoundException("Company not found with id: " + id));

        mockMvc.perform(put("/companies/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateCompanyDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Company not found with id: " + id));
    }

    @Test
    void updateCompany_NegativeBudget_ReturnsBadRequest() throws Exception {
        updateCompanyDTO.setBudget(BigDecimal.valueOf(-100));

        mockMvc.perform(put("/companies/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateCompanyDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.budget").value("Budget must be positive or zero"));
    }

    @Test
    void deleteCompany_ValidId_ReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/companies/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(companyService).deleteCompany(1L);
    }

    @Test
    void deleteCompany_InvalidId_ReturnsBadRequest() throws Exception {
        mockMvc.perform(delete("/companies/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("ID must be a positive number and not less than 1"));
    }

    @Test
    void deleteCompany_NotFound_ReturnsNotFound() throws Exception {
        Long id = 999L;

        doThrow(new NotFoundException("Company not found with id: " + id)).when(companyService).deleteCompany(id);

        mockMvc.perform(delete("/companies/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Company not found with id: " + id));
    }

    @Test
    void getCompanies_ValidPagination_ReturnsOk() throws Exception {
        int page = 0;
        int size = 10;

        CompanyDTO companyA = new CompanyDTO(1L, "Company A", BigDecimal.valueOf(1000));
        CompanyDTO companyB = new CompanyDTO(2L, "Company B", BigDecimal.valueOf(2000));

        List<CompanyDTO> companies = List.of(companyA, companyB);

        when(companyService.getCompanies(page, size)).thenReturn(companies);

        mockMvc.perform(get("/companies")
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))

                .andExpect(jsonPath("$[0].id").value(companyA.getId()))
                .andExpect(jsonPath("$[0].name").value(companyA.getName()))
                .andExpect(jsonPath("$[0].budget").value(companyA.getBudget().doubleValue()))

                .andExpect(jsonPath("$[1].id").value(companyB.getId()))
                .andExpect(jsonPath("$[1].name").value(companyB.getName()))
                .andExpect(jsonPath("$[1].budget").value(companyB.getBudget().doubleValue()));
    }

    @Test
    void getCompanies_InvalidPage_ReturnsBadRequest() throws Exception {
        int page = -1;
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
        int size = 0;

        mockMvc.perform(get("/companies")
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Size must be greater than or equal to 1"));
    }

    @Test
    void getCompanies_PageMinValue_ReturnsOk() throws Exception {
        int page = 0;
        int size = 10;

        when(companyService.getCompanies(page, size)).thenReturn(List.of(companyDTO));

        mockMvc.perform(get("/companies")
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk());
    }

    @Test
    void getCompanies_PageBelowMinValue_ReturnsBadRequest() throws Exception {
        int page = -1;
        int size = 10;

        mockMvc.perform(get("/companies")
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Page must be greater than or equal to 0"));
    }

    @Test
    void getCompanies_SizeMinValue_ReturnsOk() throws Exception {
        int page = 0;
        int size = 1;

        when(companyService.getCompanies(page, size)).thenReturn(List.of(companyDTO));

        mockMvc.perform(get("/companies")
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk());
    }

    @Test
    void getCompanies_SizeBelowMinValue_ReturnsBadRequest() throws Exception {
        int page = 0;
        int size = 0;
        mockMvc.perform(get("/companies")
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Size must be greater than or equal to 1"));
    }

    @Test
    void getCompanies_SizeMaxValue_ReturnsOk() throws Exception {
        int page = 0;
        int size = 100;

        when(companyService.getCompanies(page, size)).thenReturn(List.of(companyDTO));

        mockMvc.perform(get("/companies")
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk());
    }
}

