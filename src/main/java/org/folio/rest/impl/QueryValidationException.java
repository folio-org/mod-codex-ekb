package org.folio.rest.impl;

public class QueryValidationException extends Exception {
	/**
	 * The CQL query passed does not appear to be valid.
	 */
	private static final long serialVersionUID = 1L;

	public QueryValidationException(String message) {
		super(message);
	}

	public QueryValidationException(Exception e) {
		super(e);
	}
}
