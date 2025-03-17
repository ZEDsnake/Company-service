package com.zed.company_service.mapper;

import com.zed.company_service.dto.CompanyDTO;
import com.zed.company_service.entity.CompanyEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface CompanyMapper {
    CompanyMapper INSTANCE = Mappers.getMapper(CompanyMapper.class);

    CompanyDTO toDTO(CompanyEntity entity);
    CompanyEntity toEntity(CompanyDTO dto);
}