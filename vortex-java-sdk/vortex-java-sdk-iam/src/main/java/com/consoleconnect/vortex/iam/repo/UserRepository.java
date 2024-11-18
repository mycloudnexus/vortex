package com.consoleconnect.vortex.iam.repo;

import com.consoleconnect.vortex.iam.entity.UserEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Transactional;

public interface UserRepository
    extends PagingAndSortingRepository<UserEntity, UUID>, CrudRepository<UserEntity, UUID> {
  Optional<UserEntity> findOneByUserId(String userId);

  @Query(value = "select e from #{#entityName} e ")
  @Transactional(readOnly = true)
  Page<UserEntity> search(Pageable pageable);
}
