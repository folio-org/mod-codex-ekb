package org.folio.codex;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import org.folio.rest.jaxrs.model.Package;

public enum ContentType {

  AGGREGATED_FULL_TEXT(Package.Type.AGGREGATED_FULL_TEXT, "aggregatedfulltext"),
  ABSTRACT_AND_INDEX(Package.Type.ABSTRACT_AND_INDEX, "abstractandindex"),
  E_BOOK(Package.Type.E_BOOK, "ebook"),
  E_JOURNAL(Package.Type.E_JOURNAL, "ejournal"),
  PRINT(Package.Type.PRINT, "print"),
  UNKNOWN(Package.Type.UNKNOWN, "unknown"),
  ONLINE_REFERENCE(Package.Type.ONLINE_REFERENCE, "onlinereference");

  private final Package.Type codex;
  private final String rmAPI;

  private static final Map<Package.Type, ContentType> CODEX_MAP = new EnumMap<>(Package.Type.class);
  private static final Map<String, ContentType> RM_API_MAP = new HashMap<>();

  static {
    for (ContentType contentType : ContentType.values()) {
      CODEX_MAP.put(contentType.codex, contentType);
      RM_API_MAP.put(contentType.rmAPI, contentType);
    }
  }

  ContentType(Package.Type codex, String rmAPI) {
    this.codex = codex;
    this.rmAPI = rmAPI;
  }

  public static ContentType fromCodex(String codex) {
    return lookup(CODEX_MAP, Package.Type.fromValue(codex));
  }

  public static ContentType fromRMAPI(String rmAPI) {
    return lookup(RM_API_MAP, rmAPI.toLowerCase());
  }

  private static <T> ContentType lookup(Map<T, ContentType> map, T value) {
    final ContentType result = map.get(value);

    if (result == null) {
      throw new IllegalArgumentException("Unknown Resource Type: " + value);
    }

    return result;
  }

  public Package.Type getCodex() {
    return codex;
  }

  public String getRmAPI() {
    return rmAPI;
  }
}
