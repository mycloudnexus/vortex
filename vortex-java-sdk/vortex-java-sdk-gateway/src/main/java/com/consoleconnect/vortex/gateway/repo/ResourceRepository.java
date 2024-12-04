package com.consoleconnect.vortex.gateway.repo;

import com.consoleconnect.vortex.gateway.entity.ResourceEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface ResourceRepository
    extends PagingAndSortingRepository<ResourceEntity, UUID>, CrudRepository<ResourceEntity, UUID> {

  @Query(
      value =
          "select e from #{#entityName} e "
              + " where ( (:customerId) is null or  e.customerId = :customerId )"
              + " and ( (:resourceType) is null or  e.resourceType = :resourceType )"
              + " and ( (:orderId) is null or  e.orderId = :orderId )"
              + " and ( (:resourceId) is null or  e.resourceId = :resourceId )")
  @Transactional(readOnly = true)
  Page<ResourceEntity> search(
      @Param("customerId") String customerId,
      @Param("resourceType") String resourceType,
      @Param("orderId") String orderId,
      @Param("resourceId") String resourceId,
      Pageable pageable);

  List<ResourceEntity> findAllByCustomerId(String customerId);

  List<ResourceEntity> findAllByCustomerIdAndResourceType(String customerId, String resourceType);

  Optional<ResourceEntity> findOneByCustomerIdAndResourceTypeAndOrderId(
      String customerId, String resourceType, String orderId);
}
