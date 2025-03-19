package com.zed.company_service.mapper;

import com.zed.company_service.dto.CompanyDTO;
import com.zed.company_service.dto.CreateCompanyDTO;
import com.zed.company_service.entity.CompanyEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CompanyMapper {

    CompanyDTO toCompanyDTO(CompanyEntity entity);

    CompanyEntity toCompanyEntity(CreateCompanyDTO dto);
}