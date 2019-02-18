package org.folio.cql2rmapi;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.EnumUtils;
import org.folio.codex.PubType;

import com.google.common.collect.ImmutableMap;

public class TitleParameters {

  private static final String ERROR = "Unsupported Query Format : ";
  private static final String UNSUPPORTED = " is not supported.";
  private static final String SOURCE = "source";
  private static final String CODEX_SOURCE = "codex.source";
  private static final String SELECTED = "ext.selected";
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
  private String searchValue;
  private String filterType;
  private String filterValue;
  private String sortType;
  private String selection;

  public TitleParameters(String searchField, String searchValue, String filterType, String filterValue, String sortType, String selection) {
    this.searchField = searchField;
    this.searchValue = searchValue;
    this.filterType = filterType;
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

  public String getSearchValue() {
    return searchValue;
  }

  public String getFilterType() {
    return filterType;
  }

  public String getFilterValue() {
    return filterValue;
  }

  public String getSortType() {
    return sortType;
  }

  public String getSelection() {
    return selection;
  }

  private void parseCqlParameters(CQLParameters cqlParameters) throws QueryValidationException {
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
      filterType = filterTypes.get(0);
      filterValue = getFilterValuesByType(parameters.get(filterType));
    }

    List<String> sourceParameters = intersection(parameters.keySet(), Arrays.asList(CODEX_SOURCE, SOURCE));
    if(!sourceParameters.isEmpty()){
      checkSource(parameters.get(sourceParameters.get(0)));
    }

    if (parameters.get(SELECTED) != null) {
      // Map fields to RM API
      switch (parameters.get(SELECTED).toLowerCase()) {
        case "all":
          selection = "all";
          break;
        case "true":
          selection = "selected";
          break;
        case "false":
          selection = "notselected";
          break;
        default:
          throw new QueryValidationException(ERROR + "Selected value " + parameters.get(SELECTED) + UNSUPPORTED);
      }
    }
  }

  private List<String> intersection(Collection<String> first, Collection<String> second) {
    return first.stream()
      .filter(second::contains)
      .collect(Collectors.toList());
  }

  private String getFilterValuesByType(String termNode) throws QueryValidationException {
    try {
      return PubType.fromCodex(termNode).getRmAPI();
    } catch (final IllegalArgumentException e) {
      throw new QueryValidationException(ERROR + "Filtering on type " + termNode + UNSUPPORTED);
    }
  }

  private void checkSource(String termNode) throws QueryValidationException {
    //Throw an exception and log an error if source is invalid, if it is valid, do nothing.
    if(!EnumUtils.isValidEnum(ValidSources.class, termNode.toUpperCase())) {
      throw new QueryValidationException(ERROR + "Source " + termNode + UNSUPPORTED);
    }
  }

  private enum ValidSources {
    ALL, KB
  }
}
