package org.folio.parser;

import java.util.ArrayList;
import java.util.List;
import javax.validation.ValidationException;
import org.springframework.stereotype.Component;

@Component
public class IdParser {

  private static final String TITLE_ID_IS_INVALID_ERROR = "Title id is invalid - %s";

  public Long parseTitleId(String id){
    return parseId(id, 1, TITLE_ID_IS_INVALID_ERROR, TITLE_ID_IS_INVALID_ERROR).get(0);
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
