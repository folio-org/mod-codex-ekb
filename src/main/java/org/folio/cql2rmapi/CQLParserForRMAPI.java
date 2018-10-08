package org.folio.cql2rmapi;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang3.EnumUtils;
import org.folio.codex.PubType;
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
  private static final int RM_API_MAX_COUNT = 100;

  private static final String ERROR = "Unsupported Query Format : ";
  private static final String UNSUPPORTED = " is not supported.";
  private static final String CQL_SERVER_CHOICE = "cql.serverChoice";
  private static final String RM_API_TITLE = "titlename";
  private static final String SOURCE = "source";
  private static final String CODEX_SOURCE = "codex.source";
  private static final String SELECTED = "ext.selected";
  private static final String TITLE = "title";
  private static final String CODEX_TITLE = "codex.title";
  private static final String RESOURCE_TYPE = "resourceType";
  private static final String CODEX_RESOURCE_TYPE = "codex.resourceType";
  private static final String IDENTIFIER = "identifier";
  private static final String CODEX_IDENTIFIER = "codex.identifier";
  private static final String SUBJECT = "subject";
  private static final String CODEX_SUBJECT = "codex.subject";
  private static final String PUBLISHER = "publisher";
  private static final String CODEX_PUBLISHER = "codex.publisher";
  private static final String ID = "id";
  private static final String CODEX_ID = "codex.id";

  private static final String SELECTION_QUERY_PARAM = "&selection=";

  String searchField;
  String searchValue;
  String filterType;
  String filterValue;
  String sortType;
  String selection;
  int countRMAPI;
  int instanceIndex;
  int instanceLimit;
  private boolean idSearchField;
  private String idSearchFieldValue;
  List<String> queriesForRMAPI = new ArrayList<>();


  private enum validSources {
    ALL, KB
  }

  public CQLParserForRMAPI(String query, int offset, int limit) throws QueryValidationException, UnsupportedEncodingException {
    if(limit != 0 && query != null) {
      final CQLNode node = initCQLParser(query);
      checkNodeInstance(node);
      //If it is an id search field, we do not need to build the query since we can directly invoke RM API to look for that id
      if(!idSearchField) {
        instanceLimit = limit;
        int pageOffsetRMAPI = computePageOffsetForRMAPI(offset, limit);
        queriesForRMAPI.add(buildRMAPIQuery(limit, pageOffsetRMAPI));
        instanceIndex = computeInstanceIndex(offset, limit);
        while (checkIfSecondQueryIsNeeded(offset, limit, pageOffsetRMAPI)) {
          queriesForRMAPI.add(buildRMAPIQuery(limit, ++pageOffsetRMAPI));
        }
      }
    } else {
      throw new QueryValidationException(ERROR + "Limit/Query suggests that no results need to be returned.");
    }
  }

  CQLNode initCQLParser(String query) throws QueryValidationException {
    final CQLParser parser = new CQLParser();
    try {
      return parser.parse(query);
    } catch (CQLParseException | IOException e) {
      throw new QueryValidationException(ERROR + "Search query is in an unsupported format.", e);
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
    //Check if comparison operator is valid
    checkComparisonOperator(node);

    final String indexNode = node.getIndex(); // gives the search field
    final String termNode = node.getTerm(); // gives the search value
    final StringBuilder builder = new StringBuilder();
    builder.append(ERROR);
    switch(indexNode) {
    case CQL_SERVER_CHOICE:
      // If no search field is passed, default it to title search. This is the default
      // search supported by RMAPI
      setSearchValuesByTitle(termNode);
      break;
    case CODEX_RESOURCE_TYPE:
    case RESOURCE_TYPE:
      //Set filter values based on type
      setFilterValuesByType(indexNode, termNode);
      break;
    case CODEX_SOURCE:
    case SOURCE:
      //Ensure that source is supported
      checkSource(termNode);
      break;
    case CODEX_ID:
    case ID:
      //Extract id as a search field
      idSearchField = true;
      idSearchFieldValue = termNode;
      break;
    case SELECTED:
      //Set holdings selection
      setSelection(termNode);
      break;
    default:
      if (indexNode.startsWith("ext.")) {
        // CQL fields that are in the ext context set should be ignored if they
        // are not recognized by the module.
        break;
      } else if(Stream.of(TITLE, CODEX_TITLE, IDENTIFIER, CODEX_IDENTIFIER, PUBLISHER, CODEX_PUBLISHER, SUBJECT, CODEX_SUBJECT, ID, CODEX_ID).noneMatch(indexNode::equalsIgnoreCase)) {
        // If search field is not supported, log and return an error response
        builder.append("Search field or filter value ");
        builder.append(indexNode);
        builder.append(UNSUPPORTED);
        throw new QueryValidationException(builder.toString());
      } else if((searchField == null) && (searchValue == null)){
        searchField = indexNode;
        searchValue = termNode;
      } else {
        builder.append("Search on multiple fields");
        builder.append(UNSUPPORTED);
        throw new QueryValidationException(builder.toString());
      }
    }
  }

  private void setSelection(String termNode) {
      selection = termNode;
  }

  private void checkSource(String termNode) throws QueryValidationException {
    //Throw an exception and log an error if source is invalid, if it is valid, do nothing.
    if(!EnumUtils.isValidEnum(validSources.class, termNode.toUpperCase())) {
      final StringBuilder builder = new StringBuilder();
      builder.append(ERROR);
      builder.append("Source ");
      builder.append(termNode);
      builder.append(UNSUPPORTED);
      throw new QueryValidationException(builder.toString());
    }
  }

  private void setFilterValuesByType(String indexNode, String termNode) throws QueryValidationException {
    filterType = indexNode;
    if(filterValue == null) {
      try {
        filterValue = PubType.fromCodex(termNode).getRmAPI();
      } catch (final IllegalArgumentException e) {
        final StringBuilder builder = new StringBuilder();
        builder.append(ERROR);
        builder.append("Filtering on type ");
        builder.append(filterValue);
        builder.append(UNSUPPORTED);
        throw new QueryValidationException(builder.toString());
      }
    } else {
      final StringBuilder builder = new StringBuilder();
      builder.append(ERROR);
      builder.append("Filtering on multiple types ");
      builder.append(UNSUPPORTED);
      throw new QueryValidationException(builder.toString());
    }
  }

  private void setSearchValuesByTitle(String termNode) {
    searchField = RM_API_TITLE;
    searchValue = termNode;
  }

  private void checkComparisonOperator(CQLTermNode node) throws QueryValidationException {
    final String comparator = node.getRelation().getBase(); // gives operator
    // If comparison operators are not supported, log and return an error response
    if (!comparator.equals("=")) {
      final StringBuilder builder = new StringBuilder();
      builder.append(ERROR);
      builder.append("Search with ");
      builder.append(comparator);
      builder.append(" operator");
      builder.append(UNSUPPORTED);
      throw new QueryValidationException(builder.toString());
    }
  }

  private void parseCQLSortNode(CQLSortNode node) throws QueryValidationException {
    final List<ModifierSet> sortIndexes = node.getSortIndexes();
    if (sortIndexes.size() > 1) {
      throw new QueryValidationException(ERROR + "Sorting on multiple keys" + UNSUPPORTED);
    }
    // At this point RM API supports only sort by title and relevance
    // Inventory does not support relevance, so we only sort by title
    for (final ModifierSet ms : sortIndexes) {
      sortType = ms.getBase();
      if (sortType.equalsIgnoreCase(TITLE) || (sortType.equalsIgnoreCase(CODEX_TITLE))) {
        sortType = RM_API_TITLE;
      } else {
        final StringBuilder builder = new StringBuilder();
        builder.append(ERROR);
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
      final CQLNode leftNode = node.getLeftOperand();
      final CQLNode rightNode = node.getRightOperand();
      checkNodeInstance(leftNode);
      checkNodeInstance(rightNode);
    } else {
      final StringBuilder builder = new StringBuilder();
      builder.append(ERROR);
      builder.append("Boolean operators OR, NOT and PROX are unsupported.");
      throw new QueryUnsupportedFeatureException(builder.toString());
    }
  }

  String buildRMAPIQuery(int limit, int pageOffsetRMAPI) throws QueryValidationException, UnsupportedEncodingException  {
    final StringBuilder builder = new StringBuilder();

    if ((searchValue != null) && (searchField != null)) {
      // Map fields to RM API
      if (searchField.equalsIgnoreCase(TITLE) || searchField.equalsIgnoreCase(CODEX_TITLE)) {
        searchField = RM_API_TITLE;
      } else if (searchField.equalsIgnoreCase(IDENTIFIER) || searchField.equalsIgnoreCase(CODEX_IDENTIFIER)) {
        searchField = "isxn";
      } else if (searchField.equalsIgnoreCase(PUBLISHER) || searchField.equalsIgnoreCase(CODEX_PUBLISHER)) {
        searchField = PUBLISHER;
      } else if (searchField.equalsIgnoreCase(SUBJECT) || searchField.equalsIgnoreCase(CODEX_SUBJECT)) {
          searchField = SUBJECT;
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

      if (selection != null) {
        // Map fields to RM API
        switch(selection.toLowerCase()) {
        case "all":
          builder.append(SELECTION_QUERY_PARAM);
          builder.append("all");
          break;
        case "true":
          builder.append(SELECTION_QUERY_PARAM);
          builder.append("selected");
          break;
        case "false":
          builder.append(SELECTION_QUERY_PARAM);
          builder.append("notselected");
          break;
        default:
          final StringBuilder errorMsgBuilder = new StringBuilder();
          errorMsgBuilder.append(ERROR);
          errorMsgBuilder.append("Selected value ");
          errorMsgBuilder.append(selection);
          errorMsgBuilder.append(UNSUPPORTED);
          throw new QueryValidationException(errorMsgBuilder.toString());
        }
      }

      builder.append("&orderby=" + sortType);
      builder.append("&count=" + Math.min(limit, RM_API_MAX_COUNT));
      builder.append("&offset=" + pageOffsetRMAPI);
      builder.append("&searchtype=advanced");
    }else {
      throw new QueryValidationException(ERROR + "Invalid query format, unsupported search parameters");
    }
    return builder.toString();
  }

  private int computePageOffsetForRMAPI(int offset, int limit) {
    final float value = offset/(float)Math.min(limit, RM_API_MAX_COUNT);
    final double floor = Math.floor(value);
    final double pageOffset = floor + 1;
    return (int) pageOffset;
  }

  private boolean checkIfSecondQueryIsNeeded(int offset, int limit, int pageOffsetRMAPI) {
    boolean secondQueryNeeded = false;
    if((offset + limit) > (pageOffsetRMAPI * Math.min(limit, RM_API_MAX_COUNT))) {
      secondQueryNeeded = true;
    }
    return secondQueryNeeded;
  }

  public int computeInstanceIndex(int offset, int limit) {
    return (offset%Math.min(limit, RM_API_MAX_COUNT));
  }

  public List<String> getRMAPIQueries() {
    return queriesForRMAPI;
  }

  public int getInstanceIndex() {
    return instanceIndex;
  }

  public int getInstanceLimit() {
    return instanceLimit;
  }

  public boolean isIDSearchField() {
    return idSearchField;
  }

  public String getID() {
    return idSearchFieldValue;
  }
}
