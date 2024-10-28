package com.consoleconnect.vortex.iam.repo;

import com.consoleconnect.vortex.core.enums.OrgTypeEnum;
import com.consoleconnect.vortex.iam.entity.OrganizationEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

public interface OrganizationRepository
    extends PagingAndSortingRepository<OrganizationEntity, UUID>,
        CrudRepository<OrganizationEntity, UUID> {
  @Query(
      value =
          "select e from #{#entityName} e "
              + " where 1 = 1 "
              + " and ( :key is null or lower(e.displayName) like %:key% ) "
              + " and ( :type is null or e.type =:type ) ")
  Page<OrganizationEntity> search(
      @Param("key") String key, @Param("type") OrgTypeEnum type, Pageable pageable);

  Optional<OrganizationEntity> findByDisplayName(String displayName);

  Optional<OrganizationEntity> findByName(String name);
}
