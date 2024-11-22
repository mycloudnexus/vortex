package com.consoleconnect.vortex.iam.entity;

import com.consoleconnect.vortex.core.entity.AbstractEntity;
import com.consoleconnect.vortex.iam.enums.UserStatusEnum;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

@Entity
@Getter
@Setter
@Table(
    name = "votex_user",
    indexes = {@Index(name = "vortex_user_idx_userId", columnList = "user_id", unique = true)})
public class UserEntity extends AbstractEntity {

  @Column(name = "user_id", nullable = false)
  private String userId;

  @Enumerated(EnumType.STRING)
  @Column(name = "status")
  private UserStatusEnum status;

  @Type(JsonType.class)
  @Column(name = "roles", columnDefinition = "jsonb")
  private List<String> roles = new ArrayList<>();

  @Type(JsonType.class)
  @Column(name = "metadata", columnDefinition = "jsonb")
  private Map<String, Object> metadata = new HashMap<>();
}
