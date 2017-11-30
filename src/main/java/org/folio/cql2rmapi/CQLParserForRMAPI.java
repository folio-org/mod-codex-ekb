package org.folio.cql2rmapi;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.EnumUtils;
import org.z3950.zing.cql.CQLBooleanNode;
import org.z3950.zing.cql.CQLNode;
import org.z3950.zing.cql.CQLParseException;
import org.z3950.zing.cql.CQLParser;
import org.z3950.zing.cql.CQLSortNode;
import org.z3950.zing.cql.CQLTermNode;
import org.z3950.zing.cql.ModifierSet;

public class CQLParserForRMAPI {

	private String error = "Unsupported Query Format : ";

	private enum RMAPISupportedSearchFields {
		TITLE, PUBLISHER, ISSN, ISBN, ISXN
	}

	public CQLParserForRMAPI(String query) throws QueryValidationException {
	    initCQLParser(query);
	}

	private void initCQLParser(String query) throws QueryValidationException {
		  final CQLParser parser = new CQLParser();
			try {
				final CQLNode node = parser.parse(query);
				checkNodeInstance(node);
			} catch (CQLParseException | IOException e) {
				error += "Search query is in an unsupported format.";
				throw new QueryValidationException(error);
			}
	  }


	  private void checkNodeInstance(CQLNode node) throws QueryValidationException{
		  if(node instanceof CQLTermNode) {
			  parseCQLTermNode((CQLTermNode) node);
		  }
		  if(node instanceof CQLBooleanNode) {
			  error += "Multiple search tags are not supported.";
			  throw new QueryValidationException(error);
		  }
		  if(node instanceof CQLSortNode) {
			  parseCQLSortNode((CQLSortNode) node);
		  }
	  }

	private void parseCQLTermNode(CQLTermNode node) throws QueryValidationException {
		  String searchField = null;
		  String searchValue = null;
		  String comparator = null;

		  searchField = node.getIndex(); //gives the search field

		  //If no search field is passed, default it to title search. This is the default search supported by RMAPI
		  if ("cql.serverChoice".equalsIgnoreCase(searchField)) {
			  searchField = "title";
		  } else if(!EnumUtils.isValidEnum(RMAPISupportedSearchFields.class, searchField.toUpperCase())) {
			  //If search field is not supported, log and return an error response
			  error += "Search field " + searchField + " is not supported.";
			  throw new QueryValidationException(error);
		  }

		  comparator = node.getRelation().getBase(); //gives operator

		  //If comparison operators are not supported, log and return an error response
		  if(!comparator.equals("=")) {
			  error += "Search with " + comparator + " operator is not supported.";
			  throw new QueryValidationException(error);
		  }

		  searchValue = node.getTerm(); //gives the search value
		}

	  private void parseCQLSortNode(CQLSortNode node) throws QueryValidationException {
		  final List<ModifierSet> sortIndexes = node.getSortIndexes();
		  if(sortIndexes.size() > 1) {
			  error += "Sorting on multiple keys is unsupported.";
			  throw new QueryValidationException(error);
		  }
		  //At this point RM API supports only sort by title and relevance
		  //front end does not support relevance, so we ignore everything but title
		  String sortOrder = null;
		  for(final ModifierSet ms : sortIndexes) {
			  sortOrder = ms.getBase();
			 if(sortOrder.equalsIgnoreCase("title")) {
				 sortOrder = "title";
			 }else {
				 error += "Sorting on " + sortOrder + " key is unsupported.";
				 throw new QueryValidationException(error);
			 }
		  }
	}
}
