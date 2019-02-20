package org.folio.cql2rmapi.query;

import java.util.List;

public class RMAPIQueries {
  private int firstObjectIndex;
  private int limit;
  private List<String> queriesForRMAPI;

  public RMAPIQueries(int firstObjectIndex, int limit, List<String> queriesForRMAPI) {
    this.firstObjectIndex = firstObjectIndex;
    this.limit = limit;
    this.queriesForRMAPI = queriesForRMAPI;
  }

  public List<String> getRMAPIQueries() {
    return queriesForRMAPI;
  }

  public int getFirstObjectIndex() {
    return firstObjectIndex;
  }

  public int getLimit() {
    return limit;
  }
}
