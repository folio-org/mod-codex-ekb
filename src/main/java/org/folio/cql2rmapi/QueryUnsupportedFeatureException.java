package org.folio.cql2rmapi;

public class QueryUnsupportedFeatureException extends QueryValidationException {

	/**
	 * Unsupported Feature Exception
	 */
	private static final long serialVersionUID = 1L;

	public QueryUnsupportedFeatureException(String message) {
		super(message);
	}
}
