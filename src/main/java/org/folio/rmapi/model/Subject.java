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
public class Subject {

  @JsonProperty("type")
  public String type;

  @JsonProperty("subject")
  public String subject;
}
