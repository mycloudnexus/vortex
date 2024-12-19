package com.consoleconnect.vortex.iam.dto;

import com.consoleconnect.vortex.core.toolkit.DateTime;
import com.consoleconnect.vortex.iam.enums.ConnectionStrategyEnum;
import com.consoleconnect.vortex.iam.enums.OrgStatusEnum;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;

@Data
public class OrganizationMetadata {

  public static final String META_STATUS = "status";
  public static final String META_CONNECTION_ID = "connectionId";
  public static final String META_CONNECTION_STRATEGY = "strategy";
  public static final String META_CREATED_AT = "createdAt";

  private OrgStatusEnum status = OrgStatusEnum.ACTIVE;
  private ConnectionStrategyEnum strategy = ConnectionStrategyEnum.UNDEFINED;
  private String connectionId;
  private ZonedDateTime createdAt;

  public static OrganizationMetadata fromMap(Map<String, Object> map) {
    OrganizationMetadata metadata = new OrganizationMetadata();
    if (map == null) {
      map = new HashMap<>();
    }
    metadata.setStatus(
        OrgStatusEnum.valueOf((String) map.getOrDefault(META_STATUS, OrgStatusEnum.ACTIVE.name())));
    metadata.setStrategy(
        ConnectionStrategyEnum.from(
            (String)
                map.getOrDefault(
                    META_CONNECTION_STRATEGY, ConnectionStrategyEnum.UNDEFINED.name())));
    metadata.setConnectionId((String) map.get(META_CONNECTION_ID));
    if (map.containsKey(META_CREATED_AT)) {
      metadata.setCreatedAt(DateTime.of((String) map.get(META_CREATED_AT)));
    }
    return metadata;
  }

  public static Map<String, Object> toMap(OrganizationMetadata metadata) {
    if (metadata == null) {
      metadata = new OrganizationMetadata();
    }
    if (metadata.getStatus() == null) {
      metadata.setStatus(OrgStatusEnum.ACTIVE);
    }
    if (metadata.getStrategy() == null) {
      metadata.setStrategy(ConnectionStrategyEnum.UNDEFINED);
    }
    Map<String, Object> map = new HashMap<>();
    map.put(META_STATUS, metadata.getStatus().name());
    map.put(META_CONNECTION_STRATEGY, metadata.getStrategy().getValue());

    if (metadata.getConnectionId() != null) {
      map.put(META_CONNECTION_ID, metadata.getConnectionId());
    }
    if (metadata.getCreatedAt() == null) {
      map.put(META_CREATED_AT, DateTime.nowInUTCString());
    } else {
      map.put(META_CREATED_AT, metadata.getCreatedAt().toInstant().toString());
    }
    return map;
  }
}
