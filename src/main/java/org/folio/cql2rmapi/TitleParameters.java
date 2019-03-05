package org.folio.cql2rmapi;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import javax.validation.ValidationException;

import org.folio.codex.PubType;
import org.folio.holdingsiq.model.FilterQuery;
import org.folio.holdingsiq.model.Sort;
import org.folio.holdingsiq.service.validator.TitleParametersValidator;

public class TitleParameters extends CommonParameters {
  private static final String RESOURCE_TYPE = "resourceType";
  private static final String CODEX_RESOURCE_TYPE = "codex.resourceType";
  private static final String CQL_SERVER_CHOICE = "cql.serverChoice";
  private static final String TITLE = "title";
  private static final String CODEX_TITLE = "codex.title";
  private static final String IDENTIFIER = "identifier";
  private static final String CODEX_IDENTIFIER = "codex.identifier";
  private static final String SUBJECT = "subject";
  private static final String CODEX_SUBJECT = "codex.subject";
  private static final String PUBLISHER = "publisher";
  private static final String CODEX_PUBLISHER = "codex.publisher";
  private static final String ID = "id";
  private static final String CODEX_ID = "codex.id";

  private static final Collection<String> ALLOWED_PARAMETERS =
    Arrays.asList(SOURCE, CODEX_SOURCE, SELECTED, RESOURCE_TYPE, CODEX_RESOURCE_TYPE, CQL_SERVER_CHOICE,
      TITLE, CODEX_TITLE, IDENTIFIER, CODEX_IDENTIFIER, SUBJECT, CODEX_SUBJECT, PUBLISHER, CODEX_PUBLISHER, ID, CODEX_ID);

  private FilterQuery filterQuery;

  private final TitleParametersValidator validator = new TitleParametersValidator();

  public TitleParameters(CQLParameters cqlParameters) {
    parseCqlParameters(cqlParameters);
  }

  public FilterQuery getFilterQuery() {
    return filterQuery;
  }

  private void parseCqlParameters(CQLParameters cqlParameters) {
    FilterQuery.FilterQueryBuilder builder = FilterQuery.builder();
    Optional<String> unsupportedParameter = cqlParameters.getParameters().keySet().stream()
      .filter(param -> !ALLOWED_PARAMETERS.contains(param) && !param.startsWith("ext."))
      .findFirst();
    if (unsupportedParameter.isPresent()) {
      throw new ValidationException("Search field or filter value " + unsupportedParameter.get() + UNSUPPORTED);
    }

    Map<String, String> parameters = cqlParameters.getParameters();
    String name = getCodexParameter(TITLE, parameters);
    if (name == null) {
      name = parameters.get(CQL_SERVER_CHOICE);
    }

    String cqlSort = cqlParameters.getSort();
    if (cqlSort == null || Arrays.asList(TITLE, "codex." + TITLE).contains(cqlSort.toLowerCase())) {
      sortType = Sort.NAME;
    } else {
      throw new ValidationException(ERROR + "Sorting on " + cqlSort + " is unsupported.");
    }

    String type = null;
    String codexType = getCodexParameter(RESOURCE_TYPE, parameters);
    if(codexType != null){
      type = getFilterValuesByType(codexType);
    }

    filterQuery = builder
      .name(name)
      .isxn(getCodexParameter(IDENTIFIER, parameters))
      .selected(parseSelection(parameters))
      .type(type)
      .publisher(getCodexParameter(PUBLISHER, parameters))
      .subject(getCodexParameter(SUBJECT, parameters))
      .build();

    checkSourceParameters(parameters);
    validator.validate(filterQuery, sortType.getValue());
  }

  private String getFilterValuesByType(String termNode) {
    try {
      return PubType.fromCodex(termNode).getRmAPI();
    } catch (final IllegalArgumentException e) {
      throw new ValidationException(ERROR + "Filtering on type " + termNode + UNSUPPORTED);
    }
  }
}
