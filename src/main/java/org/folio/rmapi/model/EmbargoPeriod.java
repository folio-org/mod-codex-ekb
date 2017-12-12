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
public class EmbargoPeriod {

  @JsonProperty("embargoUnit")
  public String embargoUnit;

  @JsonProperty("embargoValue")
  public Integer embargoValue;
}
