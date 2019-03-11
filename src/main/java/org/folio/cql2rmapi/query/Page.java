package org.folio.cql2rmapi.query;

public class Page {
  private int offset;
  private int limit;

  public Page(int offset, int limit) {
    this.offset = offset;
    this.limit = limit;
  }

  public int getOffset() {
    return offset;
  }

  public int getLimit() {
    return limit;
  }
}
