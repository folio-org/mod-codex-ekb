package org.folio.cql2rmapi;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import org.apache.commons.lang3.EnumUtils;
import org.z3950.zing.cql.CQLAndNode;
import org.z3950.zing.cql.CQLBooleanNode;
import org.z3950.zing.cql.CQLNode;
import org.z3950.zing.cql.CQLParseException;
import org.z3950.zing.cql.CQLParser;
import org.z3950.zing.cql.CQLSortNode;
import org.z3950.zing.cql.CQLTermNode;
import org.z3950.zing.cql.ModifierSet;

public class CQLParserForRMAPI {

	private String error = "Unsupported Query Format : ";
	private static final String RM_API_TITLE = "titlename";
	private static final String TITLE = "title";
	private String searchField = null;
	private String searchValue = null;
	private String filterType = null;
	private String filterValue = null;
	private String sortOrder = null;
	private int offset;
	private final int limit;

	private enum RMAPISupportedSearchFields {
		TITLE, PUBLISHER, ISSN, ISBN, ISXN, TYPE
	}

	private enum RMAPISupportedFilterValues {
		ALL, JOURNAL, NEWSLETTER, REPORT, PROCEEDINGS, WEBSITE, NEWSPAPER, UNSPECIFIED, BOOK, BOOKSERIES,
		DATABASE, THESISDISSERTATION, STREAMINGAUDIO, STREAMINGVIDEO, AUDIOBOOK
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
			  parseCQLBooleanNode((CQLBooleanNode) node);
		  }
		  if(node instanceof CQLSortNode) {
			  parseCQLSortNode((CQLSortNode) node);
		  }
	  }

	private void parseCQLTermNode(CQLTermNode node) throws QueryValidationException {
		  final String comparator = node.getRelation().getBase(); //gives operator

		  //If comparison operators are not supported, log and return an error response
		  if(!comparator.equals("=")) {
			  error += "Search with " + comparator + " operator is not supported.";
			  throw new QueryValidationException(error);
		  }

		  final String indexNode = node.getIndex(); //gives the search field
		  final String termNode = node.getTerm(); //gives the search value

		  //If no search field is passed, default it to title search. This is the default search supported by RMAPI
		  if ("cql.serverChoice".equalsIgnoreCase(indexNode)) {
			  searchField = RM_API_TITLE;
			  searchValue = termNode;
		  } else if ("identifier.type".equalsIgnoreCase(indexNode) && EnumUtils.isValidEnum(RMAPISupportedSearchFields.class, termNode.toUpperCase())) {
			  searchField = termNode;
		  } else if ("identifier.value".equalsIgnoreCase(indexNode)) {
			  searchValue = termNode;
		  } else if ("type".equalsIgnoreCase(indexNode)) {
			  filterType = indexNode;
			  filterValue = termNode;
		  } else if(!EnumUtils.isValidEnum(RMAPISupportedSearchFields.class, indexNode.toUpperCase())) {
			  //If search field is not supported, log and return an error response
			  error += "Search field " + indexNode + " is not supported.";
			  throw new QueryValidationException(error);
		  } else {
			  searchField = indexNode;
			  searchValue = termNode;
		  }
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
				 sortOrder = RM_API_TITLE;
			 }else {
				 final StringBuilder builder = new StringBuilder();
				 builder.append(error);
				 builder.append("Sorting on ");
				 builder.append(sortOrder);
				 builder.append(" key is unsupported.");
				 throw new QueryValidationException(builder.toString());
			 }
		  }

		  //Get the search field and search value
		  final CQLNode subTree = node.getSubtree();
		  checkNodeInstance(subTree);
	}

	private void parseCQLBooleanNode(CQLBooleanNode node) throws QueryValidationException {
		if(node instanceof CQLAndNode) {
			final CQLNode leftNode = node.getLeftOperand();
			checkNodeInstance(leftNode);
			final CQLNode rightNode = node.getRightOperand();
			checkNodeInstance(rightNode);
		} else {
			throw new QueryUnsupportedFeatureException("Boolean operators OR and NOT are unsupported.");
		}
	}

	public String getRMAPIQuery() throws UnsupportedEncodingException {
		final StringBuilder builder = new StringBuilder();

		if((searchValue != null) && (searchField != null)) {
			// Map fields to RM API

			if (searchField.equalsIgnoreCase(TITLE)) {
				searchField = RM_API_TITLE;
			}
			if (searchField.equals("isbn") || searchField.equals("issn")) {
				searchField = "isxn";
			}
			if(sortOrder == null) {
				sortOrder = RM_API_TITLE; //orderby is a mandatory field, otherwise RMAPI throws error
			}
			if(offset == 0) {
				offset = 1; //TODO: calculate offsets correctly
			}
			builder.append("search=");
			builder.append(URLEncoder.encode(searchValue, "UTF-8"));
			builder.append("&searchfield=" + searchField);
		}

		if((filterType != null) && (filterValue != null)) {
			//Map fields to RM API
			if(filterType.equalsIgnoreCase("type") && EnumUtils.isValidEnum(RMAPISupportedFilterValues.class, filterValue.toUpperCase())) {
				builder.append("&resourcetype=" + filterValue);
			}
		}
		builder.append("&orderby=" + sortOrder);

		builder.append("&count=" + limit); //TODO : Verify limit
		builder.append("&offset=" + offset); //TODO: Compute offsets correctly

		return builder.toString();
	}
}
