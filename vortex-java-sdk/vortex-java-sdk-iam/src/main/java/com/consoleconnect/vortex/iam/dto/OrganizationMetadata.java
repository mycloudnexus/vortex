package com.consoleconnect.vortex.iam.dto;

import com.consoleconnect.vortex.iam.enums.ConnectionStrategyEnum;
import com.consoleconnect.vortex.iam.enums.OrgStatusEnum;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;

@Data
public class OrganizationMetadata {

  public static final String META_STATUS = "status";
  public static final String META_CONNECTION_ID = "connectionId";
  public static final String META_CONNECTION_STRATEGY = "strategy";

  private OrgStatusEnum status = OrgStatusEnum.ACTIVE;
  private ConnectionStrategyEnum strategy = ConnectionStrategyEnum.UNDEFINED;
  private String connectionId;

  public static OrganizationMetadata fromMap(Map<String, Object> map) {
    OrganizationMetadata metadata = new OrganizationMetadata();
    if (map == null) {
      map = new HashMap<>();
    }
    metadata.setStatus(
        OrgStatusEnum.valueOf((String) map.getOrDefault(META_STATUS, OrgStatusEnum.ACTIVE.name())));
    metadata.setStrategy(
        ConnectionStrategyEnum.valueOf(
            (String)
                map.getOrDefault(
                    META_CONNECTION_STRATEGY, ConnectionStrategyEnum.UNDEFINED.name())));
    metadata.setConnectionId((String) map.get(META_CONNECTION_ID));

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
    map.put(META_CONNECTION_STRATEGY, metadata.getStrategy().name());

    if (metadata.getConnectionId() != null) {
      map.put(META_CONNECTION_ID, metadata.getConnectionId());
    }
    return map;
  }
}
