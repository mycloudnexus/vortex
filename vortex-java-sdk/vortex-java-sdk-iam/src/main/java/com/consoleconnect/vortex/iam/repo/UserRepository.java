package com.consoleconnect.vortex.iam.repo;

import com.consoleconnect.vortex.iam.entity.UserEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface UserRepository
    extends PagingAndSortingRepository<UserEntity, UUID>, CrudRepository<UserEntity, UUID> {
  Optional<UserEntity> findOneByUserId(String userId);
}
