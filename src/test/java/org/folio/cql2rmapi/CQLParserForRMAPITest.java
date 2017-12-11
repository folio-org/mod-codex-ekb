package org.folio.cql2rmapi;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.junit.Test;
import org.z3950.zing.cql.CQLNode;

/**
 * Unit tests for CQLParserForRMAPI class.
 *
 */

public class CQLParserForRMAPITest {

  private static final String VALID_QUERY = "title=bridget";
  private static final String VALID_ISBN_QUERY = "identifier.type = isbn and identifier.value = 12345 sortby title";
  private static final String VALID_ISSN_QUERY = "identifier.type = issn and identifier.value = 12345 sortby title";
  private static final String VALID_FILTER_QUERY = "title = bridget and type = journal sortby title";

  @Test(expected = QueryValidationException.class)
  public void initCQLParserThrowsExceptionIfQueryInvalidTest() throws QueryValidationException, UnsupportedEncodingException {
    final String invalidQuery = "";
    new CQLParserForRMAPI(invalidQuery, 1, 10);
  }

  @Test
  public void initCQLParserReturnsInstanceOfCQLNodeIfQueryValidTest() throws QueryValidationException, UnsupportedEncodingException {
    final CQLParserForRMAPI parser = new CQLParserForRMAPI(VALID_QUERY, 1, 10);
    final Object returnValue = parser.initCQLParser(VALID_QUERY);
    assertThat(returnValue, instanceOf(CQLNode.class));
  }

  @Test(expected = QueryValidationException.class)
  public void CQLParserThrowsExceptionInCaseOfUnsupportedOperatorTest() throws QueryValidationException, UnsupportedEncodingException {
    final String invalidOperator = "title<>bridget";
    new CQLParserForRMAPI(invalidOperator, 1, 10);
  }

  @Test
  public void CQLParserSetsExpectedValuesIfSearchFieldNotProvidedTest() throws QueryValidationException, UnsupportedEncodingException {
    final String validQuery = "bridget";
    final CQLParserForRMAPI parser = new CQLParserForRMAPI(validQuery, 1, 10);
    assertEquals("titlename", parser.searchField);
    assertEquals("bridget", parser.searchValue);
  }

  @Test
  public void CQLParserSetsExpectedValuesIfValidSearchFieldIsProvidedTest() throws QueryValidationException, UnsupportedEncodingException {
    final CQLParserForRMAPI parser = new CQLParserForRMAPI(VALID_ISBN_QUERY, 1, 10);
    assertEquals("isxn", parser.searchField);
    assertEquals("12345", parser.searchValue);
    assertEquals("titlename", parser.sortType);
  }

  @Test(expected = QueryValidationException.class)
  public void CQLParserThrowsExceptionInCaseOfUnsupportedIdentifierFieldTest() throws QueryValidationException, UnsupportedEncodingException {
    final String invalidIdentifierTypeQuery = "identifier.type = xyz and identifier.value = 12345 sortby title";
    new CQLParserForRMAPI(invalidIdentifierTypeQuery, 1, 10);
  }

  @Test(expected = QueryValidationException.class)
  public void CQLParserThrowsExceptionInCaseOfUnsupportedSearchFieldTest() throws QueryValidationException, UnsupportedEncodingException {
    final String invalidOperator = "author=bridget";
    new CQLParserForRMAPI(invalidOperator, 1, 10);
  }

  @Test
  public void CQLParserSetsExpectedValuesIfTypeFilterIsPassedTest() throws QueryValidationException, UnsupportedEncodingException {
    final CQLParserForRMAPI parser = new CQLParserForRMAPI(VALID_FILTER_QUERY, 1, 10);
    assertEquals("titlename", parser.searchField);
    assertEquals("bridget", parser.searchValue);
    assertEquals("titlename", parser.sortType);
    assertEquals("type", parser.filterType);
    assertEquals("journal", parser.filterValue);
  }

  @Test(expected = QueryValidationException.class)
  public void CQLParserThrowsExceptionIfInvalidFilterValueIsPassedTest() throws QueryValidationException, UnsupportedEncodingException {
    final String invalidFilterQuery = "title = bridget and type = hardcopy sortby title";
    new CQLParserForRMAPI(invalidFilterQuery, 1, 10);
  }

  @Test(expected = QueryValidationException.class)
  public void CQLParserThrowsExceptionIfNullFilterValueIsPassedTest() throws QueryValidationException, UnsupportedEncodingException {
    final String invalidFilterQuery = "title = bridget and type = null sortby title";
    final CQLParserForRMAPI parser = new CQLParserForRMAPI(invalidFilterQuery, 1, 10);
    parser.filterValue = null;
  }

  @Test(expected = QueryValidationException.class)
  public void CQLParserThrowsExceptionIfMultipleFiltersSepartedByAndAreProvidedTest() throws QueryValidationException, UnsupportedEncodingException {
    final String multipleFilterQuery = "title = bridget and type = journal and type = audiobook sortby title, publisher";
    new CQLParserForRMAPI(multipleFilterQuery, 1, 10);
  }

