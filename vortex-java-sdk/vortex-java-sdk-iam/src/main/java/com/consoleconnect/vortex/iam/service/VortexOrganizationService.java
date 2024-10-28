package com.consoleconnect.vortex.iam.service;

import com.consoleconnect.vortex.core.exception.VortexException;
import com.consoleconnect.vortex.core.toolkit.Paging;
import com.consoleconnect.vortex.core.toolkit.PagingHelper;
import com.consoleconnect.vortex.core.toolkit.PatternHelper;
import com.consoleconnect.vortex.iam.dto.CreateOrganizationDto;
import com.consoleconnect.vortex.iam.dto.OrganizationDto;
import com.consoleconnect.vortex.iam.entity.OrganizationEntity;
import com.consoleconnect.vortex.iam.enums.LoginTypeEnum;
import com.consoleconnect.vortex.iam.enums.OrgStatusEnum;
import com.consoleconnect.vortex.iam.enums.OrgTypeEnum;
import com.consoleconnect.vortex.iam.mapper.OrganizationMapper;
import com.consoleconnect.vortex.iam.repo.OrganizationRepository;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class VortexOrganizationService {

  private final OrganizationRepository organizationRepository;
  private final OrganizationService organizationService;

  public VortexOrganizationService(
      OrganizationRepository organizationRepository, OrganizationService organizationService) {
    this.organizationRepository = organizationRepository;
    this.organizationService = organizationService;
  }

  public Paging<OrganizationDto> search(String q, OrgTypeEnum type, int page, int size) {

    final String key = StringUtils.isBlank(q) ? null : q.toLowerCase();
    Page<OrganizationEntity> data =
        organizationRepository.search(key, type, PageRequest.of(page, size));
    return PagingHelper.toPaging(data, x -> OrganizationMapper.INSTANCE.toOrganizationDto(x));
  }

  public OrganizationDto findOne(String orgId) {
    return OrganizationMapper.INSTANCE.toOrganizationDto(getById(orgId));
  }

  public OrganizationEntity getById(String orgId) {
    return organizationRepository
        .findById(UUID.fromString(orgId))
        .orElseThrow(() -> VortexException.notFound("Organization not found."));
  }

  public OrganizationEntity getOneByName(String shortName) {
    return organizationRepository
        .findByName(shortName)
        .orElseThrow(() -> VortexException.notFound("Organization not found."));
  }

  @Transactional
  public OrganizationDto create(CreateOrganizationDto request, String createdBy) {
    log.info("creating organization: {}, requestedBy:{}", request, createdBy);
    if (request == null
        || StringUtils.isBlank(request.getDisplayName())
        || StringUtils.isBlank(request.getName())) {
      throw VortexException.badRequest("Invalid parameters.");
    }

    if (request.getDisplayName().length() > 255) {
      throw VortexException.badRequest("Display name cannot exceed 255 characters.");
    }

    if (request.getName().length() > 20) {
      throw VortexException.badRequest("Name cannot exceed 20 characters.");
    }

    if (!PatternHelper.validShortName(request.getName())) {
      throw VortexException.badRequest("Invalid name.");
    }

    if (organizationRepository.findByDisplayName(request.getDisplayName()).isPresent()) {
      throw VortexException.badRequest("Display name already exists.");
    }

    if (organizationRepository.findByName(request.getName()).isPresent()) {
      throw VortexException.badRequest("Name already exists.");
    }

    OrganizationEntity org = new OrganizationEntity();
    org.setName(request.getName());
    org.setDisplayName(request.getDisplayName());
    org.setStatus(OrgStatusEnum.ACTIVE);
    org.setType(OrgTypeEnum.CUSTOMER);
    org.setLoginType(LoginTypeEnum.USERNAME_PASSWORD);
    organizationRepository.save(org);
    organizationService.create(request, createdBy);
    return OrganizationMapper.INSTANCE.toOrganizationDto(org);
  }

  @Transactional
  public OrganizationDto update(String orgId, CreateOrganizationDto req) {
    if (req == null) {
      throw VortexException.badRequest("Payload cannot be empty.");
    }
    OrganizationEntity org = getById(orgId);

    if (StringUtils.isNotBlank(req.getDisplayName())) {

      if (req.getDisplayName().length() > 255) {
        throw VortexException.badRequest("Display name cannot exceed 255 characters.");
      }

      organizationRepository
          .findByDisplayName(req.getDisplayName())
          .ifPresent(
              c -> {
                if (!c.getId().toString().equals(orgId)) {
                  throw VortexException.badRequest("Display name already exists.");
                }
              });
      org.setDisplayName(req.getDisplayName());
    }

    if (req.getStatus() != null) {
      org.setStatus(req.getStatus());
    }
    organizationRepository.save(org);
    // organizationService.update(company.getName(), company.getCompanyName())
    return OrganizationMapper.INSTANCE.toOrganizationDto(org);
  }
}
