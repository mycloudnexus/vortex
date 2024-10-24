package com.consoleconnect.vortex.core.service;

import com.consoleconnect.vortex.auth.service.OrganizationService;
import com.consoleconnect.vortex.core.entity.CompanyEntity;
import com.consoleconnect.vortex.core.enums.CompanyStatusEnum;
import com.consoleconnect.vortex.core.exception.VortexException;
import com.consoleconnect.vortex.core.model.req.CompanyDto;
import com.consoleconnect.vortex.core.repo.CompanyRepository;
import com.consoleconnect.vortex.core.toolkit.Paging;
import com.consoleconnect.vortex.core.toolkit.PagingHelper;
import com.consoleconnect.vortex.core.toolkit.PatternHelper;
import java.util.Optional;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompanyService {

  private final CompanyRepository companyRepository;
  private final OrganizationService organizationService;

  public CompanyService(
      CompanyRepository companyRepository, OrganizationService organizationService) {
    this.companyRepository = companyRepository;
    this.organizationService = organizationService;
  }

  public Paging<CompanyEntity> search(String q, int page, int size) {

    final String key = StringUtils.isBlank(q) ? null : q.toLowerCase();
    Page<CompanyEntity> data = companyRepository.search(key, PageRequest.of(page, size));
    return PagingHelper.toPaging(data, x -> x);
  }

  public CompanyEntity getOne(String companyId) {

    return companyRepository
        .findById(UUID.fromString(companyId))
        .orElseThrow(() -> VortexException.notFound("Company not found."));
  }

  public CompanyEntity getOneByShortName(String shortName) {
    return companyRepository
        .findByShortName(shortName)
        .orElseThrow(() -> VortexException.notFound("Company not found."));
  }

  @Transactional
  public CompanyEntity create(CompanyDto companyDto) {
    if (companyDto == null
        || StringUtils.isBlank(companyDto.getCompanyName())
        || StringUtils.isBlank(companyDto.getShortName())) {
      throw VortexException.badRequest("Invalid parameters.");
    }

    if (companyDto.getCompanyName().length() > 255) {
      throw VortexException.badRequest("companyName cannot exceed 255 characters.");
    }

    if (companyDto.getShortName().length() > 20) {
      throw VortexException.badRequest("shortName cannot exceed 20 characters.");
    }

    if (!PatternHelper.validShortName(companyDto.getShortName())) {
      throw VortexException.badRequest("Invalid shortName.");
    }

    if (companyRepository.findByCompanyName(companyDto.getCompanyName()).isPresent()) {
      throw VortexException.badRequest("Company name already exists.");
    }

    if (companyRepository.findByShortName(companyDto.getShortName()).isPresent()) {
      throw VortexException.badRequest("shortName already exists.");
    }

    CompanyEntity company = new CompanyEntity();
    company.setCompanyName(companyDto.getCompanyName());
    company.setShortName(companyDto.getShortName());
    company.setStatus(CompanyStatusEnum.ACTIVE);
    companyRepository.save(company);
    organizationService.create(company.getShortName(), company.getCompanyName());
    return company;
  }

  @Transactional
  public CompanyEntity update(String companyId, CompanyDto companyDto) {
    if (companyDto == null) {
      throw VortexException.badRequest("Payload cannot be empty.");
    }
    CompanyEntity company = getOne(companyId);

    if (StringUtils.isNotBlank(companyDto.getCompanyName())) {

      if (companyDto.getCompanyName().length() > 255) {
        throw VortexException.badRequest("companyName cannot exceed 255 characters.");
      }

      companyRepository
          .findByCompanyName(companyDto.getCompanyName())
          .ifPresent(
              c -> {
                if (!c.getId().toString().equals(companyId)) {
                  throw VortexException.badRequest("Company name already exists.");
                }
              });
      company.setCompanyName(companyDto.getCompanyName());
    }

    if (companyDto.getStatus() != null) {
      company.setStatus(companyDto.getStatus());
    }
    companyRepository.save(company);
    organizationService.update(company.getShortName(), company.getCompanyName());
    return company;
  }

  public Boolean delete(String companyId) {
    Optional<CompanyEntity> companyEntity = companyRepository.findById(UUID.fromString(companyId));
    if (companyEntity.isPresent()) {
      companyRepository.deleteById(UUID.fromString(companyId));
      organizationService.delete(companyEntity.get().getShortName());
      return Boolean.TRUE;
    }
    return Boolean.FALSE;
  }
}
