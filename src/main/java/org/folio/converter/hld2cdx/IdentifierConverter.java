package org.folio.converter.hld2cdx;

import org.folio.codex.IdentifierSubType;
import org.folio.codex.IdentifierType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;

import org.folio.rest.jaxrs.model.Identifier;

public class IdentifierConverter implements Converter<org.folio.holdingsiq.model.Identifier, Identifier> {

  @Override
  public Identifier convert(@NonNull org.folio.holdingsiq.model.Identifier source) {
    final IdentifierType type = IdentifierType.valueOf(source.type);
    final IdentifierSubType subType = IdentifierSubType.valueOf(source.subtype);

    final Identifier codexIdentifier;
    if (type != IdentifierType.UNKNOWN) {
      String codexType = type.getDisplayName();

      if (subType != IdentifierSubType.UNKNOWN) {
        codexType += '(' + subType.getDisplayName() + ')';
      }

      codexIdentifier = new Identifier()
        .withType(codexType)
        .withValue(source.id);
    } else {
      codexIdentifier = null;
    }

    return codexIdentifier;
  }

}