  @Test(expected = QueryValidationException.class)
  public void CQLParserThrowsExceptionIfMultipleFiltersSepartedByCommaAreProvidedTest() throws QueryValidationException, UnsupportedEncodingException {
    final String multipleFilterQuery = "title = bridget and type = journal, audiobook sortby title, publisher";
    new CQLParserForRMAPI(multipleFilterQuery, 1, 10);
  }

  @Test(expected = QueryValidationException.class)
  public void CQLParserThrowsExceptionIfMultipleFiltersSepartedBySpaceAreProvidedTest() throws QueryValidationException, UnsupportedEncodingException {
    final String multipleFilterQuery = "title = bridget and type = journal audiobook sortby title, publisher";
    new CQLParserForRMAPI(multipleFilterQuery, 1, 10);
  }

  @Test(expected = QueryValidationException.class)
  public void CQLParserThrowsExceptionIfMultipleSortKeysAreProvidedTest() throws QueryValidationException, UnsupportedEncodingException {
    final String multipleSortQuery = "title = bridget and type = journal sortby title, publisher";
    new CQLParserForRMAPI(multipleSortQuery, 1, 10);
  }

  @Test(expected = QueryValidationException.class)
  public void CQLParserThrowsExceptionIfSortOnUnsupportedFieldTest() throws QueryValidationException, UnsupportedEncodingException {
    final String multipleSortQuery = "title = bridget and type = journal sortby publisher";
    new CQLParserForRMAPI(multipleSortQuery, 1, 10);
  }

  @Test(expected = QueryValidationException.class)
  public void CQLParserThrowsExceptionIfUnsupportedBooleanOperatorIsPassedTest() throws QueryValidationException, UnsupportedEncodingException {
    final String multipleSortQuery = "title = bridget OR publisher = Ebsco sortby title";
    new CQLParserForRMAPI(multipleSortQuery, 1, 10);
  }

  @Test
  public void CQLParserReturnsExpectedQueryStringIfValidQueryIsPassedTest()
      throws QueryValidationException, UnsupportedEncodingException {
    final CQLParserForRMAPI parser = new CQLParserForRMAPI(VALID_QUERY, 10, 10);
    assertEquals("search=bridget&searchfield=titlename&orderby=titlename&count=10&offset=2", parser.getRMAPIQueries().get(0));
  }

  @Test(expected = QueryValidationException.class)
  public void CQLParserThrowsExceptionIfsearchValueAndSearchFieldAreNullTest() throws QueryValidationException, UnsupportedEncodingException {
    final CQLParserForRMAPI parser = new CQLParserForRMAPI(VALID_QUERY, 0, 0);
    parser.searchField = null;
    parser.searchValue = null;
    parser.buildRMAPIQuery(10, 1);
  }

  @Test(expected = QueryValidationException.class)
  public void CQLParserThrowsExceptionIfsearchValueIsNullTest() throws QueryValidationException, UnsupportedEncodingException {
    final CQLParserForRMAPI parser = new CQLParserForRMAPI(VALID_QUERY, 0, 0);
    parser.searchValue = null;
    parser.buildRMAPIQuery(10, 1);
  }

  @Test(expected = QueryValidationException.class)
  public void CQLParserThrowsExceptionIfsearchFieldIsNullTest() throws QueryValidationException, UnsupportedEncodingException {
    final CQLParserForRMAPI parser = new CQLParserForRMAPI(VALID_QUERY, 0, 0);
    parser.searchField = null;
    parser.buildRMAPIQuery(10, 1);
  }

  @Test
  public void CQLParserMapsISBNToISXNTest() throws QueryValidationException, UnsupportedEncodingException {
    final CQLParserForRMAPI parser = new CQLParserForRMAPI(VALID_ISBN_QUERY, 0, 10);
    assertEquals("search=12345&searchfield=isxn&orderby=titlename&count=10&offset=1", parser.getRMAPIQueries().get(0));
  }

  @Test
  public void CQLParserMapsISSNToISXNTest() throws QueryValidationException, UnsupportedEncodingException {
    final CQLParserForRMAPI parser = new CQLParserForRMAPI(VALID_ISSN_QUERY, 0, 10);
    assertEquals("search=12345&searchfield=isxn&orderby=titlename&count=10&offset=1", parser.getRMAPIQueries().get(0));
  }

  @Test
  public void CQLParserPassesAlongOffsetIfNotEqualToZeroTest() throws QueryValidationException, UnsupportedEncodingException {
    final CQLParserForRMAPI parser = new CQLParserForRMAPI(VALID_ISSN_QUERY, 10, 10);
    assertEquals("search=12345&searchfield=isxn&orderby=titlename&count=10&offset=2", parser.getRMAPIQueries().get(0));
  }

