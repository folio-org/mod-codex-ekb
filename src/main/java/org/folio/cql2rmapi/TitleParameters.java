package org.folio.cql2rmapi;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.folio.codex.PubType;

import com.google.common.collect.ImmutableMap;

public class TitleParameters extends CommonParameters {
  private static final String RESOURCE_TYPE = "resourceType";
  private static final String CODEX_RESOURCE_TYPE = "codex.resourceType";
  private static final String CQL_SERVER_CHOICE = "cql.serverChoice";
  private static final String RM_API_TITLE = "titlename";
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

  private static final Map<String, String> SEARCH_FIELDS_TO_RMAPI = ImmutableMap.<String, String>builder()
    .put(IDENTIFIER, "isxn")
    .put(CODEX_IDENTIFIER, "isxn")
    .put(PUBLISHER, PUBLISHER)
    .put(CODEX_PUBLISHER, PUBLISHER)
    .put(SUBJECT, SUBJECT)
    .put(CODEX_SUBJECT, SUBJECT)
    .put(ID, SUBJECT)
    .put(CODEX_ID, SUBJECT)
    .put(TITLE, RM_API_TITLE)
    .put(CODEX_TITLE, RM_API_TITLE)
    // Search defaults to title. This is the default search supported by RMAPI
    .put(CQL_SERVER_CHOICE, RM_API_TITLE).build();
  private String searchField;
  private String filterValue;
  private String selection;

  public TitleParameters(String searchField, String searchValue, String filterValue, String sortType, String selection) {
    this.searchField = searchField;
    this.searchValue = searchValue;
    this.filterValue = filterValue;
    this.sortType = sortType;
    this.selection = selection;
  }

  public TitleParameters(CQLParameters cqlParameters) throws QueryValidationException {
    parseCqlParameters(cqlParameters);
  }

  public String getSearchField() {
    return searchField;
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

    List<String> searchParameters = intersection(parameters.keySet(), SEARCH_FIELDS_TO_RMAPI.keySet());
    if (searchParameters.size() > 1) {
      throw new QueryValidationException(ERROR + "Search on multiple fields" + UNSUPPORTED);
    }
    if (searchParameters.isEmpty()) {
      throw new QueryValidationException(ERROR + "Invalid query format, unsupported search parameters");
    }

    searchField = SEARCH_FIELDS_TO_RMAPI.get(searchParameters.get(0));
    searchValue = parameters.get(searchParameters.get(0));


    String cqlSort = cqlParameters.getSort();
    if (cqlSort == null || Arrays.asList(TITLE, CODEX_TITLE).contains(cqlSort.toLowerCase())) {
      sortType = RM_API_TITLE;
    } else {
      throw new QueryValidationException(ERROR + "Sorting on " + cqlSort + " is unsupported.");
    }

    List<String> filterTypes = intersection(parameters.keySet(), Arrays.asList(CODEX_RESOURCE_TYPE, RESOURCE_TYPE));
    if (filterTypes.size() > 1) {
      throw new QueryValidationException(ERROR + "Filtering on multiple types " + UNSUPPORTED);
    }
    if (!filterTypes.isEmpty()) {
      filterValue = getFilterValuesByType(parameters.get(filterTypes.get(0)));
    }

    selection = parseSelection(parameters);

    checkSourceParameters(parameters);
  }

  private String getFilterValuesByType(String termNode) throws QueryValidationException {
    try {
      return PubType.fromCodex(termNode).getRmAPI();
    } catch (final IllegalArgumentException e) {
      throw new QueryValidationException(ERROR + "Filtering on type " + termNode + UNSUPPORTED);
    }
  }
}
