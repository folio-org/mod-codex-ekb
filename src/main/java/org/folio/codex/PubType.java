/**
 *
 */
package org.folio.codex;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import org.folio.rest.jaxrs.model.Instance;

/**
 * @author mreno
 *
 */
public enum PubType {
  AUDIO(Instance.Type.AUDIO, "streamingaudio"),
  AUDIO_BOOKS(Instance.Type.AUDIOBOOKS, "audiobook"),
  BOOK_SERIES(Instance.Type.BOOKSERIES, "bookseries"),
  DATABASES(Instance.Type.DATABASES, "database"),
  EBOOKS(Instance.Type.EBOOKS, "book"),
  NEWSLETTERS(Instance.Type.NEWSLETTERS, "newsletter"),
  NEWSPAPERS(Instance.Type.NEWSPAPERS, "newspaper"),
  PERIODICALS(Instance.Type.PERIODICALS, "journal"),
  PROCEEDINGS(Instance.Type.PROCEEDINGS, "proceedings"),
  REPORTS(Instance.Type.REPORTS, "report"),
  THESIS_AND_DISSERTATION(Instance.Type.THESISANDDISSERTATION, "thesisdissertation"),
  UNSPECIFIED(Instance.Type.UNSPECIFIED, "unspecified"),
  VIDEO(Instance.Type.VIDEO, "streamingvideo"),
  WEB_RESOURCES(Instance.Type.WEBRESOURCES, "website");

  private static final Map<Instance.Type, PubType> CODEX_MAP = new EnumMap<>(Instance.Type.class);
  private static final Map<String, PubType> RM_API_MAP = new HashMap<>();

  static {
    for (PubType pt : PubType.values()) {
      CODEX_MAP.put(pt.codex, pt);
      RM_API_MAP.put(pt.rmAPI, pt);
    }
  }

  public static PubType fromCodex(String codex) {
    return lookup(CODEX_MAP, Instance.Type.fromValue(codex));
  }

  public static PubType fromRMAPI(String rmAPI) {
    return lookup(RM_API_MAP, rmAPI.toLowerCase());
  }

  private static <T> PubType lookup(Map<T, PubType> map, T value) {
    final PubType result = map.get(value);

    if (result == null) {
      throw new IllegalArgumentException("Unknown Resource Type: " + value);
    }

    return result;
  }

  private final Instance.Type codex;
  private final String rmAPI;

  private PubType(Instance.Type codex, String rmAPI) {
    this.codex = codex;
    this.rmAPI = rmAPI;
  }

  /**
   * @return the codex
   */
  public Instance.Type getCodex() {
    return codex;
  }

  /**
   * @return the rmAPI
   */
  public String getRmAPI() {
    return rmAPI;
  }
}
