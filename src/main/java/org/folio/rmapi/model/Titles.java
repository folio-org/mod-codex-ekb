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
public class Titles {

  @JsonProperty("titles")
  public List<Title> titleList;

  @JsonProperty("totalResults")
  public Integer totalResults;

}
