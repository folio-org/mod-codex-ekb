package org.folio.codex;

import java.util.HashMap;
import java.util.Map;

/**
 * @author mreno
 *
 */
enum SubType {
  UNKNOWN(-1, ""),
  PRINT(1, "Print"),
  ONLINE(2, "Online");

  private static final Map<Integer, SubType> MAP = new HashMap<>();

  static {
    for (SubType id : SubType.values()) {
      MAP.put(id.code, id);
    }
  }

  static SubType valueOf(Integer i) {
    return MAP.getOrDefault(i, UNKNOWN);
  }

  private final int code;
  private final String displayName;

  SubType(int code, String displayName) {
    this.code = code;
    this.displayName = displayName;
  }

  /**
   * @return the code
   */
  public int getCode() {
    return code;
  }

  /**
   * @return the displayName
   */
  public String getDisplayName() {
    return displayName;
  }
}
