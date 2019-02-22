package org.folio.codex;

import java.util.HashMap;
import java.util.Map;

/**
 * @author mreno
 *
 */
public enum IdentifierType {
  UNKNOWN(-1, ""),
  ISSN(0, "ISSN"),
  ISBN(1, "ISBN"),
  ZDBID(6, "ZDBID");

  private static final Map<Integer, IdentifierType> MAP = new HashMap<>();

  static {
    for (IdentifierType id : IdentifierType.values()) {
      MAP.put(id.code, id);
    }
  }

  public static IdentifierType valueOf(Integer i) {
    return MAP.getOrDefault(i, UNKNOWN);
  }

  private final int code;
  private final String displayName;

  IdentifierType(int code, String displayName) {
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
