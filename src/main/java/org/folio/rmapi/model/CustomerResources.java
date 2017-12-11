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
public class CustomerResources {

  @JsonProperty("titleId")
  public String titleId;

  @JsonProperty("packageId")
  public Integer packageId;

  @JsonProperty("packageName")
  public String packageName;

  @JsonProperty("isPackageCustom")
  public Boolean isPackageCustom;

  @JsonProperty("vendorId")
  public Integer vendorId;

  @JsonProperty("vendorName")
  public String vendorName;

  @JsonProperty("locationId")
  public Integer locationId;

  @JsonProperty("isSelected")
  public Boolean isSelected;

  @JsonProperty("isTokenNeeded")
  public Boolean isTokenNeeded;

  @JsonProperty("visibilityData")
  public VisibilityInfo visibilityData;

  @JsonProperty("managedCoverageList")
  public List<CoverageDates> managedCoverageList;

  @JsonProperty("customCoverageList")
  public List<CoverageDates> customCoverageList;

  @JsonProperty("coverageStatement")
  public String coverageStatement;

  @JsonProperty("managedEmbargoPeriod")
  public EmbargoPeriod managedEmbargoPeriod;

  @JsonProperty("customEmbargoPeriod")
  public EmbargoPeriod customEmbargoPeriod;

  @JsonProperty("url")
  public String url;

  @JsonProperty("userDefinedField1")
  public String userDefinedField1;
  @JsonProperty("userDefinedField2")
  public String userDefinedField2;
  @JsonProperty("userDefinedField3")
  public String userDefinedField3;
  @JsonProperty("userDefinedField4")
  public String userDefinedField4;
  @JsonProperty("userDefinedField5")
  public String userDefinedField5;

}
