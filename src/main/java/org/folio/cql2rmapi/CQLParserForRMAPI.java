package org.folio.cql2rmapi;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
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

/**
 * This class is responsible for parsing CQL query to an extent that we extract
 * the supported features of RM API, translate, construct a query string and
 * return it to the CodexInstancesResourceImpl class.
 */

public class CQLParserForRMAPI {

	private String error = "Unsupported Query Format : ";
	private static final String RM_API_TITLE = "titlename";
	private static final String TITLE = "title";
	private static final String TYPE = "type";
	private static final String ID_TYPE = "identifier.type";
	private static final String ID_VALUE = "identifier.value";
	String searchField;
	String searchValue;
	String filterType;
	String filterValue;
	String sortType;
	int countRMAPI;
	int instanceIndex;
	List<String> queriesForRMAPI = new ArrayList<>();

	private enum RMAPISupportedSearchFields {
		TITLE, PUBLISHER, ISSN, ISBN, ISXN, TYPE
	}

	private enum RMAPISupportedFilterValues {
		ALL, JOURNAL, NEWSLETTER, REPORT, PROCEEDINGS, WEBSITE, NEWSPAPER, UNSPECIFIED, BOOK, BOOKSERIES,
		DATABASE, THESISDISSERTATION, STREAMINGAUDIO, STREAMINGVIDEO, AUDIOBOOK
	}

	public CQLParserForRMAPI(String query, int offset, int limit) throws QueryValidationException, UnsupportedEncodingException {
		if(limit != 0) {
		  final CQLNode node = initCQLParser(query);
	    checkNodeInstance(node);
	    final int pageOffsetRMAPI = computePageOffsetForRMAPI(offset, limit);
	    queriesForRMAPI.add(buildRMAPIQuery(limit, pageOffsetRMAPI));
	    instanceIndex = computeInstanceIndex(offset, limit);
	    if(checkIfSecondQueryIsNeeded(offset, limit, pageOffsetRMAPI)) {
	      queriesForRMAPI.add(buildRMAPIQuery(limit, pageOffsetRMAPI+1));
	    }
		} else {
		  throw new QueryValidationException(error + "Limit suggests that no results need to be returned.");
		}
	}

  CQLNode initCQLParser(String query) throws QueryValidationException {
		final CQLParser parser = new CQLParser();
		try {
			return parser.parse(query);
		} catch (CQLParseException | IOException e) {
			error += "Search query is in an unsupported format.";
			throw new QueryValidationException(error, e);
		}
	}

	private void checkNodeInstance(CQLNode node) throws QueryValidationException {
		if (node instanceof CQLTermNode) {
			parseCQLTermNode((CQLTermNode) node);
		}
		if (node instanceof CQLBooleanNode) {
			parseCQLBooleanNode((CQLBooleanNode) node);
		}
		if (node instanceof CQLSortNode) {
			parseCQLSortNode((CQLSortNode) node);
		}
	}

  private void parseCQLTermNode(CQLTermNode node) throws QueryValidationException {
		final String comparator = node.getRelation().getBase(); // gives operator

		// If comparison operators are not supported, log and return an error response
		if (!comparator.equals("=")) {
			error += "Search with " + comparator + " operator is not supported.";
			throw new QueryValidationException(error);
		}

		final String indexNode = node.getIndex(); // gives the search field
		final String termNode = node.getTerm(); // gives the search value

		// If no search field is passed, default it to title search. This is the default
		// search supported by RMAPI
		if ("cql.serverChoice".equalsIgnoreCase(indexNode)) {
			searchField = RM_API_TITLE;
			searchValue = termNode;
		} else if (ID_TYPE.equalsIgnoreCase(indexNode)
				&& EnumUtils.isValidEnum(RMAPISupportedSearchFields.class, termNode.toUpperCase())) {
			searchField = termNode;
		} else if (ID_VALUE.equalsIgnoreCase(indexNode)) {
			searchValue = termNode;
		} else if (TYPE.equalsIgnoreCase(indexNode)) {
			filterType = indexNode;
			if(filterValue == null) {
			  filterValue = termNode;
			} else {
			  error += "Filtering on multiple types is not supported.";
			  throw new QueryValidationException(error);
			}
			if((filterValue != null) && !EnumUtils.isValidEnum(RMAPISupportedFilterValues.class, filterValue.toUpperCase())) {
				// If filter value is not supported, log and return an error response
				error += "Filter on resource type whose value is " + filterValue + " is not supported.";
				throw new QueryValidationException(error);
			}
		} else if (!EnumUtils.isValidEnum(RMAPISupportedSearchFields.class, indexNode.toUpperCase())) {
			// If search field is not supported, log and return an error response
			error += "Search field " + indexNode + " is not supported.";
			throw new QueryValidationException(error);
		} else {
			searchField = indexNode;
			searchValue = termNode;
		}
	}

