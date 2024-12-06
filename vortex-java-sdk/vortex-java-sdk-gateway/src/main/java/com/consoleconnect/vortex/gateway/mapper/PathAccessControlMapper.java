package com.consoleconnect.vortex.gateway.mapper;

import com.consoleconnect.vortex.gateway.dto.CreatePathAccessRuleRequest;
import com.consoleconnect.vortex.gateway.dto.PathAccessRule;
import com.consoleconnect.vortex.gateway.entity.PathAccessRuleEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper()
public interface PathAccessControlMapper {
  PathAccessControlMapper INSTANCE = Mappers.getMapper(PathAccessControlMapper.class);

  PathAccessRule toDto(PathAccessRuleEntity entity);

  PathAccessRuleEntity toEntity(CreatePathAccessRuleRequest request);
}
