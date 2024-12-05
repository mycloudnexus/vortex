package com.consoleconnect.vortex.gateway.service;

import com.consoleconnect.vortex.core.exception.VortexException;
import com.consoleconnect.vortex.core.toolkit.Paging;
import com.consoleconnect.vortex.core.toolkit.PagingHelper;
import com.consoleconnect.vortex.gateway.dto.CreatePathAccessRuleRequest;
import com.consoleconnect.vortex.gateway.dto.PathAccessRule;
import com.consoleconnect.vortex.gateway.dto.UpdatePathAccessRuleRequest;
import com.consoleconnect.vortex.gateway.entity.PathAccessRuleEntity;
import com.consoleconnect.vortex.gateway.enums.AccessActionEnum;
import com.consoleconnect.vortex.gateway.mapper.PathAccessControlMapper;
import com.consoleconnect.vortex.gateway.model.GatewayProperty;
import com.consoleconnect.vortex.gateway.model.PathAccessRuleTable;
import com.consoleconnect.vortex.gateway.repo.PathAccessRuleRepository;
import com.consoleconnect.vortex.gateway.toolkit.PathMatcherToolkit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class PathAccessRuleService {

  private final PathAccessRuleRepository repo;
  private final GatewayProperty gatewayProperty;

  public Paging<PathAccessRule> search(
      String method, String path, AccessActionEnum action, int page, int size) {
    Page<PathAccessRuleEntity> data =
        repo.search(method, path, action, PagingHelper.toPageable(page, size));
    return PagingHelper.toPaging(data, PathAccessControlMapper.INSTANCE::toDto);
  }

  public PathAccessRule create(CreatePathAccessRuleRequest request, String createdBy) {
    PathAccessRuleEntity entity = PathAccessControlMapper.INSTANCE.toEntity(request);
    entity.setCreatedBy(createdBy);
    entity = repo.save(entity);
    return PathAccessControlMapper.INSTANCE.toDto(entity);
  }

  public Optional<PathAccessRule> findOneByMethodAndPath(String method, String path) {
    return repo.findOneByMethodAndPath(method, path).map(PathAccessControlMapper.INSTANCE::toDto);
  }

  public PathAccessRule findOne(String id) {
    return repo.findById(UUID.fromString(id))
        .map(PathAccessControlMapper.INSTANCE::toDto)
        .orElseThrow(() -> VortexException.notFound("not found"));
  }

  private PathAccessRuleTable getPathAccessRuleTable(List<PathAccessRule> defaultRules) {
    List<PathAccessRule> rules = new ArrayList<>();
    if (defaultRules != null) {
      rules.addAll(defaultRules);
    }
    for (PathAccessRuleEntity pathAccessRuleEntity : this.repo.findAll()) {
      rules.add(PathAccessControlMapper.INSTANCE.toDto(pathAccessRuleEntity));
    }

    return PathAccessRuleTable.build(rules);
  }

  public AccessActionEnum makeAccessDecision(String method, String path) {
    return makeAccessDecision(method, path, null);
  }

  public AccessActionEnum makeAccessDecision(
      String method, String path, List<PathAccessRule> defaultRules) {
    log.info("makeAccessDecision, method:{}, path:{}", method, path);
    PathAccessRuleTable table = getPathAccessRuleTable(defaultRules);
    table.dump();
    // any denied rules
    List<String> deniedPathPatterns = table.getPaths(method, AccessActionEnum.DENIED);
    deniedPathPatterns.forEach(System.out::println);
    Optional<String> matchedDeniedPath =
        PathMatcherToolkit.findFirstMatch(deniedPathPatterns, path);
    if (matchedDeniedPath.isPresent()) {
      log.info("Access to path {} is denied based on the rule:{}", path, matchedDeniedPath.get());
      return AccessActionEnum.DENIED;
    }
    // any allowed rules
    List<String> allowedPathPatterns = table.getPaths(method, AccessActionEnum.ALLOWED);
    allowedPathPatterns.forEach(System.out::println);
    Optional<String> matchedAllowedPath =
        PathMatcherToolkit.findFirstMatch(allowedPathPatterns, path);
    if (matchedAllowedPath.isPresent()) {
      log.info("Access to path {} is allowed based on the rule:{}", path, matchedAllowedPath.get());
      return AccessActionEnum.ALLOWED;
    }

    // default rule
    log.info("Access to path {} is {} based on the default rule", path, AccessActionEnum.UNDEFINED);
    return AccessActionEnum.UNDEFINED;
  }

  public PathAccessRule update(String id, UpdatePathAccessRuleRequest request, String updatedBy) {
    log.info("update, id:{}, updatedBy:{}", id, updatedBy);
    PathAccessRuleEntity entity =
        repo.findById(UUID.fromString(id)).orElseThrow(() -> VortexException.notFound("not found"));
    if (request.getPath() != null) {
      entity.setPath(request.getPath());
    }
    if (request.getMethod() != null) {
      entity.setMethod(request.getMethod());
    }
    if (request.getAction() != null) {
      entity.setAction(request.getAction());
    }
    entity.setUpdatedBy(updatedBy);
    entity = repo.save(entity);
    return PathAccessControlMapper.INSTANCE.toDto(entity);
  }

  public PathAccessRule delete(String id, String deletedBy) {
    log.info("delete, id:{}, deletedBy:{}", id, deletedBy);
    PathAccessRuleEntity entity =
        repo.findById(UUID.fromString(id)).orElseThrow(() -> VortexException.notFound("not found"));
    repo.delete(entity);
    return PathAccessControlMapper.INSTANCE.toDto(entity);
  }
}