  @Test
  public void CQLParserDoesNotAppendResourceTypeIfTypeNotPassedAlongTest() throws QueryValidationException, UnsupportedEncodingException {
    final CQLParserForRMAPI parser = new CQLParserForRMAPI(VALID_ISSN_QUERY, 10, 10);
    assertEquals("search=12345&searchfield=isxn&orderby=titlename&count=10&offset=2", parser.getRMAPIQueries().get(0));
  }

  @Test
  public void CQLParserDoesNotAppendResourceTypeIfFilterTypeIsNullTest() throws QueryValidationException, UnsupportedEncodingException {
    final CQLParserForRMAPI parser = new CQLParserForRMAPI(VALID_FILTER_QUERY, 10, 10);
    parser.filterType = null;
    assertEquals("search=bridget&searchfield=titlename&orderby=titlename&count=10&offset=1", parser.buildRMAPIQuery(10, 1));
  }

  @Test
  public void CQLParserDoesNotAppendResourceTypeIfFilTerValueIsNullTest() throws QueryValidationException, UnsupportedEncodingException {
    final CQLParserForRMAPI parser = new CQLParserForRMAPI(VALID_FILTER_QUERY, 10, 10);
    parser.filterValue = null;
    assertEquals("search=bridget&searchfield=titlename&orderby=titlename&count=10&offset=1", parser.buildRMAPIQuery(10, 1));
  }

  @Test
  public void CQLParserAppendsResourceTypeIfFilterTypeAndFilTerValueAreValidTest() throws QueryValidationException, UnsupportedEncodingException {
    final CQLParserForRMAPI parser = new CQLParserForRMAPI(VALID_FILTER_QUERY, 10, 10);
    assertEquals("search=bridget&searchfield=titlename&resourcetype=journal&orderby=titlename&count=10&offset=2",
        parser.getRMAPIQueries().get(0));
  }

  @Test(expected = QueryValidationException.class)
  public void CQLParserThrowsExceptionIfMultipleSearchFieldsAreProvidedTest() throws UnsupportedEncodingException, QueryValidationException {
    new CQLParserForRMAPI("title=something AND publisher=EBSCO" , 10, 10);
  }

  @Test(expected = QueryValidationException.class)
  public void CQLParserThrowsExceptionIfANDSearchIsInvalidTest() throws UnsupportedEncodingException, QueryValidationException {
    new CQLParserForRMAPI("title=something AND SORTBY TITLE" , 10, 10);
  }

  @Test
  public void CQLParserSetsExpectedValuesInCaseOfValidSearchTest() throws UnsupportedEncodingException, QueryValidationException {
    final CQLParserForRMAPI parser = new CQLParserForRMAPI("type=journal AND title=bridget" , 10, 10);
    assertEquals("titlename", parser.searchField);
    assertEquals("bridget", parser.searchValue);
    assertEquals("type", parser.filterType);
    assertEquals("journal", parser.filterValue);
  }

  @Test
  public void CQLParserSetsExpectedValuesInCaseOfValidISXNsearchTest() throws UnsupportedEncodingException, QueryValidationException {
    final CQLParserForRMAPI parser = new CQLParserForRMAPI("identifier.value=12345 AND identifier.type=isxn" , 10, 10);
    assertEquals("isxn", parser.searchField);
    assertEquals("12345", parser.searchValue);
  }

  @Test
  public void CQLParserSetsExpectedValuesInCaseOfValidISBNsearchTest() throws UnsupportedEncodingException, QueryValidationException {
    final CQLParserForRMAPI parser = new CQLParserForRMAPI("identifier.type=isbn AND identifier.value=12345" , 10, 10);
    assertEquals("isxn", parser.searchField);
    assertEquals("12345", parser.searchValue);
  }

  @Test
  public void CQLParserComputesQueriesAndIndexForRMAPICorrectlyTest() throws UnsupportedEncodingException, QueryValidationException {
    final CQLParserForRMAPI parser = new CQLParserForRMAPI("title=bridget" , 900, 100);
    final ArrayList<String> queries = (ArrayList<String>) parser.getRMAPIQueries();
    assertEquals(1, queries.size());
    assertEquals(0, parser.getInstanceIndex());
    for (final String query: queries) {
      assertEquals("search=bridget&searchfield=titlename&orderby=titlename&count=100&offset=10", query);
    }
  }

  @Test
  public void CQLParserComputesMultipleQueriesAndIndexForRMAPICorrectlyTest() throws UnsupportedEncodingException, QueryValidationException {
    final CQLParserForRMAPI parser = new CQLParserForRMAPI("title=bridget" , 31, 15);
    final ArrayList<String> queries = (ArrayList<String>) parser.getRMAPIQueries();
    assertEquals(2, queries.size());
    assertEquals(1, parser.getInstanceIndex());
    assertEquals("search=bridget&searchfield=titlename&orderby=titlename&count=15&offset=3", queries.get(0));
    assertEquals("search=bridget&searchfield=titlename&orderby=titlename&count=15&offset=4", queries.get(1));
  }
}
