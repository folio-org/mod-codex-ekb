package org.folio.converter.hld2cdx;

import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;

import org.folio.rest.jaxrs.model.Contributor;

public class ContributorConverter implements Converter<org.folio.holdingsiq.model.Contributor, Contributor> {

  @Override
  public Contributor convert(@NonNull org.folio.holdingsiq.model.Contributor source) {
    return new Contributor()
      .withName(source.getTitleContributor())
      .withType(source.getType());
  }

}
