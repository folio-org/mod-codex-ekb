/**
 * 
 */
package org.folio.rmapi;

/**
 * @author cgodfrey
 *
 */
public class RMAPIResultsProcessingException extends Exception {

  private static final long serialVersionUID = 1L;

  public RMAPIResultsProcessingException(String message, Exception e) {
    super(message, e);
  }
}
