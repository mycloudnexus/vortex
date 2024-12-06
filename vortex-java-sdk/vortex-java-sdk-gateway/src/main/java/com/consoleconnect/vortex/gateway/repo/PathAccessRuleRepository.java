package com.consoleconnect.vortex.gateway.repo;

import com.consoleconnect.vortex.gateway.entity.PathAccessRuleEntity;
import com.consoleconnect.vortex.gateway.enums.AccessActionEnum;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface PathAccessRuleRepository
    extends PagingAndSortingRepository<PathAccessRuleEntity, UUID>,
        CrudRepository<PathAccessRuleEntity, UUID> {

  @Query(
      value =
          "select e from #{#entityName} e "
              + " where ( (:method) is null or  e.method = :method )"
              + " and ( (:path) is null or  LOWER(e.path) like %:path% )"
              + " and ( (:action) is null or  e.action = :action )")
  @Transactional(readOnly = true)
  Page<PathAccessRuleEntity> search(
      @Param("method") String method,
      @Param("path") String path,
      @Param("action") AccessActionEnum action,
      Pageable pageable);

  Optional<PathAccessRuleEntity> findOneByMethodAndPath(String method, String path);
}
