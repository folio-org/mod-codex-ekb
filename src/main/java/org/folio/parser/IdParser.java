package org.folio.parser;

import java.util.ArrayList;
import java.util.List;

import javax.validation.ValidationException;

import org.springframework.stereotype.Component;

import org.folio.holdingsiq.model.PackageId;

@Component
public class IdParser {

  private static final String INSTANCE_ID_IS_INVALID_ERROR = "Instance id is invalid - %s";
  private static final String PACKAGE_ID_MISSING_ERROR = "Package and provider id are required";
  private static final String PACKAGE_ID_INVALID_ERROR = "Package or provider id are invalid";

  public Long parseTitleId(String id){
    return parseId(id, 1, INSTANCE_ID_IS_INVALID_ERROR, INSTANCE_ID_IS_INVALID_ERROR).get(0);
  }

  public PackageId parsePackageId(String id){
    List<Long> parts = parseId(id, 2, PACKAGE_ID_MISSING_ERROR, PACKAGE_ID_INVALID_ERROR);
    return PackageId.builder().providerIdPart(parts.get(0)).packageIdPart(parts.get(1)).build();
  }

  private List<Long> parseId(String id, int partCount, String wrongCountErrorMessage, String numberFormatErrorMessage) {
    String[] parts = id.split("-");
    if (parts.length != partCount) {
      throw new ValidationException(
        String.format(wrongCountErrorMessage, id));
    }
    List<Long> parsedParts = new ArrayList<>();
    try {
      for (String part : parts) {
        parsedParts.add(Long.parseLong(part));
      }
      return parsedParts;
    } catch (NumberFormatException e) {
      throw new ValidationException(String.format(numberFormatErrorMessage, id));
    }
  }
}
