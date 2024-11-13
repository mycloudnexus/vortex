package com.consoleconnect.vortex.iam.dto;

import java.util.List;
import lombok.Data;

@Data
public class DownstreamMember {
  private String id;
  private String username;
  private String name;
  private String email;
  private List<DownstreamRole> roles;
}
