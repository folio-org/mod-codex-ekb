package org.folio.cql2rmapi.query;

import java.util.ArrayList;
import java.util.List;

public class PaginationCalculator {
  private static final int RM_API_MAX_COUNT = 100;

  public PaginationInfo getPagination(int offset, int limit) {
    List<Page> pages = new ArrayList<>();
    int rmAPILimit = Math.min(limit, RM_API_MAX_COUNT);
    int pageOffsetRMAPI = computePageOffsetForRMAPI(offset, rmAPILimit);
    pages.add(new Page(pageOffsetRMAPI, rmAPILimit));
    while (checkIfSecondQueryIsNeeded(offset, rmAPILimit, pageOffsetRMAPI)) {
      pages.add(new Page(++pageOffsetRMAPI, rmAPILimit));
    }
    return new PaginationInfo(pages, offset % rmAPILimit, limit);
  }

  private int computePageOffsetForRMAPI(int offset, int limit) {
    final float value = offset / (float) limit;
    final double floor = Math.floor(value);
    final double pageOffset = floor + 1;
    return (int) pageOffset;
  }

  private boolean checkIfSecondQueryIsNeeded(int offset, int limit, int pageOffsetRMAPI) {
    return (offset + limit) > (pageOffsetRMAPI * limit);
  }
}
