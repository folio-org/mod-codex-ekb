package org.folio.converter.hld2cdx;

import org.folio.holdingsiq.model.CoverageDates;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;

import org.folio.rest.jaxrs.model.Coverage;

public class CoverageConverter implements Converter<CoverageDates, Coverage> {

  @Override
  public Coverage convert(@NonNull CoverageDates source) {
    return new Coverage()
      .withBeginCoverage(source.getBeginCoverage())
      .withEndCoverage(source.getEndCoverage());
  }

}
