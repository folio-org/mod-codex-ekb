package org.folio.cql2rmapi.query;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.folio.cql2rmapi.CommonParameters;

public abstract class AbsctractQueryBuilder<T> {
  private static final int RM_API_MAX_COUNT = 100;

  public RMAPIQueries build(T parameters, int offset, int limit) throws UnsupportedEncodingException {
    List<String> queriesForRMAPI = new ArrayList<>();
    int rmAPILimit = Math.min(limit, RM_API_MAX_COUNT);
    int pageOffsetRMAPI = computePageOffsetForRMAPI(offset, rmAPILimit);
    queriesForRMAPI.add(buildRMAPIQuery(rmAPILimit, pageOffsetRMAPI, parameters));
    while (checkIfSecondQueryIsNeeded(offset, rmAPILimit, pageOffsetRMAPI)) {
      queriesForRMAPI.add(buildRMAPIQuery(rmAPILimit, ++pageOffsetRMAPI, parameters));
    }
    return new RMAPIQueries(offset % rmAPILimit, limit, queriesForRMAPI);
  }

  protected abstract String buildRMAPIQuery(int limit, int pageOffsetRMAPI, T parameters) throws UnsupportedEncodingException;

  protected String buildRMAPIQueryForCommonParameters(int limit, int pageOffsetRMAPI, CommonParameters parameters) throws UnsupportedEncodingException {
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

}
