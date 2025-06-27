package com.zed.company_service.mapper;

import com.zed.company_service.dto.CompanyInfoDto;
import com.zed.company_service.dto.CompanyResponseDto;
import com.zed.company_service.dto.CreateCompanyDto;
import com.zed.company_service.dto.PatchCompanyDto;
import com.zed.company_service.dto.UpdateCompanyDto;
import com.zed.company_service.dto.UserInfoDto;
import com.zed.company_service.entity.CompanyEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CompanyMapper {

    CompanyResponseDto toCompanyResponseDto(CompanyEntity entity, List<UserInfoDto> employees);

    CompanyInfoDto toCompanyInfoDto(CompanyEntity entity);

    CompanyEntity toCompanyEntity(CreateCompanyDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromUpdateDto(UpdateCompanyDto dto, @MappingTarget CompanyEntity entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromPatchDto(PatchCompanyDto dto, @MappingTarget CompanyEntity entity);
}