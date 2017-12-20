/**
 * 
 */
package org.folio.rmapi;

/**
 * @author cgodfrey
 *
 */
public class RMAPIUnAuthorizedException extends Exception {

  private static final long serialVersionUID = 1L;

  public RMAPIUnAuthorizedException(String message) {
    super(message);
  }
}
