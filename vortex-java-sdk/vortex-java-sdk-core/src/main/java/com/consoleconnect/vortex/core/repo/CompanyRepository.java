package com.consoleconnect.vortex.core.repo;

import com.consoleconnect.vortex.core.entity.CompanyEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

public interface CompanyRepository
    extends PagingAndSortingRepository<CompanyEntity, UUID>, CrudRepository<CompanyEntity, UUID> {
  @Query(
      value =
          "select e from #{#entityName} e "
              + " where 1 = 1 "
              + " and ( :key is null or lower(e.companyName) like %:key% ) ")
  Page<CompanyEntity> search(@Param("key") String key, Pageable pageable);

  Optional<CompanyEntity> findByCompanyName(String companyName);

  Optional<CompanyEntity> findByShortName(String shortName);
}
