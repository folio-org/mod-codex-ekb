package org.folio.cql2rmapi;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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
	private final String RMAPITITLE = "titlename";
	private final String TITLE = "title";
	private String searchField = null;
	private String searchValue = null;
	private String sortOrder = null;
	private int offset;
	private final int limit;

	private enum RMAPISupportedSearchFields {
		TITLE, PUBLISHER, ISSN, ISBN, ISXN
	}

	public CQLParserForRMAPI(String query, int offset, int limit) throws QueryValidationException {
		this.offset = offset;
		this.limit = limit;
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
		  String comparator = null;
		  searchField = node.getIndex(); //gives the search field

		  //If no search field is passed, default it to title search. This is the default search supported by RMAPI
		  if ("cql.serverChoice".equalsIgnoreCase(searchField)) {
			  searchField = RMAPITITLE;
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
		  for(final ModifierSet ms : sortIndexes) {
			  sortOrder = ms.getBase();
			 if(sortOrder.equalsIgnoreCase(TITLE)) {
				 sortOrder = RMAPITITLE;
			 }else {
				 final StringBuilder builder = new StringBuilder();
				 builder.append(error);
				 builder.append("Sorting on ");
				 builder.append(sortOrder);
				 builder.append(" key is unsupported.");
				 throw new QueryValidationException(builder.toString());
			 }
		  }
	}

	public String getRMAPIQuery() throws UnsupportedEncodingException {
		final StringBuilder builder = new StringBuilder();

		if((searchValue != null) && (searchField != null)) {
			// Map fields to RM API

			if (searchField.equalsIgnoreCase(TITLE)) {
				searchField = RMAPITITLE;
			}
			if (searchField.equals("isbn") || searchField.equals("issn")) {
				searchField = "isxn";
			}
			if(sortOrder == null) {
				sortOrder = RMAPITITLE; //orderby is a mandatory field, otherwise RMAPI throws error
			}
			if(offset == 0) {
				offset = 1; //minimum offset should be 1, otherwise RMAPI throws an error
			}
			builder.append("search=");
			builder.append(URLEncoder.encode(searchValue, "UTF-8"));
			builder.append("&searchfield=" + searchField);
		}
			builder.append("&orderby=" + sortOrder);

		builder.append("&count=" + limit);
		builder.append("&offset=" + offset);

		return builder.toString();
	}
}
