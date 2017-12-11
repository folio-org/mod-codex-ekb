/**
 * 
 */
package org.folio.rmapi.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author cgodfrey
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Title {
  @JsonProperty("description")
  public String description;

  @JsonProperty("edition")
  public String edition;

  @JsonProperty("isPeerReviewed")
  public Boolean isPeerReviewed;

  @JsonProperty("contributorsList")
  public List<Contributor> contributorsList;

  @JsonProperty("titleId")
  public Integer titleId;

  @JsonProperty("titleName")
  public String titleName;

  @JsonProperty("publisherName")
  public String publisherName;

  @JsonProperty("identifiersList")
  public List<Identifier> identifiersList;

  @JsonProperty("subjectsList")
  public List<Subject> subjectsList;

  @JsonProperty("isTitleCustom")
  public Boolean isTitleCustom;

  @JsonProperty("pubType")
  public String pubType;

  @JsonProperty("customerResourcesList")
  public List<CustomerResources> customerResourcesList;

}