	private void parseCQLSortNode(CQLSortNode node) throws QueryValidationException {
		final List<ModifierSet> sortIndexes = node.getSortIndexes();
		if (sortIndexes.size() > 1) {
			error += "Sorting on multiple keys is unsupported.";
			throw new QueryValidationException(error);
		}
		// At this point RM API supports only sort by title and relevance
		// front end does not support relevance, so we ignore everything but title
		for (final ModifierSet ms : sortIndexes) {
			sortType = ms.getBase();
			if (sortType.equalsIgnoreCase(TITLE)) {
				sortType = RM_API_TITLE;
			} else {
				final StringBuilder builder = new StringBuilder();
				builder.append(error);
				builder.append("Sorting on ");
				builder.append(sortType);
				builder.append(" is unsupported.");
				throw new QueryValidationException(builder.toString());
			}
		}

		// Get the search field and search value from sort node
		final CQLNode subTree = node.getSubtree();
		checkNodeInstance(subTree);
	}

	private void parseCQLBooleanNode(CQLBooleanNode node) throws QueryValidationException {
		if (node instanceof CQLAndNode) {
		  final String MULTIPLE_FIELDS_ERROR = error + "Search on multiple fields is not supported.";
			final CQLNode leftNode = node.getLeftOperand();
			final CQLNode rightNode = node.getRightOperand();
			if((leftNode != null) && (rightNode != null) && ((leftNode instanceof CQLTermNode) && (rightNode instanceof CQLTermNode))) {
			  final CQLTermNode leftTermNode = (CQLTermNode)leftNode;
			  final CQLTermNode rightTermNode = (CQLTermNode)rightNode;
			  //Support AND operation only if either search param is type or if it is isxn search
			  if ((leftTermNode.getIndex().equalsIgnoreCase(TYPE) || rightTermNode.getIndex().equalsIgnoreCase(TYPE)) ||
			    (leftTermNode.getIndex().equalsIgnoreCase(ID_TYPE) && rightTermNode.getIndex().equalsIgnoreCase(ID_VALUE)) ||
			    (leftTermNode.getIndex().equalsIgnoreCase(ID_VALUE) && (rightTermNode.getIndex().equalsIgnoreCase(ID_TYPE)))) {
			      parseCQLTermNode(leftTermNode);
			      parseCQLTermNode(rightTermNode);
			  } else {
			    throw new QueryValidationException(MULTIPLE_FIELDS_ERROR);
			  }
			} else {
			  throw new QueryValidationException(MULTIPLE_FIELDS_ERROR);
			}
		} else {
			throw new QueryUnsupportedFeatureException("Boolean operators OR, NOT and PROX are unsupported.");
		}
	}

	String buildRMAPIQuery(int limit, int pageOffsetRMAPI) throws QueryValidationException, UnsupportedEncodingException  {
		final StringBuilder builder = new StringBuilder();

		if ((searchValue != null) && (searchField != null)) {
			// Map fields to RM API

			if (searchField.equalsIgnoreCase(TITLE)) {
				searchField = RM_API_TITLE;
			}
			if (searchField.equals("isbn") || searchField.equals("issn")) {
				searchField = "isxn";
			}
			if (sortType == null) {
				sortType = RM_API_TITLE; // orderby is a mandatory field, otherwise RMAPI throws error
			}
			builder.append("search=");
			builder.append(URLEncoder.encode(searchValue, "UTF-8"));
			builder.append("&searchfield=" + searchField);

			if ((filterType != null) && (filterValue != null)) {
				// Map fields to RM API
				builder.append("&resourcetype=" + filterValue);
			}
			builder.append("&orderby=" + sortType);
			builder.append("&count=" + limit);
			builder.append("&offset=" + pageOffsetRMAPI);
		}else {
			throw new QueryValidationException(error + "Invalid query format, unsupported search parameters");
		}
		return builder.toString();
	}

  private int computePageOffsetForRMAPI(int offset, int limit) {
    final float value = offset/(float)limit;
    final double floor = Math.floor(value);
    final double pageOffset = floor + 1;
    return (int) pageOffset;
  }

  private boolean checkIfSecondQueryIsNeeded(int offset, int limit, int pageOffsetRMAPI) {
    boolean secondQueryNeeded = false;
    if((offset + limit) > (pageOffsetRMAPI * limit)) {
      secondQueryNeeded = true;
    }
    return secondQueryNeeded;
  }

  public int computeInstanceIndex(int offset, int limit) {
    return (offset%limit);
  }

  public List<String> getRMAPIQueries() {
	  return queriesForRMAPI;
	}

  public int getInstanceIndex() {
    return instanceIndex;
  }
}
