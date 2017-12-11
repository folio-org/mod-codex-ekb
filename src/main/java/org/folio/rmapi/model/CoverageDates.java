/**
 * 
 */
package org.folio.rmapi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author cgodfrey
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CoverageDates {
  @JsonProperty("beginCoverage")
  public String beginCoverage;

  @JsonProperty("endCoverage")
  public String endCoverage;
}
