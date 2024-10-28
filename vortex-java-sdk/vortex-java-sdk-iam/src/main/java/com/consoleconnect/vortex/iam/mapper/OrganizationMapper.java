package com.consoleconnect.vortex.iam.mapper;

import com.consoleconnect.vortex.iam.dto.OrganizationDto;
import com.consoleconnect.vortex.iam.entity.OrganizationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper()
public interface OrganizationMapper {
  OrganizationMapper INSTANCE = Mappers.getMapper(OrganizationMapper.class);

  OrganizationDto toOrganizationDto(OrganizationEntity user);
}
