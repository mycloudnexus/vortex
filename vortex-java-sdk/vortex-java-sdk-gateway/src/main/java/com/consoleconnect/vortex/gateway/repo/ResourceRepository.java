package com.consoleconnect.vortex.gateway.repo;

import com.consoleconnect.vortex.gateway.entity.ResourceEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface ResourceRepository
    extends PagingAndSortingRepository<ResourceEntity, UUID>, CrudRepository<ResourceEntity, UUID> {

  List<ResourceEntity> findAllByCustomerId(String customerId);

  List<ResourceEntity> findAllByCustomerIdAndResourceType(String customerId, String resourceType);

  Optional<ResourceEntity> findOneByCustomerIdAndResourceTypeAndOrderId(
      String customerId, String resourceType, String orderId);
}
