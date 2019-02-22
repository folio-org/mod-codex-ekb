package org.folio.converter.hld2cdx;

import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;

import org.folio.rest.jaxrs.model.Coverage;
import org.folio.rmapi.model.CoverageDates;

public class CoverageConverter implements Converter<CoverageDates, Coverage> {

  @Override
  public Coverage convert(@NonNull CoverageDates source) {
    return new Coverage()
      .withBeginCoverage(source.beginCoverage)
      .withEndCoverage(source.endCoverage);
  }

}
