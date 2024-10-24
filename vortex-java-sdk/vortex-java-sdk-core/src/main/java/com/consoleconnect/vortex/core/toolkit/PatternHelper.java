package com.consoleconnect.vortex.core.toolkit;

import java.util.regex.Pattern;

public class PatternHelper {
  private PatternHelper() {}

  private static final String SHORTNAME_REGEX = "^[a-z0-9][a-z0-9_-]*$";

  public static boolean validShortName(String str) {
    Pattern p = Pattern.compile(SHORTNAME_REGEX);
    return p.matcher(str).matches();
  }

  public static void main(String[] args) {
    System.out.println(validShortName("my-string-12_3"));
    System.out.println(validShortName("4my-string-12_3"));
    System.out.println(validShortName("_my-string-12_3"));
    System.out.println(validShortName("-my-string-12_3"));
    System.out.println(validShortName("My-string-12_3"));
  }
}
