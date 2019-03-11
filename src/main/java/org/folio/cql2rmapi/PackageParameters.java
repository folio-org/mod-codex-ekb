package org.folio.cql2rmapi;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import javax.validation.ValidationException;

import org.folio.codex.ContentType;
import org.folio.holdingsiq.model.Sort;
import org.folio.holdingsiq.service.validator.PackageParametersValidator;

public class PackageParameters extends CommonParameters {
  private static final String NAME = "name";
  private static final String CODEX_NAME = "codex.name";
  private static final String TYPE = "type";
  private static final String CODEX_TYPE = "codex.type";
  private static final String CQL_SERVER_CHOICE = "cql.serverChoice";
  private static final Collection<String> ALLOWED_PARAMETERS =
    Arrays.asList(CODEX_NAME, NAME, TYPE, CODEX_TYPE, CQL_SERVER_CHOICE, SELECTED, SOURCE, CODEX_SOURCE);

  private String selection;
  private String searchValue;
  private String filterType;
  private final PackageParametersValidator parametersValidator = new PackageParametersValidator();

  public PackageParameters(CQLParameters cqlParameters) {
    parseCqlParameters(cqlParameters);
  }

  public String getSelection() {
    return selection;
  }

  public String getSearchValue() {
    return searchValue;
  }

  public String getFilterType() {
    return filterType;
  }

  private void parseCqlParameters(CQLParameters cqlParameters) {
    Optional<String> unsupportedParameter = cqlParameters.getParameters().keySet().stream()
      .filter(param -> !ALLOWED_PARAMETERS.contains(param) && !param.startsWith("ext."))
      .findFirst();
    if (unsupportedParameter.isPresent()) {
      throw new ValidationException("Search field or filter value " + unsupportedParameter.get() + UNSUPPORTED);
    }

    Map<String, String> parameters = cqlParameters.getParameters();

    searchValue = getCodexParameter(NAME, parameters);
    if (searchValue == null && parameters.get(CQL_SERVER_CHOICE) != null) {
      searchValue = parameters.get(CQL_SERVER_CHOICE);
    }

    String cqlSort = cqlParameters.getSort();
    if (cqlSort == null || Arrays.asList(CODEX_NAME, NAME).contains(cqlSort.toLowerCase())) {
      sortType = Sort.NAME;
    } else {
      throw new ValidationException(ERROR + "Sorting on " + cqlSort + " is unsupported.");
    }

    filterType = getCodexParameter(TYPE, parameters);
    if (filterType != null) {
      filterType = getFilterValuesByType(filterType);
    }

    checkSourceParameters(parameters);

    selection = parseSelection(parameters);

    parametersValidator.validate(selection, filterType, sortType.getValue(), searchValue);
  }

  private String getFilterValuesByType(String termNode) {
    try {
      return ContentType.fromCodex(termNode).getRmAPI();
    } catch (final IllegalArgumentException e) {
      throw new ValidationException(ERROR + "Filtering on type " + termNode + UNSUPPORTED);
    }
  }
}
