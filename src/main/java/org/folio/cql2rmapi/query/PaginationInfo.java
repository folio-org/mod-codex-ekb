package org.folio.cql2rmapi.query;

import java.util.List;

/**
 * Contains pagination information for getting objects from HoldingsIQ
 */
public class PaginationInfo {
  /**
   * List of pages to get
   */
  private List<Page> pages;
  /**
   * Index of the first object that should be returned.
   * Not all objects returned from HoldingsIQ need to be returned from mod-codex-ekb, the beginning of first page and ending of last page
   * may contain some unnecessary objects, so to know which objects to return we need to store firstObjectIndex and limit.
   */
  private int firstObjectIndex;
  /**
   * Amount of objects to return starting from firstObjectIndex
   */
  private int limit;

  public PaginationInfo(List<Page> pages, int firstObjectIndex, int limit) {
    this.pages = pages;
    this.firstObjectIndex = firstObjectIndex;
    this.limit = limit;
  }

  public List<Page> getPages() {
    return pages;
  }

  public int getFirstObjectIndex() {
    return firstObjectIndex;
  }

  public int getLimit() {
    return limit;
  }
}
