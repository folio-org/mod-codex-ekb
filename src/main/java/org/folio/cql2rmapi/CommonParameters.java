package org.folio.cql2rmapi;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.validation.ValidationException;

import org.apache.commons.lang3.EnumUtils;
import org.folio.holdingsiq.model.Sort;

import com.google.common.collect.ImmutableMap;

public abstract class CommonParameters {
  protected static final String ERROR = "Unsupported Query Format : ";
  protected static final String UNSUPPORTED = " is not supported.";
  protected static final String SOURCE = "source";
  protected static final String CODEX_SOURCE = "codex.source";
  protected static final String SELECTED = "ext.selected";
  protected static final Map<String, String> FILTER_SELECTED_MAPPING =
    ImmutableMap.of(
      "all", "all",
      "true", "selected",
      "false", "notselected"
    );
  private static final String CODEX_PREFIX = "codex.";
  protected Sort sortType;

  public Sort getSortType() {
    return sortType;
  }

  protected List<String> intersection(Collection<String> first, Collection<String> second) {
    return first.stream()
      .filter(second::contains)
      .collect(Collectors.toList());
  }

  protected void checkSourceParameters(Map<String, String> parameters) {
    List<String> sourceParameters = intersection(parameters.keySet(), Arrays.asList(CODEX_SOURCE, SOURCE));
    if (!sourceParameters.isEmpty()) {
      String termNode = parameters.get(sourceParameters.get(0));
      //Throw an exception and log an error if source is invalid, if it is valid, do nothing.
      if(!EnumUtils.isValidEnum(ValidSources.class, termNode.toUpperCase())) {
        throw new ValidationException(ERROR + "Source " + termNode + UNSUPPORTED);
      }
    }
  }

  protected String parseSelection(Map<String, String> parameters) {
    if (parameters.get(SELECTED) != null) {
      String selection = FILTER_SELECTED_MAPPING.get(parameters.get(SELECTED).toLowerCase());
      if (selection != null) {
        return selection;
      }
      throw new ValidationException(ERROR + "Selected value " + parameters.get(SELECTED) + UNSUPPORTED);
    }
    return null;
  }

  protected String getCodexParameter(String key, Map<String, String> parameters) {
    String param = parameters.get(key);
    String codexParam = parameters.get(CODEX_PREFIX + key);
    if (param != null && codexParam != null) {
      throw new ValidationException(ERROR + "Using parameter " + key + "multiple times " + UNSUPPORTED);
    }
    return param != null ? param : codexParam;
  }
}
