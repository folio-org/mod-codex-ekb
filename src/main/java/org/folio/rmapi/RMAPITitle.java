package org.folio.rmapi;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties({"subjectsList", "isTitleCustom", "customerResourcesList", "description", "isPeerReviewed"})

public class RMAPITitle {
  
  @JsonProperty("titleName")
  public String titleName;
 
  @JsonProperty("titleId")
  public Integer titleId;
  
  @JsonProperty("publisherName")
  public String publisherName;
  
  @JsonProperty("pubType")
  public String pubType;
  
  @JsonProperty("edition")
  public String edition;
  
  @JsonProperty("identifiersList")
  public List<RMAPIIdentifier> identifiers;
  
  @JsonProperty("contributorsList")
  public List<RMAPIContributor> contributors;  
}

