package org.folio.rmapi;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RMAPIIdentifier {
  @JsonProperty("id")
  public String id;
 
  @JsonProperty("source")
  public String source;
  
  @JsonProperty("subtype")
  public Integer subtype;
  
  @JsonProperty("type")
  public Integer type;
}
