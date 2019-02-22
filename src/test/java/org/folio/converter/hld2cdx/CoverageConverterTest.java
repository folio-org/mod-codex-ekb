package org.folio.converter.hld2cdx;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

import org.folio.rest.jaxrs.model.Coverage;
import org.folio.rmapi.model.CoverageDates;

public class CoverageConverterTest {

  private static final String BEGIN_COVERAGE = "2019-01-01";
  private static final String END_COVERAGE = "2019-02-01";

  private CoverageConverter converter;

  @Before
  public void setUp() {
    converter = new CoverageConverter();
  }

  @Test
  public void shouldCopyDates() {
    Coverage converted = converter.convert(coverage(BEGIN_COVERAGE, END_COVERAGE));

    assertNotNull(converted);
    assertEquals(BEGIN_COVERAGE, converted.getBeginCoverage());
    assertEquals(END_COVERAGE, converted.getEndCoverage());
  }

  @Test(expected = NullPointerException.class)
  public void shouldThrowNPEIfInputIsNull() {
    converter.convert(null);
  }

  @Test
  public void shouldAcceptNullDates() {
    Coverage converted = converter.convert(coverage(null, null));

    assertNotNull(converted);
    assertNull(converted.getBeginCoverage());
    assertNull(converted.getEndCoverage());
  }

  private CoverageDates coverage(String begin, String end) {
    CoverageDates input = new CoverageDates();
    input.beginCoverage = begin;
    input.endCoverage = end;
    return input;
  }
}
