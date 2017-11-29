package org.folio.rest.impl;

import java.io.IOException;

import org.apache.commons.lang3.EnumUtils;
import org.z3950.zing.cql.CQLBooleanNode;
import org.z3950.zing.cql.CQLNode;
import org.z3950.zing.cql.CQLParseException;
import org.z3950.zing.cql.CQLParser;
import org.z3950.zing.cql.CQLSortNode;
import org.z3950.zing.cql.CQLTermNode;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class CQLParserForRMAPI {

	private final Logger log = LoggerFactory.getLogger(CQLParserForRMAPI.class);

	private enum RMAPISupportedSearchFields {
		TITLE, PUBLISHER
	}

	public CQLParserForRMAPI(String query) throws QueryValidationException {
	    initCQLParser(query);
	}

	private void initCQLParser(String query) throws QueryValidationException {
		  final CQLParser parser = new CQLParser();
		  System.out.println(query);
			try {
				final CQLNode node = parser.parse(query);
				System.out.println(node.toXCQL());
				checkNodeInstance(node);
			} catch (CQLParseException | IOException e) {
				throw new QueryValidationException(e);
			}
	  }


	  private void checkNodeInstance(CQLNode node) throws QueryValidationException{
		  if(node instanceof CQLTermNode) {
			  parseCQLTermNode((CQLTermNode) node);
		  }
		  if(node instanceof CQLBooleanNode) {
			  throw new UnsupportedOperationException("Boolean operations in searches are not supported at this time.");
		  }
		  if(node instanceof CQLSortNode) {
			  throw new UnsupportedOperationException("Sort operations are not supported at this time.");
		  }

	  }

	  private void parseCQLTermNode(CQLTermNode node) {
		   if(!EnumUtils.isValidEnum(RMAPISupportedSearchFields.class, node.getIndex().toUpperCase())) {
			   throw new UnsupportedOperationException("Search operation on this field is not supported at this time.");
		   }

		   System.out.println("valid enum type" +node.getIndex()); // gives title
		   System.out.println("value" +node.getTerm()); //gives value
		   System.out.println("operation" +node.getRelation()); //gives operator
		}
}
