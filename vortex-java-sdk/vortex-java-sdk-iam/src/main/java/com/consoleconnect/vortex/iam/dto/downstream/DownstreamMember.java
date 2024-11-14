package com.consoleconnect.vortex.iam.dto.downstream;

import java.util.List;
import lombok.Data;

@Data
public class DownstreamMember extends BaseUserInfo {
  private List<DownstreamRole> roles;
}
