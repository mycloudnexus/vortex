package com.consoleconnect.vortex.gateway.service;

import com.consoleconnect.vortex.core.exception.VortexException;
import com.consoleconnect.vortex.gateway.dto.CreateResourceRequest;
import com.consoleconnect.vortex.gateway.entity.ResourceEntity;
import com.consoleconnect.vortex.gateway.repo.ResourceRepository;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Slf4j
public class ResourceService {
  private ResourceRepository resourceRepository;

  @Transactional
  public ResourceEntity create(CreateResourceRequest request) {

    log.info("create resource: {}", request);
    ResourceEntity resource = new ResourceEntity();
    resource.setCustomerId(request.getCustomerId());
    resource.setResourceType(request.getResourceType());
    resource.setOrderId(request.getOrderId());
    resource.setResourceId(request.getResourceId());
    return resourceRepository.save(resource);
  }

  public List<ResourceEntity> findAllByCustomerIdAndResourceType(
      String customerId, String resourceType) {
    if (resourceType == null) {
      return resourceRepository.findAllByCustomerId(customerId);
    }
    return resourceRepository.findAllByCustomerIdAndResourceType(customerId, resourceType);
  }

  @Transactional
  public void updateResourceId(
      String customerId, String resourceType, String orderId, String resourceId) {
    ResourceEntity resource =
        resourceRepository
            .findOneByCustomerIdAndResourceTypeAndOrderId(customerId, resourceType, orderId)
            .orElseThrow(() -> VortexException.notFound("Resource not found"));
    resource.setResourceId(resourceId);
    resourceRepository.save(resource);
  }

  @Transactional
  public void updateAll(List<ResourceEntity> resources) {
    log.info("update resources: {}", resources.size());
    resourceRepository.saveAll(resources);
  }
}
