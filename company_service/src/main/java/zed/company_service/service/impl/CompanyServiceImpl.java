package zed.company_service.service.impl;

import zed.company_service.dto.CompanyDTO;
import zed.company_service.dto.CreateCompanyDTO;
import zed.company_service.dto.UpdateCompanyDTO;
import zed.company_service.dto.UserInfoDTO;
import zed.company_service.entity.CompanyEntity;
import zed.company_service.exception.AlreadyExistsException;
import zed.company_service.exception.NotFoundException;
import zed.company_service.feign.UserClient;
import zed.company_service.mapper.CompanyMapper;
import zed.company_service.repository.CompanyRepository;
import zed.company_service.service.CompanyService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class CompanyServiceImpl implements CompanyService {

    private final UserClient userClient;
    private final CompanyMapper companyMapper;
    private final CompanyRepository companyRepository;


    @Override
    public List<UserInfoDTO> getCompanyEmployees(Long companyId, int page, int size) {
        return userClient.getUsersByCompanyId(companyId, page, size);
    }


    @Override
    @Transactional
    public CompanyDTO addCompany(CreateCompanyDTO createCompanyDTO) {
        String name = createCompanyDTO.getName();

        checkIfCompanyAlreadyExists(name);
        CompanyEntity companyEntity = companyMapper.toCompanyEntity(createCompanyDTO);
        CompanyEntity savedEntity = companyRepository.save(companyEntity);
        CompanyDTO result = companyMapper.toCompanyDTO(savedEntity);
        log.info("CompanyService: addCompany method result: {}", result);
        return result;
    }

    @Override
    @Transactional
    public CompanyDTO updateCompany(Long id, UpdateCompanyDTO updateCompanyDTO) {
        CompanyEntity companyEntity = findCompanyById(id);

        validateCompanyNameChange(updateCompanyDTO.getName(), companyEntity);

        companyMapper.updateEntityFromDTO(updateCompanyDTO, companyEntity);
        CompanyEntity updatedEntity = companyRepository.save(companyEntity);
        CompanyDTO result = companyMapper.toCompanyDTO(updatedEntity);
        log.info("CompanyService: updateCompany method result: {}", result);
        return result;
    }

    @Override
    @Transactional
    public void deleteCompany(Long companyId) {
        CompanyEntity company = findCompanyById(companyId);
        userClient.deleteUsersByCompanyId(company.getId());
        companyRepository.delete(company);
        log.info("CompanyService: Deleted company with id: {}", companyId);
    }

    @Override
    @Transactional
    public List<CompanyDTO> getCompanies(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<CompanyEntity> companyPage = companyRepository.findAll(pageable);
        List<CompanyDTO> companyDTOList = companyMapper.toCompanyDTOList(companyPage.getContent());
        log.info("Retrieved {} companies for page {} with size {}", companyDTOList.size(), page, size);
        return companyDTOList;
    }

    @Override
    public CompanyDTO getCompanyById(Long id) {
        CompanyEntity company = findCompanyById(id);
        return companyMapper.toCompanyDTO(company);
    }

    private void validateCompanyNameChange(String newName, CompanyEntity existingCompany) {
    if (newName != null && !newName.equals(existingCompany.getName())) {
        checkIfCompanyAlreadyExists(newName);
    }
}

private CompanyEntity findCompanyById(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Company not found with id: " + id));
    }

private void checkIfCompanyAlreadyExists(String name) {
        if (companyRepository.existsByName(name)) {
            log.warn("Attempt to create a company with existing name: {}", name);
            throw new AlreadyExistsException("Company with name \"" + name + "\" already exists");
        }
    }
}









