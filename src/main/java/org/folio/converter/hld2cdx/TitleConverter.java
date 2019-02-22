package org.folio.converter.hld2cdx;

import static java.util.stream.Collectors.toSet;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import java.util.Objects;
import java.util.Set;

import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;

import org.folio.codex.PubType;
import org.folio.rest.jaxrs.model.Contributor;
import org.folio.rest.jaxrs.model.Identifier;
import org.folio.rest.jaxrs.model.Instance;
import org.folio.rest.jaxrs.model.Subject;
import org.folio.rmapi.model.Title;

public class TitleConverter implements Converter<Title, Instance> {

  private static final String E_RESOURCE_FORMAT = "Electronic Resource";
  private static final String E_RESOURCE_SOURCE = "kb";

  private Converter<org.folio.rmapi.model.Identifier, Identifier> identifierConverter;
  private Converter<org.folio.rmapi.model.Contributor, Contributor> contributorConverter;
  private Converter<org.folio.rmapi.model.Subject, Subject> subjectConverter;


  public TitleConverter(Converter<org.folio.rmapi.model.Identifier, Identifier> identifierConverter,
                        Converter<org.folio.rmapi.model.Contributor, Contributor> contributorConverter,
                        Converter<org.folio.rmapi.model.Subject, Subject> subjectConverter) {
    this.identifierConverter = identifierConverter;
    this.contributorConverter = contributorConverter;
    this.subjectConverter = subjectConverter;
  }

  @Override
  public Instance convert(@NonNull Title source) {
    final Instance codexInstance = new Instance();

    codexInstance.setId(Integer.toString(source.titleId));
    codexInstance.setTitle(source.titleName);
    codexInstance.setPublisher(source.publisherName);
    codexInstance.setType(PubType.fromRMAPI(source.pubType).getCodex());
    codexInstance.setFormat(E_RESOURCE_FORMAT);
    codexInstance.setSource(E_RESOURCE_SOURCE);
    codexInstance.setVersion(source.edition);

    if (isNotEmpty(source.identifiersList)) {
      final Set<Identifier> identifiers = source.identifiersList.stream()
        .map(identifierConverter::convert)
        .filter(Objects::nonNull)
        .collect(toSet());

      if (!identifiers.isEmpty()) {
        codexInstance.setIdentifier(identifiers);
      }
    }

    if (isNotEmpty(source.contributorsList)) {
      codexInstance.setContributor(source.contributorsList.stream()
        .map(contributorConverter::convert)
        .collect(toSet()));
    }

    if (isNotEmpty(source.subjectsList)) {
      codexInstance.setSubject(source.subjectsList.stream()
        .map(subjectConverter::convert)
        .collect(toSet()));
    }

    return codexInstance;
  }

}
