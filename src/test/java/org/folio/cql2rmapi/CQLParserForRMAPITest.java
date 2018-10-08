package org.folio.cql2rmapi;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.z3950.zing.cql.CQLNode;

/**
 * Unit tests for CQLParserForRMAPI class.
 *
 */

public class CQLParserForRMAPITest {

  private static final String VALID_QUERY = "title=bridget";
  private static final String VALID_ISBN_QUERY = "identifier = 12345 sortby title";
  private static final String VALID_FILTER_QUERY = "title = bridget and resourceType = video sortby title";
  private static final String VALID_ADVANCED_BOOLEAN_QUERY = "(title = \"spring OR summer\") and (ext.selected = true) sortby title";
  private static final String VALID_ADVANCED_PHRASE_QUERY = "(title = \"“moby dick”\") and (ext.selected = true) sortby title";
  private static final String VALID_ADVANCED_WILDCARD_QUERY = "(title = comput*) and (ext.selected = true) sortby title";
  private static final String VALID_ADVANCED_NESTED_QUERY = "(title = \"(company AND business) NOT report*\") and (ext.selected = true) sortby title";
  
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
    assertEquals("resourceType", parser.filterType);
    assertEquals("streamingvideo", parser.filterValue);
  }
  
  @Test
  public void CQLParserSetsExpectedValuesIfBooleanQueryIsPassedTest() throws QueryValidationException, UnsupportedEncodingException {
    final CQLParserForRMAPI parser = new CQLParserForRMAPI(VALID_ADVANCED_BOOLEAN_QUERY, 1, 10);
    assertEquals("titlename", parser.searchField);
    assertEquals("spring OR summer", parser.searchValue);
    assertEquals("search=spring+OR+summer&searchfield=titlename&selection=selected&orderby=titlename&count=10&offset=1&searchtype=advanced", parser.getRMAPIQueries().get(0));
  }
  
  @Test
  public void CQLParserSetsExpectedValuesIfPhraseQueryIsPassedTest() throws QueryValidationException, UnsupportedEncodingException {
    final CQLParserForRMAPI parser = new CQLParserForRMAPI(VALID_ADVANCED_PHRASE_QUERY, 1, 10);
    assertEquals("titlename", parser.searchField);
    assertEquals("“moby dick”", parser.searchValue);
    assertEquals("search=%E2%80%9Cmoby+dick%E2%80%9D&searchfield=titlename&selection=selected&orderby=titlename&count=10&offset=1&searchtype=advanced", parser.getRMAPIQueries().get(0));
   }
  
  @Test
  public void CQLParserSetsExpectedValuesIfWildcardQueryIsPassedTest() throws QueryValidationException, UnsupportedEncodingException {
    final CQLParserForRMAPI parser = new CQLParserForRMAPI(VALID_ADVANCED_WILDCARD_QUERY, 1, 10);
    assertEquals("titlename", parser.searchField);
    assertEquals("comput*", parser.searchValue);
    assertEquals("search=comput*&searchfield=titlename&selection=selected&orderby=titlename&count=10&offset=1&searchtype=advanced", parser.getRMAPIQueries().get(0));
  }
  
  @Test
  public void CQLParserSetsExpectedValuesIfNestedQueryIsPassedTest() throws QueryValidationException, UnsupportedEncodingException {
    final CQLParserForRMAPI parser = new CQLParserForRMAPI(VALID_ADVANCED_NESTED_QUERY, 1, 10);
    assertEquals("titlename", parser.searchField);
    assertEquals("(company AND business) NOT report*", parser.searchValue);
    assertEquals("search=%28company+AND+business%29+NOT+report*&searchfield=titlename&selection=selected&orderby=titlename&count=10&offset=1&searchtype=advanced", parser.getRMAPIQueries().get(0));
  }
  
  @Test(expected = QueryValidationException.class)
  public void CQLParserThrowsExceptionIfInvalidFilterValueIsPassedTest() throws QueryValidationException, UnsupportedEncodingException {
    final String invalidFilterQuery = "title = bridget and resourceType = hardcopy sortby title";
    new CQLParserForRMAPI(invalidFilterQuery, 1, 10);
  }

  @Test(expected = QueryValidationException.class)
  public void CQLParserThrowsExceptionIfNullFilterValueIsPassedTest() throws QueryValidationException, UnsupportedEncodingException {
    final String invalidFilterQuery = "title = bridget and resourceType = null sortby title";
    final CQLParserForRMAPI parser = new CQLParserForRMAPI(invalidFilterQuery, 1, 10);
    parser.filterValue = null;
  }

  @Test(expected = QueryValidationException.class)
  public void CQLParserThrowsExceptionIfMultipleFiltersSepartedByAndAreProvidedTest() throws QueryValidationException, UnsupportedEncodingException {
    final String multipleFilterQuery = "title = bridget and resourceType = databases and type = audiobook sortby title";
    new CQLParserForRMAPI(multipleFilterQuery, 1, 10);
  }

  @Test(expected = QueryValidationException.class)
  public void CQLParserThrowsExceptionIfMultipleFiltersSepartedByCommaAreProvidedTest() throws QueryValidationException, UnsupportedEncodingException {
    final String multipleFilterQuery = "title = bridget and resourceType = databases, audiobook sortby title";
    new CQLParserForRMAPI(multipleFilterQuery, 1, 10);
  }

  @Test(expected = QueryValidationException.class)
  public void CQLParserThrowsExceptionIfMultipleFiltersSepartedBySpaceAreProvidedTest() throws QueryValidationException, UnsupportedEncodingException {
    final String multipleFilterQuery = "title = bridget and resourceType = databases audiobook sortby title";
    new CQLParserForRMAPI(multipleFilterQuery, 1, 10);
  }

  @Test(expected = QueryValidationException.class)
  public void CQLParserThrowsExceptionIfMultipleSortKeysAreProvidedTest() throws QueryValidationException, UnsupportedEncodingException {
    final String multipleSortQuery = "title = bridget and resourceType = journal sortby title, publisher";
    new CQLParserForRMAPI(multipleSortQuery, 1, 10);
  }

  @Test(expected = QueryValidationException.class)
  public void CQLParserThrowsExceptionIfSortOnUnsupportedFieldTest() throws QueryValidationException, UnsupportedEncodingException {
    final String multipleSortQuery = "title = bridget and resourceType = journal sortby publisher";
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
    assertEquals("search=bridget&searchfield=titlename&orderby=titlename&count=10&offset=2&searchtype=advanced", parser.getRMAPIQueries().get(0));
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
  public void CQLParserMapsIdentifierToISXNTest() throws QueryValidationException, UnsupportedEncodingException {
    final CQLParserForRMAPI parser = new CQLParserForRMAPI(VALID_ISBN_QUERY, 0, 10);
    assertEquals("search=12345&searchfield=isxn&orderby=titlename&count=10&offset=1&searchtype=advanced", parser.getRMAPIQueries().get(0));
  }

  @Test
  public void CQLParserPassesAlongOffsetIfNotEqualToZeroTest() throws QueryValidationException, UnsupportedEncodingException {
    final CQLParserForRMAPI parser = new CQLParserForRMAPI(VALID_ISBN_QUERY, 10, 10);
    assertEquals("search=12345&searchfield=isxn&orderby=titlename&count=10&offset=2&searchtype=advanced", parser.getRMAPIQueries().get(0));
  }

  @Test
  public void CQLParserDoesNotAppendResourceTypeIfTypeNotPassedAlongTest() throws QueryValidationException, UnsupportedEncodingException {
    final CQLParserForRMAPI parser = new CQLParserForRMAPI(VALID_ISBN_QUERY, 10, 10);
    assertEquals("search=12345&searchfield=isxn&orderby=titlename&count=10&offset=2&searchtype=advanced", parser.getRMAPIQueries().get(0));
  }

  @Test
  public void CQLParserDoesNotAppendResourceTypeIfFilterTypeIsNullTest() throws QueryValidationException, UnsupportedEncodingException {
    final CQLParserForRMAPI parser = new CQLParserForRMAPI(VALID_FILTER_QUERY, 10, 10);
    parser.filterType = null;
    assertEquals("search=bridget&searchfield=titlename&orderby=titlename&count=10&offset=1&searchtype=advanced", parser.buildRMAPIQuery(10, 1));
  }

  @Test
  public void CQLParserDoesNotAppendResourceTypeIfFilTerValueIsNullTest() throws QueryValidationException, UnsupportedEncodingException {
    final CQLParserForRMAPI parser = new CQLParserForRMAPI(VALID_FILTER_QUERY, 10, 10);
    parser.filterValue = null;
    assertEquals("search=bridget&searchfield=titlename&orderby=titlename&count=10&offset=1&searchtype=advanced", parser.buildRMAPIQuery(10, 1));
  }

  @Test
  public void CQLParserAppendsResourceTypeIfFilterTypeAndFilTerValueAreValidTest() throws QueryValidationException, UnsupportedEncodingException {
    final CQLParserForRMAPI parser = new CQLParserForRMAPI(VALID_FILTER_QUERY, 10, 10);
    assertEquals("search=bridget&searchfield=titlename&resourcetype=streamingvideo&orderby=titlename&count=10&offset=2&searchtype=advanced",
        parser.getRMAPIQueries().get(0));
  }

  @Test(expected = QueryValidationException.class)
  public void CQLParserThrowsExceptionIfMultipleSearchFieldsAreProvidedTest() throws UnsupportedEncodingException, QueryValidationException {
    new CQLParserForRMAPI("title=something AND publisher=EBSCO" , 10, 10);
  }

  @Test
  public void CQLParserSetsExpectedValuesInCaseOfValidSearchTest() throws UnsupportedEncodingException, QueryValidationException {
    final CQLParserForRMAPI parser = new CQLParserForRMAPI("resourceType=reports AND title=bridget" , 10, 10);
    assertEquals("titlename", parser.searchField);
    assertEquals("bridget", parser.searchValue);
    assertEquals("resourceType", parser.filterType);
    assertEquals("report", parser.filterValue);
  }

  @Test
  public void CQLParserSetsExpectedValuesInCaseOfValidISXNsearchTest() throws UnsupportedEncodingException, QueryValidationException {
    final CQLParserForRMAPI parser = new CQLParserForRMAPI("identifier=12345" , 10, 10);
    assertEquals("isxn", parser.searchField);
    assertEquals("12345", parser.searchValue);
  }

  @Test
  public void CQLParserSetsExpectedValuesInCaseOfValidISBNsearchTest() throws UnsupportedEncodingException, QueryValidationException {
    final CQLParserForRMAPI parser = new CQLParserForRMAPI("identifier=12345" , 10, 10);
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
      assertEquals("search=bridget&searchfield=titlename&orderby=titlename&count=100&offset=10&searchtype=advanced", query);
    }
  }

  @Test
  public void CQLParserComputesMultipleQueriesAndIndexForRMAPICorrectlyTest() throws UnsupportedEncodingException, QueryValidationException {
    final CQLParserForRMAPI parser = new CQLParserForRMAPI("title=bridget" , 31, 15);
    final ArrayList<String> queries = (ArrayList<String>) parser.getRMAPIQueries();
    assertEquals(2, queries.size());
    assertEquals(1, parser.getInstanceIndex());
    assertEquals("search=bridget&searchfield=titlename&orderby=titlename&count=15&offset=3&searchtype=advanced", queries.get(0));
    assertEquals("search=bridget&searchfield=titlename&orderby=titlename&count=15&offset=4&searchtype=advanced", queries.get(1));
  }

  @Test(expected = QueryValidationException.class)
  public void CQLParserThrowsExceptionIfsourceIsNotSupportedTest() throws QueryValidationException, UnsupportedEncodingException {
    new CQLParserForRMAPI("title=bridget and source=local", 0, 1);
  }

  @Test
  public void CQLParserSetExpectedValuesIfsourceIsAllTest() throws QueryValidationException, UnsupportedEncodingException {
    final CQLParserForRMAPI parser = new CQLParserForRMAPI("title=bridget and source=all", 0, 1);
    assertEquals("titlename", parser.searchField);
    assertEquals("bridget", parser.searchValue);
  }

  @Test
  public void CQLParserSetExpectedValuesIfsourceIsKbTest() throws QueryValidationException, UnsupportedEncodingException {
    final CQLParserForRMAPI parser = new CQLParserForRMAPI("title=bridget and source=kb", 0, 1);
    assertEquals("titlename", parser.searchField);
    assertEquals("bridget", parser.searchValue);
  }

  @Test(expected = QueryValidationException.class)
  public void CQLParserThrowsExceptionIfselectionIsNotSupportedTest() throws QueryValidationException, UnsupportedEncodingException {
    new CQLParserForRMAPI("title=bridget and selected=yes", 0, 1);
  }

  @Test
  public void CQLParserReturnsExpectedQueriesIfSelectionIsSetToTrueTest() throws QueryValidationException, UnsupportedEncodingException {
    final CQLParserForRMAPI parser = new CQLParserForRMAPI("title=bridget and resourceType = video and ext.selected=true" , 900, 100);
    final ArrayList<String> queries = (ArrayList<String>) parser.getRMAPIQueries();
    assertEquals(1, queries.size());
    assertEquals(0, parser.getInstanceIndex());
    for (final String query: queries) {
      assertEquals("search=bridget&searchfield=titlename&resourcetype=streamingvideo&selection=selected&orderby=titlename&count=100&offset=10&searchtype=advanced", query);
    }
  }

  @Test
  public void CQLParserReturnsExpectedQueriesIfSelectionIsSetToAllTest() throws QueryValidationException, UnsupportedEncodingException {
    final CQLParserForRMAPI parser = new CQLParserForRMAPI("title=bridget and resourceType = databases and ext.selected=all" , 900, 100);
    final ArrayList<String> queries = (ArrayList<String>) parser.getRMAPIQueries();
    assertEquals(1, queries.size());
    assertEquals(0, parser.getInstanceIndex());
    for (final String query: queries) {
      assertEquals("search=bridget&searchfield=titlename&resourcetype=database&selection=all&orderby=titlename&count=100&offset=10&searchtype=advanced", query);
    }
  }

  @Test
  public void CQLParserReturnsExpectedQueriesIfSelectionIsSetToFalseTest() throws QueryValidationException, UnsupportedEncodingException {
    final CQLParserForRMAPI parser = new CQLParserForRMAPI("title=bridget and resourceType = video and ext.selected=false" , 900, 100);
    final ArrayList<String> queries = (ArrayList<String>) parser.getRMAPIQueries();
    assertEquals(1, queries.size());
    assertEquals(0, parser.getInstanceIndex());
    for (final String query: queries) {
      assertEquals("search=bridget&searchfield=titlename&resourcetype=streamingvideo&selection=notselected&orderby=titlename&count=100&offset=10&searchtype=advanced", query);
    }
  }

  @Test(expected = QueryValidationException.class)
  public void CQLParserThrowsExceptionIfSelectionIsInvalidTest() throws QueryValidationException, UnsupportedEncodingException {
    new CQLParserForRMAPI("title=bridget and resourceType = databases and ext.selected=yes" , 900, 100);
  }

  @Test(expected = QueryValidationException.class)
  public void CQLParserThrowsExceptionIfAllRecordsAreRequestedTest() throws QueryValidationException, UnsupportedEncodingException {
    new CQLParserForRMAPI("cql.allRecords=all" , 900, 100);
  }

  @Test
  public void CQLParserReturnsExpectedQueriesIfCodexTitleIsPassedTest() throws QueryValidationException, UnsupportedEncodingException {
    final CQLParserForRMAPI parser = new CQLParserForRMAPI("codex.title=bridget and resourceType = audiobooks and ext.selected=false" , 900, 100);
    final ArrayList<String> queries = (ArrayList<String>) parser.getRMAPIQueries();
    assertEquals(1, queries.size());
    assertEquals(0, parser.getInstanceIndex());
    for (final String query: queries) {
      assertEquals("search=bridget&searchfield=titlename&resourcetype=audiobook&selection=notselected&orderby=titlename&count=100&offset=10&searchtype=advanced", query);
    }
  }

  @Test
  public void CQLParserReturnsExpectedQueriesIfCodexIdentifierIsPassedTest() throws QueryValidationException, UnsupportedEncodingException {
    final CQLParserForRMAPI parser = new CQLParserForRMAPI("codex.identifier=12345 and resourceType = databases and ext.selected=false" , 900, 100);
    final ArrayList<String> queries = (ArrayList<String>) parser.getRMAPIQueries();
    assertEquals(1, queries.size());
    assertEquals(0, parser.getInstanceIndex());
    for (final String query: queries) {
      assertEquals("search=12345&searchfield=isxn&resourcetype=database&selection=notselected&orderby=titlename&count=100&offset=10&searchtype=advanced", query);
    }
  }

  @Test
  public void CQLParserReturnsExpectedQueriesIfCodexPublisherIsPassedTest() throws QueryValidationException, UnsupportedEncodingException {
    final CQLParserForRMAPI parser = new CQLParserForRMAPI("codex.publisher=ebsco and resourceType = databases and ext.selected=false" , 900, 100);
    final ArrayList<String> queries = (ArrayList<String>) parser.getRMAPIQueries();
    assertEquals(1, queries.size());
    assertEquals(0, parser.getInstanceIndex());
    for (final String query: queries) {
      assertEquals("search=ebsco&searchfield=publisher&resourcetype=database&selection=notselected&orderby=titlename&count=100&offset=10&searchtype=advanced", query);
    }
  }

  @Test
  public void CQLParserReturnsExpectedQueriesIfPublisherIsPassedTest() throws QueryValidationException, UnsupportedEncodingException {
    final CQLParserForRMAPI parser = new CQLParserForRMAPI("publisher=ebsco and resourceType = video and ext.selected=false" , 900, 100);
    final ArrayList<String> queries = (ArrayList<String>) parser.getRMAPIQueries();
    assertEquals(1, queries.size());
    assertEquals(0, parser.getInstanceIndex());
    for (final String query: queries) {
      assertEquals("search=ebsco&searchfield=publisher&resourcetype=streamingvideo&selection=notselected&orderby=titlename&count=100&offset=10&searchtype=advanced", query);
    }
  }

  @Test
  public void CQLParserReturnsExpectedQueriesIfCodexSubjectIsPassedTest() throws QueryValidationException, UnsupportedEncodingException {
    final CQLParserForRMAPI parser = new CQLParserForRMAPI("codex.subject=history and resourceType = databases and ext.selected=false" , 900, 100);
    final ArrayList<String> queries = (ArrayList<String>) parser.getRMAPIQueries();
    assertEquals(1, queries.size());
    assertEquals(0, parser.getInstanceIndex());
    for (final String query: queries) {
      assertEquals("search=history&searchfield=subject&resourcetype=database&selection=notselected&orderby=titlename&count=100&offset=10", query);
    }
  }

  @Test
  public void CQLParserReturnsExpectedQueriesIfSubjectIsPassedTest() throws QueryValidationException, UnsupportedEncodingException {
    final CQLParserForRMAPI parser = new CQLParserForRMAPI("subject=history and resourceType = video and ext.selected=false" , 900, 100);
    final ArrayList<String> queries = (ArrayList<String>) parser.getRMAPIQueries();
    assertEquals(1, queries.size());
    assertEquals(0, parser.getInstanceIndex());
    for (final String query: queries) {
      assertEquals("search=history&searchfield=subject&resourcetype=streamingvideo&selection=notselected&orderby=titlename&count=100&offset=10", query);
    }
  }
  @Test(expected = QueryValidationException.class)
  public void CQLParserThrowsExceptionIfInvalidSearchFieldWithPrefixIsPassedTest() throws QueryValidationException, UnsupportedEncodingException {
    new CQLParserForRMAPI("codex.resourceType=12345" , 900, 100);
  }

  @Test
  public void CQLParserReturnsCorrectIdValueIfPassed() throws UnsupportedEncodingException, QueryValidationException {
    final CQLParserForRMAPI parser = new CQLParserForRMAPI("id=12345" , 0, 1);
    assertTrue(parser.isIDSearchField());
    assertEquals("12345", parser.getID());
  }

  @Test
  public void CQLParserUnknownExt() throws Exception {
    final CQLParserForRMAPI parser = new CQLParserForRMAPI("title=xyzzy and ext.available=all", 0, 10);
    final List<String> queries = parser.getRMAPIQueries();
    assertEquals(1, queries.size());
    String query = queries.get(0);
    assertNotNull(query);
    assertThat(query, stringContainsInOrder(Arrays.asList("search=xyzzy", "searchfield=title")));
  }

  @Test
  public void CQLParserSelectedNotPresent() throws Exception {
    final CQLParserForRMAPI parser = new CQLParserForRMAPI("title=xyzzy", 0, 10);
    final List<String> queries = parser.getRMAPIQueries();
    assertEquals(1, queries.size());
    String query = queries.get(0);
    assertNotNull(query);
    assertThat(query, not(containsString("selection=")));
  }
}
