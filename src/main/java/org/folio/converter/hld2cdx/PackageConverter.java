package org.folio.converter.hld2cdx;

import org.apache.commons.lang3.StringUtils;
import org.folio.holdingsiq.model.CoverageDates;
import org.folio.holdingsiq.model.PackageData;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;

import org.folio.codex.ContentType;
import org.folio.rest.jaxrs.model.Coverage;
import org.folio.rest.jaxrs.model.Package;

public class PackageConverter implements Converter<PackageData, Package> {

  private Converter<CoverageDates, Coverage> coverageConverter;


  public PackageConverter(Converter<CoverageDates, Coverage> coverageConverter) {
    this.coverageConverter = coverageConverter;
  }

  @Override
  public Package convert(@NonNull PackageData source) {
    Package result = new Package();

    if (source.getVendorId() == null) {
      throw new IllegalArgumentException("Vendor id cannot be null");
    }
    if (source.getPackageId() == null) {
      throw new IllegalArgumentException("Package id cannot be null");
    }

    result.setId(source.getVendorId() + "-" + source.getPackageId());

    result.setIsSelected(convertSelected(source.getIsSelected()));
    if (source.getCustomCoverage() != null) {
      result.setCoverage(coverageConverter.convert(source.getCustomCoverage()));
    }
    result.setItemCount(source.getTitleCount());
    result.setName(source.getPackageName());
    result.setProvider(source.getVendorName());
    result.setProviderId(Integer.toString(source.getVendorId()));
    result.setSource("kb");
    result.setType(ContentType.fromRMAPI(StringUtils.defaultString(source.getContentType())).getCodex());

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
