package com.consoleconnect.vortex.gateway.service;

import com.consoleconnect.vortex.gateway.dto.CreateResourceRequest;
import com.consoleconnect.vortex.gateway.entity.ResourceEntity;
import com.consoleconnect.vortex.gateway.enums.ResourceTypeEnum;
import com.consoleconnect.vortex.gateway.repo.ResourceRepository;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ResourceService {
  private ResourceRepository resourceRepository;

  public ResourceEntity create(CreateResourceRequest request) {

    ResourceEntity resource = new ResourceEntity();
    resource.setOrganizationId(request.getOrganizationId());
    resource.setResourceType(request.getResourceType());
    resource.setOrderId(request.getOrderId());
    resource.setResourceId(request.getResourceId());
    resource.setSyncResourceConfig(request.getSyncResourceConfig());
    return resourceRepository.save(resource);
  }

  public List<ResourceEntity> findAllByOrganizationIdAndResourceType(
      String organizationId, ResourceTypeEnum resourceTypeEnum) {
    return resourceRepository.findAllByOrganizationIdAndResourceType(
        organizationId, resourceTypeEnum);
  }
}
