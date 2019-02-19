package org.folio.cql2rmapi;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.folio.codex.ContentType;

import com.google.common.collect.ImmutableMap;

public class PackageParameters extends CommonParameters {
  private static final String NAME = "name";
  private static final String CODEX_NAME = "codex.name";
  private static final String TYPE = "type";
  private static final String CODEX_TYPE = "codex.type";
  private static final String SOURCE = "source";
  private static final String CODEX_SOURCE = "codex.source";
  private static final String CQL_SERVER_CHOICE = "cql.serverChoice";
  private static final String RM_API_PACKAGE = "packagename";
  private static final String SELECTED = "ext.selected";
  private static final Map<String, String> FILTER_SELECTED_MAPPING =
    ImmutableMap.of(
      "all", "all",
      "true", "selected",
      "false", "notselected"
    );
  private static final Collection<String> ALLOWED_PARAMETERS =
    Arrays.asList(CODEX_NAME, NAME, TYPE, CODEX_TYPE, CQL_SERVER_CHOICE, SELECTED, SOURCE, CODEX_SOURCE);

  private String filterValue;
  private String selection;

  public PackageParameters(String searchValue, String filterValue, String sortType, String selection) {
    this.searchValue = searchValue;
    this.filterValue = filterValue;
    this.sortType = sortType;
    this.selection = selection;
  }

  public PackageParameters(CQLParameters cqlParameters) throws QueryValidationException {
    parseCqlParameters(cqlParameters);
  }

  public String getFilterValue() {
    return filterValue;
  }

  public String getSelection() {
    return selection;
  }

  private void parseCqlParameters(CQLParameters cqlParameters) throws QueryValidationException {
    Optional<String> unsupportedParameter = cqlParameters.getParameters().keySet().stream()
      .filter(param -> !ALLOWED_PARAMETERS.contains(param) && !param.startsWith("ext."))
      .findFirst();
    if (unsupportedParameter.isPresent()) {
      throw new QueryValidationException("Search field or filter value " + unsupportedParameter.get() + UNSUPPORTED);
    }

    Map<String, String> parameters = cqlParameters.getParameters();

    List<String> searchParameters = intersection(parameters.keySet(), Arrays.asList(CODEX_NAME, NAME, CQL_SERVER_CHOICE));
    if (searchParameters.size() > 1) {
      throw new QueryValidationException(ERROR + "Search on multiple fields" + UNSUPPORTED);
    }
    if (searchParameters.isEmpty()) {
      throw new QueryValidationException(ERROR + "Invalid query format, unsupported search parameters");
    }
    searchValue = parameters.get(searchParameters.get(0));


    String cqlSort = cqlParameters.getSort();
    if (cqlSort == null || Arrays.asList(CODEX_NAME, NAME).contains(cqlSort.toLowerCase())) {
      sortType = RM_API_PACKAGE;
    } else {
      throw new QueryValidationException(ERROR + "Sorting on " + cqlSort + " is unsupported.");
    }

    List<String> filterTypes = intersection(parameters.keySet(), Arrays.asList(TYPE, CODEX_TYPE));
    if (filterTypes.size() > 1) {
      throw new QueryValidationException(ERROR + "Filtering on multiple types " + UNSUPPORTED);
    }
    if (!filterTypes.isEmpty()) {
      filterValue = getFilterValuesByType(parameters.get(filterTypes.get(0)));
    }

    List<String> sourceParameters = intersection(parameters.keySet(), Arrays.asList(CODEX_SOURCE, SOURCE));
    if (!sourceParameters.isEmpty()) {
      checkSource(parameters.get(sourceParameters.get(0)));
    }

    if (parameters.get(SELECTED) != null) {
      selection = FILTER_SELECTED_MAPPING.get(parameters.get(SELECTED).toLowerCase());
      if (selection == null) {
        throw new QueryValidationException(ERROR + "Selected value " + parameters.get(SELECTED) + UNSUPPORTED);
      }
    }
  }

  private String getFilterValuesByType(String termNode) throws QueryValidationException {
    try {
      return ContentType.fromCodex(termNode).getRmAPI();
    } catch (final IllegalArgumentException e) {
      throw new QueryValidationException(ERROR + "Filtering on type " + termNode + UNSUPPORTED);
    }
  }
}
