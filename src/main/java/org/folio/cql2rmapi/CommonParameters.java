package org.folio.cql2rmapi;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.EnumUtils;

public abstract class CommonParameters {
  protected static final String ERROR = "Unsupported Query Format : ";
  protected static final String UNSUPPORTED = " is not supported.";

  protected String searchValue;
  protected String sortType;

  public String getSearchValue() {
    return searchValue;
  }

  public String getSortType() {
    return sortType;
  }

  protected List<String> intersection(Collection<String> first, Collection<String> second) {
    return first.stream()
      .filter(second::contains)
      .collect(Collectors.toList());
  }

  protected void checkSource(String termNode) throws QueryValidationException {
    //Throw an exception and log an error if source is invalid, if it is valid, do nothing.
    if(!EnumUtils.isValidEnum(ValidSources.class, termNode.toUpperCase())) {
      throw new QueryValidationException(ERROR + "Source " + termNode + UNSUPPORTED);
    }
  }
}
