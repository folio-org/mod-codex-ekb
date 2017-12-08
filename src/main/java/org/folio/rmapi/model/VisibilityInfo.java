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
public class VisibilityInfo {
  @JsonProperty("isHidden")
  public Boolean isHidden;

  @JsonProperty("reason")
  public String reason;
}
