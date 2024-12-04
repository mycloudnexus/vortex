package com.consoleconnect.vortex.gateway.repo;

import com.consoleconnect.vortex.gateway.entity.OrderEntity;
import com.consoleconnect.vortex.gateway.enums.ResourceTypeEnum;
import java.util.List;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface OrderRepository
    extends PagingAndSortingRepository<OrderEntity, UUID>, CrudRepository<OrderEntity, UUID> {
  List<OrderEntity> findByOrganizationIdAndResourceType(
      String organizationId, ResourceTypeEnum resourceType);

  List<OrderEntity> findByOrganizationIdAndResourceTypeAndResourceIdNotNull(
      String organizationId, ResourceTypeEnum resourceType);
}
