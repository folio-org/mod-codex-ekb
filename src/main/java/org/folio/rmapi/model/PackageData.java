package org.folio.rmapi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PackageData {
  @JsonProperty("packageId")
  public Integer packageId;
  @JsonProperty("packageName")
  public String packageName;
  @JsonProperty("vendorId")
  public Integer vendorId;
  @JsonProperty("vendorName")
  public String vendorName;
  @JsonProperty("isCustom")
  public Boolean isCustom;
  @JsonProperty("titleCount")
  public Integer titleCount;
  @JsonProperty("isSelected")
  public Boolean isSelected;
  @JsonProperty("selectedCount")
  public Integer selectedCount;
  @JsonProperty("contentType")
  public String contentType;
  @JsonProperty("visibilityData")
  public VisibilityInfo visibilityData;
  @JsonProperty("customCoverage")
  public CoverageDates customCoverage;
  @JsonProperty("isTokenNeeded")
  public Boolean isTokenNeeded;
  @JsonProperty("allowEbscoToAddTitles")
  public Boolean allowEbscoToAddTitles;
  @JsonProperty("packageType")
  public String packageType;
}
