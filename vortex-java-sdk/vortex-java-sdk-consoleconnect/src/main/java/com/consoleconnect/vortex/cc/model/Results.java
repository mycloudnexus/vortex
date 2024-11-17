package com.consoleconnect.vortex.cc.model;

import java.util.List;
import lombok.Data;

@Data
public class Results<T> {
  private List<T> results;
}
