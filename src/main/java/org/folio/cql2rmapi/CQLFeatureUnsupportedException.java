package org.folio.cql2rmapi;

public class CQLFeatureUnsupportedException extends QueryValidationException {

	private static final long serialVersionUID = 1L;

	/**
	 * Feature or features of the CQL query are currently unsupported by CQL2PgJSON
	 */
	public CQLFeatureUnsupportedException(String message) {
		super(message);
	}
}
