package org.folio.cql2rmapi.query;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class PaginationCalculatorTest {

  private PaginationCalculator calculator = new PaginationCalculator();

  @Test
  public void shouldReturnOnePageWhenLimitSmallerThanRMAPILimit() {
    PaginationInfo info = calculator.getPagination(0, 50);

    assertEquals(0, info.getFirstObjectIndex());
    assertEquals(50, info.getLimit());

    assertEquals(1, info.getPages().size());
    assertEquals(1, info.getPages().get(0).getOffset());
    assertEquals(50, info.getPages().get(0).getLimit());
  }

  @Test
  public void shouldReturnMultiplePagesWhenLimitBiggerThanRMAPILimit() {
    PaginationInfo info = calculator.getPagination(0, 143);

    assertEquals(2, info.getPages().size());
    assertEquals(1, info.getPages().get(0).getOffset());
    assertEquals(100, info.getPages().get(0).getLimit());
    assertEquals(2, info.getPages().get(1).getOffset());
    assertEquals(100, info.getPages().get(1).getLimit());
  }

  @Test
  public void shouldReturnOnePageWhenLimitEqualToRMAPILimit() {
    PaginationInfo info = calculator.getPagination(0, 100);

    assertEquals(1, info.getPages().size());
    assertEquals(1, info.getPages().get(0).getOffset());
    assertEquals(100, info.getPages().get(0).getLimit());
  }
}
