package org.folio.rmapi;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RMAPIContributor {
  
  @JsonProperty("type")
  public String type;
 
  @JsonProperty("contributor")
  public String contributor;
 
}
