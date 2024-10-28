package com.consoleconnect.vortex.core.repo;

import com.consoleconnect.vortex.core.entity.OrderEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface OrderRepository
    extends PagingAndSortingRepository<OrderEntity, UUID>, CrudRepository<OrderEntity, UUID> {
  Optional<OrderEntity> findByOrganizationId(String organizationId);
}
