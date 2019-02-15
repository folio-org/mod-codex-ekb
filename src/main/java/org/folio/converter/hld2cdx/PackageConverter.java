package org.folio.converter.hld2cdx;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;

import org.folio.codex.ContentType;
import org.folio.rest.jaxrs.model.Coverage;
import org.folio.rest.jaxrs.model.Package;
import org.folio.rmapi.model.CoverageDates;
import org.folio.rmapi.model.PackageData;

public class PackageConverter implements Converter<PackageData, Package> {

  private Converter<CoverageDates, Coverage> coverageConverter;


  public PackageConverter(Converter<CoverageDates, Coverage> coverageConverter) {
    this.coverageConverter = coverageConverter;
  }

  @Override
  public Package convert(@NonNull PackageData source) {
    Package result = new Package();

    if (source.vendorId == null) {
      throw new IllegalArgumentException("Vendor id cannot be null");
    }
    if (source.packageId == null) {
      throw new IllegalArgumentException("Package id cannot be null");
    }

    result.setId(source.vendorId + "-" + source.packageId);

    result.setIsSelected(convertSelected(source.isSelected));
    if (source.customCoverage != null) {
      result.setCoverage(coverageConverter.convert(source.customCoverage));
    }
    result.setItemCount(source.titleCount);
    result.setName(source.packageName);
    result.setProvider(source.vendorName);
    result.setProviderId(Integer.toString(source.vendorId));
    result.setSource("kb");
    result.setType(ContentType.fromRMAPI(StringUtils.defaultString(source.contentType)).getCodex());

    return result;
  }

  private Package.IsSelected convertSelected(Boolean isSelected) {
    if (isSelected == null) {
      return Package.IsSelected.NOT_SPECIFIED;
    } else {
      return isSelected ? Package.IsSelected.YES : Package.IsSelected.NO;
    }
  }

}
