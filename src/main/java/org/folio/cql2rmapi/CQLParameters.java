package org.folio.cql2rmapi;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.z3950.zing.cql.CQLAndNode;
import org.z3950.zing.cql.CQLBooleanNode;
import org.z3950.zing.cql.CQLNode;
import org.z3950.zing.cql.CQLParseException;
import org.z3950.zing.cql.CQLParser;
import org.z3950.zing.cql.CQLSortNode;
import org.z3950.zing.cql.CQLTermNode;
import org.z3950.zing.cql.ModifierSet;

public class CQLParameters {
  private static final String ID = "id";
  private static final String CODEX_ID = "codex.id";
  private static final String ERROR = "Unsupported Query Format : ";
  private static final String UNSUPPORTED = " is not supported.";

  private Map<String, String> parameters = new HashMap<>();
  private String sort;
  private boolean idSearch;
  private String idSearchValue;

  public CQLParameters(Map<String, String> parameters, String sort, boolean idSearch, String idSearchValue) {
    this.parameters = parameters;
    this.sort = sort;
    this.idSearch = idSearch;
    this.idSearchValue = idSearchValue;
  }

  public CQLParameters(String query) throws QueryValidationException {
    CQLNode node = initCQLParser(query);
    parseNode(node);
  }

  public Map<String, String> getParameters() {
    return parameters;
  }

  public String getSort() {
    return sort;
  }

  public boolean isIdSearch() {
    return idSearch;
  }

  public String getIdSearchValue() {
    return idSearchValue;
  }

  private void parseNode(CQLNode node) throws QueryValidationException {
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
    if (Arrays.asList(ID, CODEX_ID).contains(indexNode)) {
      idSearch = true;
      idSearchValue = termNode;
    }
    parameters.put(indexNode, termNode);
  }

  private void checkComparisonOperator(CQLTermNode node) throws QueryValidationException {
    final String comparator = node.getRelation().getBase(); // gives operator
    // If comparison operators are not supported, log and return an error response
    if (!comparator.equals("=")) {
      throw new QueryValidationException(ERROR + "Search with " + comparator + " operator" + UNSUPPORTED);
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
      sort = ms.getBase();
    }

    // Get the search field and search value from sort node
    parseNode(node.getSubtree());
  }

  private void parseCQLBooleanNode(CQLBooleanNode node) throws QueryValidationException {
    if (node instanceof CQLAndNode) {
      final CQLNode leftNode = node.getLeftOperand();
      final CQLNode rightNode = node.getRightOperand();
      parseNode(leftNode);
      parseNode(rightNode);
    } else {
      throw new QueryUnsupportedFeatureException(ERROR + "Boo1lean operators OR, NOT and PROX are unsupported.");
    }
  }

  private CQLNode initCQLParser(String query) throws QueryValidationException {
    final CQLParser parser = new CQLParser();
    try {
      return parser.parse(query);
    } catch (CQLParseException | IOException e) {
      throw new QueryValidationException(ERROR + "Search query is in an unsupported format.", e);
    }
  }
}
