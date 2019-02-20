package org.folio.cql2rmapi.query;

import java.io.UnsupportedEncodingException;

import org.folio.cql2rmapi.TitleParameters;

public class TitlesQueryBuilder extends AbsctractQueryBuilder<TitleParameters> {
  protected String buildRMAPIQuery(int limit, int pageOffsetRMAPI, TitleParameters parameters) throws UnsupportedEncodingException {
    final StringBuilder builder = new StringBuilder();
    builder.append("searchfield=" + parameters.getSearchField());

    if (parameters.getFilterValue() != null) {
      // Map fields to RM API
      builder.append("&resourcetype=" + parameters.getFilterValue());
    }

    if (parameters.getSelection() != null) {
      builder.append("&selection=");
      builder.append(parameters.getSelection());
    }

    builder.append("&")
      .append(buildRMAPIQueryForCommonParameters(limit, pageOffsetRMAPI, parameters))
      .append("&searchtype=advanced");
    return builder.toString();
  }
}
