package org.folio.converter.hld2cdx;

import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;

import org.folio.rest.jaxrs.model.Contributor;

public class ContributorConverter implements Converter<org.folio.rmapi.model.Contributor, Contributor> {

  @Override
  public Contributor convert(@NonNull org.folio.rmapi.model.Contributor source) {
    return new Contributor()
      .withName(source.titleContributor)
      .withType(source.type);
  }

}
