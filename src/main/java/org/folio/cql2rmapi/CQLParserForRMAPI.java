package org.folio.cql2rmapi;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is responsible for parsing CQL query to an extent that we extract
 * the supported features of RM API, translate, construct a query string and
 * return it to the CodexInstancesResourceImpl class.
 */

public class CQLParserForRMAPI {
  private static final int RM_API_MAX_COUNT = 100;
  private static final String ERROR = "Unsupported Query Format : ";
  private static final String UNSUPPORTED = " is not supported.";
  private static final String SELECTION_QUERY_PARAM = "&selection=";

  int instanceIndex;
  int instanceLimit;
  List<String> queriesForRMAPI = new ArrayList<>();
  private boolean idSearchField;
  private String idSearchFieldValue;


  public CQLParserForRMAPI(String query, int offset, int limit) throws QueryValidationException, UnsupportedEncodingException {
    if (limit != 0 && query != null) {
      CQLParameters parameters = new CQLParameters(query);
      idSearchField = parameters.isIdSearch();
      idSearchFieldValue = parameters.getIdSearchValue();

      //If it is an id search field, we do not need to build the query since we can directly invoke RM API to look for that id
      if (!parameters.isIdSearch()) {
        TitleParameters titleParameters = new TitleParameters(parameters);
        instanceLimit = limit;
        int rmAPILimit = Math.min(limit, RM_API_MAX_COUNT);
        int pageOffsetRMAPI = computePageOffsetForRMAPI(offset, rmAPILimit);
        queriesForRMAPI.add(buildRMAPIQuery(rmAPILimit, pageOffsetRMAPI, titleParameters));
        instanceIndex = offset % rmAPILimit;
        while (checkIfSecondQueryIsNeeded(offset, rmAPILimit, pageOffsetRMAPI)) {
          queriesForRMAPI.add(buildRMAPIQuery(rmAPILimit, ++pageOffsetRMAPI, titleParameters));
        }
      }
    } else {
      throw new QueryValidationException(ERROR + "Limit/Query suggests that no results need to be returned.");
    }
  }

  String buildRMAPIQuery(int limit, int pageOffsetRMAPI, TitleParameters parameters) throws UnsupportedEncodingException {

    final StringBuilder builder = new StringBuilder();
    builder.append("search=");
    builder.append(URLEncoder.encode(parameters.getSearchValue(), "UTF-8"));
    builder.append("&searchfield=" + parameters.getSearchField());

    if ((parameters.getFilterType() != null) && (parameters.getFilterValue() != null)) {
      // Map fields to RM API
      builder.append("&resourcetype=" + parameters.getFilterValue());
    }

    if (parameters.getSelection() != null) {
      builder.append(SELECTION_QUERY_PARAM);
      builder.append(parameters.getSelection());
    }

    builder.append("&orderby=" + parameters.getSortType());
    builder.append("&count=" + limit);
    builder.append("&offset=" + pageOffsetRMAPI);
    builder.append("&searchtype=advanced");
    return builder.toString();
  }

  private int computePageOffsetForRMAPI(int offset, int limit) {
    final float value = offset / (float) limit;
    final double floor = Math.floor(value);
    final double pageOffset = floor + 1;
    return (int) pageOffset;
  }

  private boolean checkIfSecondQueryIsNeeded(int offset, int limit, int pageOffsetRMAPI) {
    return (offset + limit) > (pageOffsetRMAPI * limit);
  }

  public List<String> getRMAPIQueries() {
    return queriesForRMAPI;
  }

  public int getInstanceIndex() {
    return instanceIndex;
  }

  public int getInstanceLimit() {
    return instanceLimit;
  }

  public boolean isIDSearchField() {
    return idSearchField;
  }

  public String getID() {
    return idSearchFieldValue;
  }
}
