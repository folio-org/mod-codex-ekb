package org.folio.cql2rmapi.query;

import java.io.UnsupportedEncodingException;

import org.folio.cql2rmapi.PackageParameters;

public class PackagesQueryBuilder extends AbsctractQueryBuilder<PackageParameters> {
  @Override
  protected String buildRMAPIQuery(int limit, int pageOffsetRMAPI, PackageParameters parameters) throws UnsupportedEncodingException {
    final StringBuilder builder = new StringBuilder();
    if (parameters.getFilterValue() != null) {
      // Map fields to RM API
      builder.append("&contenttype=" + parameters.getFilterValue());
    }

    if (parameters.getSelection() != null) {
      builder.append("&selection=");
      builder.append(parameters.getSelection());
    }
    builder.append("&")
      .append(buildRMAPIQueryForCommonParameters(limit, pageOffsetRMAPI, parameters));
    return builder.toString();
  }
}
