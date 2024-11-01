package com.consoleconnect.vortex.gateway.repo;

import com.consoleconnect.vortex.gateway.entity.OrderEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface OrderRepository
    extends PagingAndSortingRepository<OrderEntity, UUID>, CrudRepository<OrderEntity, UUID> {
  List<OrderEntity> findByOrganizationId(String organizationId);

  OrderEntity findByOrganizationIdAndOrderId(String organizationId, String orderId);
}
