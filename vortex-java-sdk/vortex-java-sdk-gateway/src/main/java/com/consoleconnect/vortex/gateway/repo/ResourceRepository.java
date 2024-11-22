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

  List<ResourceEntity> findAllByCustomerIdAndResourceType(
      String customerId, ResourceTypeEnum resourceTypeEnum);

  Optional<ResourceEntity> findOneByCustomerIdAndResourceTypeAndOrderId(
      String customerId, ResourceTypeEnum resourceTypeEnum, String orderId);

  Optional<ResourceEntity> findOneByCustomerIdAndResourceTypeAndResourceId(
      String customerId, ResourceTypeEnum resourceTypeEnum, String resourceId);
}
