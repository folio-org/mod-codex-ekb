package org.folio.converter.hld2cdx;

import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;

import org.folio.rest.jaxrs.model.Subject;

public class SubjectConverter implements Converter<org.folio.holdingsiq.model.Subject, Subject> {

  @Override
  public Subject convert(@NonNull org.folio.holdingsiq.model.Subject source) {
    return new Subject()
      .withName(source.getValue())
      .withType(source.getType());
  }
}
