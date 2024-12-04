package com.consoleconnect.vortex.gateway.mapper;

import com.consoleconnect.vortex.gateway.dto.Resource;
import com.consoleconnect.vortex.gateway.entity.ResourceEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper()
public interface ResourceMapper {
  ResourceMapper INSTANCE = Mappers.getMapper(ResourceMapper.class);

  Resource toDto(ResourceEntity entity);
}
