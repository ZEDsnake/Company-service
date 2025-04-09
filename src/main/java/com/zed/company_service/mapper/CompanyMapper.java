package com.zed.company_service.mapper;

import com.zed.company_service.dto.CompanyDTO;
import com.zed.company_service.dto.CreateCompanyDTO;
import com.zed.company_service.dto.UpdateCompanyDTO;
import com.zed.company_service.entity.CompanyEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = UserMapper.class)
public interface CompanyMapper {

    CompanyDTO toCompanyDTO(CompanyEntity entity);

    List<CompanyDTO> toCompanyDTOList(List<CompanyEntity> entities);

    @Mapping(target = "employees", ignore = true)
    CompanyEntity toCompanyEntity(CreateCompanyDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "employees", ignore = true)
    void updateEntityFromDTO(UpdateCompanyDTO dto, @MappingTarget CompanyEntity entity);
}