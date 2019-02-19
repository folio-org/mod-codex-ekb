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
  private static final String SELECTION_QUERY_PARAM = "&selection=";

  private int instanceIndex;
  private int instanceLimit;
  private List<String> queriesForRMAPI = new ArrayList<>();

  public CQLParserForRMAPI(TitleParameters titleParameters, int offset, int limit) throws UnsupportedEncodingException {
    instanceLimit = limit;
    int rmAPILimit = Math.min(limit, RM_API_MAX_COUNT);
    int pageOffsetRMAPI = computePageOffsetForRMAPI(offset, rmAPILimit);
    queriesForRMAPI.add(buildRMAPIQuery(rmAPILimit, pageOffsetRMAPI, titleParameters));
    instanceIndex = offset % rmAPILimit;
    while (checkIfSecondQueryIsNeeded(offset, rmAPILimit, pageOffsetRMAPI)) {
      queriesForRMAPI.add(buildRMAPIQuery(rmAPILimit, ++pageOffsetRMAPI, titleParameters));
    }
  }

  public CQLParserForRMAPI(PackageParameters packageParameters, int offset, int limit) throws UnsupportedEncodingException {
    instanceLimit = limit;
    int rmAPILimit = Math.min(limit, RM_API_MAX_COUNT);
    int pageOffsetRMAPI = computePageOffsetForRMAPI(offset, rmAPILimit);
    queriesForRMAPI.add(buildRMAPIQuery(rmAPILimit, pageOffsetRMAPI, packageParameters));
    instanceIndex = offset % rmAPILimit;
    while (checkIfSecondQueryIsNeeded(offset, rmAPILimit, pageOffsetRMAPI)) {
      queriesForRMAPI.add(buildRMAPIQuery(rmAPILimit, ++pageOffsetRMAPI, packageParameters));
    }
  }

  String buildRMAPIQuery(int limit, int pageOffsetRMAPI, TitleParameters parameters) throws UnsupportedEncodingException {
    final StringBuilder builder = new StringBuilder();
    builder.append("searchfield=" + parameters.getSearchField());

    if (parameters.getFilterValue() != null) {
      // Map fields to RM API
      builder.append("&resourcetype=" + parameters.getFilterValue());
    }

    if (parameters.getSelection() != null) {
      builder.append(SELECTION_QUERY_PARAM);
      builder.append(parameters.getSelection());
    }

    builder.append("&")
      .append(buildRMAPIQueryForCommonParameters(limit, pageOffsetRMAPI, parameters))
      .append("&searchtype=advanced");
    return builder.toString();
  }

  String buildRMAPIQuery(int limit, int pageOffsetRMAPI, PackageParameters parameters) throws UnsupportedEncodingException {
    final StringBuilder builder = new StringBuilder();
    if (parameters.getFilterValue() != null) {
      // Map fields to RM API
      builder.append("&contenttype=" + parameters.getFilterValue());
    }

    if (parameters.getSelection() != null) {
      builder.append(SELECTION_QUERY_PARAM);
      builder.append(parameters.getSelection());
    }
    builder.append("&")
      .append(buildRMAPIQueryForCommonParameters(limit, pageOffsetRMAPI, parameters));
    return builder.toString();
  }

  private String buildRMAPIQueryForCommonParameters(int limit, int pageOffsetRMAPI, CommonParameters parameters) throws UnsupportedEncodingException {
    return "search=" + URLEncoder.encode(parameters.getSearchValue(), "UTF-8") +
      "&orderby=" + parameters.getSortType() +
      "&count=" + limit +
      "&offset=" + pageOffsetRMAPI;
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
}
