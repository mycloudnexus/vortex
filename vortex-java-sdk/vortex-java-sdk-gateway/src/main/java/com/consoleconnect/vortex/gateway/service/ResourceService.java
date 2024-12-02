package com.consoleconnect.vortex.gateway.service;

import com.consoleconnect.vortex.core.exception.VortexException;
import com.consoleconnect.vortex.core.toolkit.Paging;
import com.consoleconnect.vortex.core.toolkit.PagingHelper;
import com.consoleconnect.vortex.gateway.dto.CreateResourceRequest;
import com.consoleconnect.vortex.gateway.dto.Resource;
import com.consoleconnect.vortex.gateway.dto.UpdateResourceRequest;
import com.consoleconnect.vortex.gateway.entity.ResourceEntity;
import com.consoleconnect.vortex.gateway.mapper.ResourceMapper;
import com.consoleconnect.vortex.gateway.repo.ResourceRepository;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Slf4j
public class ResourceService {
  private ResourceRepository resourceRepository;

  public Paging<Resource> search(
      String customerId,
      String resourceType,
      String orderId,
      String resourceId,
      int page,
      int size) {
    Page<ResourceEntity> data =
        resourceRepository.search(
            customerId, resourceType, orderId, resourceId, PagingHelper.toPageable(page, size));

    return PagingHelper.toPaging(data, ResourceMapper.INSTANCE::toDto);
  }

  @Transactional
  public Resource create(CreateResourceRequest request, String createdBy) {

    log.info("create resource: {},createdBy:{}", request, createdBy);
    ResourceEntity entity = new ResourceEntity();
    entity.setCustomerId(request.getCustomerId());
    entity.setResourceType(request.getResourceType());
    entity.setOrderId(request.getOrderId());
    entity.setResourceId(request.getResourceId());
    entity.setCreatedBy(createdBy);
    entity = resourceRepository.save(entity);
    return ResourceMapper.INSTANCE.toDto(entity);
  }

  @Transactional
  public Resource update(String id, UpdateResourceRequest request, String updatedBy) {

    log.info("update resource,id:{},{},updatedBy:{}", id, request, updatedBy);

    ResourceEntity entity =
        resourceRepository
            .findById(UUID.fromString(id))
            .orElseThrow(() -> VortexException.notFound("Resource not found"));

    if (request.getCustomerId() != null) {
      entity.setCustomerId(request.getCustomerId());
    }
    if (request.getResourceType() != null) {
      entity.setResourceType(request.getResourceType());
    }
    if (request.getOrderId() != null) {
      entity.setOrderId(request.getOrderId());
    }
    if (request.getResourceId() != null) {
      entity.setResourceId(request.getResourceId());
    }
    entity.setUpdatedBy(updatedBy);
    entity = resourceRepository.save(entity);
    return ResourceMapper.INSTANCE.toDto(entity);
  }

  public Resource findOne(String id) {
    ResourceEntity entity =
        resourceRepository
            .findById(UUID.fromString(id))
            .orElseThrow(() -> VortexException.notFound("Resource not found"));
    return ResourceMapper.INSTANCE.toDto(entity);
  }

  @Transactional
  public Resource delete(String id, String deletedBy) {
    log.info("delete resource,id:{},deletedBy:{}", id, deletedBy);
    Resource resource = this.findOne(id);
    resourceRepository.deleteById(UUID.fromString(id));
    return resource;
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
