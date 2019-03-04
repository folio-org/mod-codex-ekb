package org.folio.converter.hld2cdx;

import static java.util.stream.Collectors.toSet;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import java.util.Objects;
import java.util.Set;

import org.folio.holdingsiq.model.Title;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;

import org.folio.codex.PubType;
import org.folio.rest.jaxrs.model.Contributor;
import org.folio.rest.jaxrs.model.Identifier;
import org.folio.rest.jaxrs.model.Instance;
import org.folio.rest.jaxrs.model.Subject;

public class TitleConverter implements Converter<Title, Instance> {

  private static final String E_RESOURCE_FORMAT = "Electronic Resource";
  private static final String E_RESOURCE_SOURCE = "kb";

  private Converter<org.folio.holdingsiq.model.Identifier, Identifier> identifierConverter;
  private Converter<org.folio.holdingsiq.model.Contributor, Contributor> contributorConverter;
  private Converter<org.folio.holdingsiq.model.Subject, Subject> subjectConverter;


  public TitleConverter(Converter<org.folio.holdingsiq.model.Identifier, Identifier> identifierConverter,
                        Converter<org.folio.holdingsiq.model.Contributor, Contributor> contributorConverter,
                        Converter<org.folio.holdingsiq.model.Subject, Subject> subjectConverter) {
    this.identifierConverter = identifierConverter;
    this.contributorConverter = contributorConverter;
    this.subjectConverter = subjectConverter;
  }

  @Override
  public Instance convert(@NonNull Title source) {
    final Instance codexInstance = new Instance();

    codexInstance.setId(Integer.toString(source.getTitleId()));
    codexInstance.setTitle(source.getTitleName());
    codexInstance.setPublisher(source.getPublisherName());
    codexInstance.setType(PubType.fromRMAPI(source.getPubType()).getCodex());
    codexInstance.setFormat(E_RESOURCE_FORMAT);
    codexInstance.setSource(E_RESOURCE_SOURCE);
    codexInstance.setVersion(source.getEdition());

    if (isNotEmpty(source.getIdentifiersList())) {
      final Set<Identifier> identifiers = source.getIdentifiersList().stream()
        .map(identifierConverter::convert)
        .filter(Objects::nonNull)
        .collect(toSet());

      if (!identifiers.isEmpty()) {
        codexInstance.setIdentifier(identifiers);
      }
    }

    if (isNotEmpty(source.getContributorsList())) {
      codexInstance.setContributor(source.getContributorsList().stream()
        .map(contributorConverter::convert)
        .collect(toSet()));
    }

    if (isNotEmpty(source.getSubjectsList())) {
      codexInstance.setSubject(source.getSubjectsList().stream()
        .map(subjectConverter::convert)
        .collect(toSet()));
    }

    return codexInstance;
  }

}
