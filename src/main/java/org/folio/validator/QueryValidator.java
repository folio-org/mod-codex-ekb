package org.folio.validator;

import java.util.Objects;
import javax.validation.ValidationException;
import org.springframework.stereotype.Component;

@Component
public class QueryValidator {

  public void validate(String query, int limit) {

    if (limit <= 0 || Objects.isNull(query)) {
      throw new ValidationException("Unsupported Query Format : Limit/Query suggests that no results need to be returned.");
    }

  }
}
