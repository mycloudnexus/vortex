package com.consoleconnect.vortex.iam.service;

import com.consoleconnect.vortex.core.entity.CompanyEntity;
import com.consoleconnect.vortex.core.enums.CompanyStatusEnum;
import com.consoleconnect.vortex.core.repo.CompanyRepository;
import jakarta.annotation.PostConstruct;
import java.time.ZonedDateTime;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class InitService {
  private final CompanyRepository companyRepository;

  @Value("${app.reseller-company}")
  private String resellerCompany;

  public InitService(CompanyRepository companyRepository) {
    this.companyRepository = companyRepository;
  }

  @PostConstruct
  public void init() {
    initResellerCompany();
  }

  private void initResellerCompany() {
    Optional<CompanyEntity> optionalCompanyEntity = companyRepository.findByShortName("master");
    log.info(
        "initResellerCompany.result={}, resellerCompany={}",
        optionalCompanyEntity.isPresent(),
        resellerCompany);
    if (optionalCompanyEntity.isEmpty()) {
      CompanyEntity company = new CompanyEntity();
      company.setCompanyName(resellerCompany);
      company.setStatus(CompanyStatusEnum.ACTIVE);
      company.setShortName("master");
      company.setCompanyName(resellerCompany);
      company.setCreatedAt(ZonedDateTime.now());
      company.setUpdatedAt(ZonedDateTime.now());
      companyRepository.save(company);
    }
  }
}
