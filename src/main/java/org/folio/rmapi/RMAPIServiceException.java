package org.folio.rmapi;

public class RMAPIServiceException extends Exception {

  private static final long serialVersionUID = 1L;

  public RMAPIServiceException(String message) {
    super(message);
  }

  public RMAPIServiceException(String message, Exception e) {
    super(message, e);
  }

}
