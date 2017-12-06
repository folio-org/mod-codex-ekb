package org.folio.rmapi;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RMAPITitleList {
  
  @JsonProperty("totalResults")
  public Integer totalResults;  
 
  @JsonProperty("titles")
  public List<RMAPITitle> titles;  
}

