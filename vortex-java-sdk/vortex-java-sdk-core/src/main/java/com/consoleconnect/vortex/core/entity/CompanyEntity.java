package com.consoleconnect.vortex.core.entity;

import com.consoleconnect.vortex.core.enums.CompanyStatusEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(
    name = "white_label_company",
    indexes = {
      @Index(name = "idx_company_company_name", columnList = "company_name", unique = true),
      @Index(name = "idx_company_short_name", columnList = "short_name", unique = true)
    })
public class CompanyEntity extends AbstractEntity {

  @Column(name = "company_name", nullable = false)
  private String companyName;

  @Column(name = "short_name", nullable = false, length = 100)
  private String shortName;

  @Enumerated(EnumType.STRING)
  @Column(name = "status")
  private CompanyStatusEnum status;
}
