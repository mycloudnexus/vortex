package com.consoleconnect.vortex.gateway.repo;

import com.consoleconnect.vortex.gateway.entity.ResourceEntity;
import com.consoleconnect.vortex.gateway.enums.ResourceTypeEnum;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface ResourceRepository
    extends PagingAndSortingRepository<ResourceEntity, UUID>, CrudRepository<ResourceEntity, UUID> {

  List<ResourceEntity> findAllByOrganizationIdAndResourceType(
      String organizationId, ResourceTypeEnum resourceTypeEnum);

  Optional<ResourceEntity> findOneByOrganizationIdAndResourceTypeAndOrderId(
      String organizationId, ResourceTypeEnum resourceTypeEnum, String orderId);

  Optional<ResourceEntity> findOneByOrganizationIdAndResourceTypeAndResourceId(
      String organizationId, ResourceTypeEnum resourceTypeEnum, String resourceId);
}
