package org.folio.cql2rmapi;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

/**
 * Unit tests for CQLParserForRMAPI class.
 *
 */

public class CQLParserForRMAPITest {

  private static final String VALID_QUERY = "title=bridget";
  private static final String VALID_ISBN_QUERY = "identifier = 12345 sortby title";
  private static final String VALID_FILTER_QUERY = "title = bridget and resourceType = video sortby title";
  private static final String VALID_ADVANCED_BOOLEAN_QUERY = "(title = \"spring OR summer\") and (ext.selected = true) sortby title";
  private static final String VALID_ADVANCED_PHRASE_QUERY = "(title = \"\"great gatsby\"\") and (ext.selected = true) sortby title";
  private static final String VALID_ADVANCED_WILDCARD_QUERY = "(title = comput*) and (ext.selected = true) sortby title";
  private static final String VALID_ADVANCED_NESTED_QUERY = "(title = \"(company AND business) NOT report*\") and (ext.selected = true) sortby title";

  @Test(expected = QueryValidationException.class)
  public void cqlParametersThrowsExceptionIfQueryInvalidTest() throws QueryValidationException {
    final String invalidQuery = "";
    new CQLParameters(invalidQuery);
  }

  @Test(expected = QueryValidationException.class)
  public void titleParametersThrowsExceptionIfQueryIsInvalidTest() throws QueryValidationException {
    final String invalidQuery = "offset=1&limit=10";
    new TitleParameters(new CQLParameters(invalidQuery));
  }

  @Test(expected = QueryValidationException.class)
  public void cqlParserForRMAPIThrowsExceptionIfQueryIsNullTest() throws QueryValidationException, UnsupportedEncodingException {
    final String invalidQuery = null;
    new CQLParserForRMAPI(invalidQuery, 1, 10);
  }

  @Test
  public void cqlParametersParsesQueryIfQueryValidTest() throws QueryValidationException {
    new CQLParameters(VALID_QUERY);
  }

  @Test(expected = QueryValidationException.class)
  public void CQLParametersThrowsExceptionInCaseOfUnsupportedOperatorTest() throws QueryValidationException {
    final String invalidOperator = "title<>bridget";
    new CQLParameters(invalidOperator);
  }

  @Test
  public void TitleParametersSetsExpectedValuesIfSearchFieldNotProvidedTest() throws QueryValidationException {
    final String validQuery = "bridget";
    TitleParameters parameters = new TitleParameters(new CQLParameters(validQuery));
    assertEquals("titlename", parameters.getSearchField());
    assertEquals("bridget", parameters.getSearchValue());
  }

  @Test
  public void TitleParametersSetsExpectedValuesIfValidSearchFieldIsProvidedTest() throws QueryValidationException {
    TitleParameters parameters = new TitleParameters(new CQLParameters(VALID_ISBN_QUERY));
    assertEquals("isxn", parameters.getSearchField());
    assertEquals("12345", parameters.getSearchValue());
    assertEquals("titlename", parameters.getSortType());
  }

  @Test(expected = QueryValidationException.class)
  public void TitleParametersThrowsExceptionInCaseOfUnsupportedSearchFieldTest() throws QueryValidationException {
    final String invalidOperator = "author=bridget";
    new TitleParameters(new CQLParameters(invalidOperator));
  }

  @Test
  public void TitleParametersSetsExpectedValuesIfTypeFilterIsPassedTest() throws QueryValidationException {
    TitleParameters parameters = new TitleParameters(new CQLParameters(VALID_FILTER_QUERY));
    assertEquals("titlename", parameters.getSearchField());
    assertEquals("bridget", parameters.getSearchValue());
    assertEquals("titlename", parameters.getSortType());
    assertEquals("resourceType", parameters.getFilterType());
    assertEquals("streamingvideo", parameters.getFilterValue());
  }

  @Test
  public void TitleParametersSetsExpectedValuesIfBooleanQueryIsPassedTest() throws QueryValidationException, UnsupportedEncodingException {
    TitleParameters parameters = new TitleParameters(new CQLParameters(VALID_ADVANCED_BOOLEAN_QUERY));
    assertEquals("titlename", parameters.getSearchField());
    assertEquals("spring OR summer", parameters.getSearchValue());

    CQLParserForRMAPI parser = new CQLParserForRMAPI(VALID_ADVANCED_BOOLEAN_QUERY, 1, 10);
    assertEquals("search=spring+OR+summer&searchfield=titlename&selection=selected&orderby=titlename&count=10&offset=1&searchtype=advanced", parser.getRMAPIQueries().get(0));
  }

  @Ignore("Test Pending - CQL parser handling of quoted query")
  @Test
  public void TitleParametersSetsExpectedValuesIfPhraseQuery2IsPassedTest() throws QueryValidationException, UnsupportedEncodingException {
    TitleParameters parameters = new TitleParameters(new CQLParameters(VALID_ADVANCED_PHRASE_QUERY));
    assertEquals("titlename", parameters.getSearchField());
    assertEquals("\"great gatsby\"", parameters.getSearchValue());

    CQLParserForRMAPI parser = new CQLParserForRMAPI(VALID_ADVANCED_PHRASE_QUERY, 1, 10);
    assertEquals("search=%22great+gatsby%22&searchfield=titlename&selection=selected&orderby=titlename&count=10&offset=1&searchtype=advanced", parser.getRMAPIQueries().get(0));
   }

  @Test
  public void TitleParametersSetsExpectedValuesIfWildcardQueryIsPassedTest() throws QueryValidationException, UnsupportedEncodingException {
    TitleParameters parameters = new TitleParameters(new CQLParameters(VALID_ADVANCED_WILDCARD_QUERY));
    assertEquals("titlename", parameters.getSearchField());
    assertEquals("comput*", parameters.getSearchValue());

    CQLParserForRMAPI parser = new CQLParserForRMAPI(VALID_ADVANCED_WILDCARD_QUERY, 1, 10);
    assertEquals("search=comput*&searchfield=titlename&selection=selected&orderby=titlename&count=10&offset=1&searchtype=advanced", parser.getRMAPIQueries().get(0));
  }

  @Test
  public void TitleParametersSetsExpectedValuesIfNestedQueryIsPassedTest() throws QueryValidationException, UnsupportedEncodingException {
    TitleParameters parameters = new TitleParameters(new CQLParameters(VALID_ADVANCED_NESTED_QUERY));
    assertEquals("titlename", parameters.getSearchField());
    assertEquals("(company AND business) NOT report*", parameters.getSearchValue());

    CQLParserForRMAPI parser = new CQLParserForRMAPI(VALID_ADVANCED_NESTED_QUERY, 1, 10);
    assertEquals("search=%28company+AND+business%29+NOT+report*&searchfield=titlename&selection=selected&orderby=titlename&count=10&offset=1&searchtype=advanced", parser.getRMAPIQueries().get(0));
  }

  @Test(expected = QueryValidationException.class)
  public void TitleParametersThrowsExceptionIfInvalidFilterValueIsPassedTest() throws QueryValidationException {
    new TitleParameters(new CQLParameters("title = bridget and resourceType = hardcopy sortby title"));
  }

  @Test(expected = QueryValidationException.class)
  public void TitleParametersThrowsExceptionIfNullFilterValueIsPassedTest() throws QueryValidationException {
    new TitleParameters(new CQLParameters("title = bridget and resourceType = null sortby title"));
  }

  @Test(expected = QueryValidationException.class)
  public void TitleParametersThrowsExceptionIfMultipleFiltersSepartedByAndAreProvidedTest() throws QueryValidationException {
    new TitleParameters(new CQLParameters("title = bridget and resourceType = databases and resourceType = audiobook sortby title"));
  }

  @Test(expected = QueryValidationException.class)
  public void TitleParametersThrowsExceptionIfMultipleFiltersSepartedByCommaAreProvidedTest() throws QueryValidationException {
    new TitleParameters(new CQLParameters("title = bridget and resourceType = databases, audiobook sortby title"));
  }

  @Test(expected = QueryValidationException.class)
  public void TitleParametersThrowsExceptionIfMultipleFiltersSepartedBySpaceAreProvidedTest() throws QueryValidationException {
    new TitleParameters(new CQLParameters("title = bridget and resourceType = databases audiobook sortby title"));
  }

  @Test(expected = QueryValidationException.class)
  public void TitleParametersThrowsExceptionIfMultipleSortKeysAreProvidedTest() throws QueryValidationException {
    new TitleParameters(new CQLParameters("title = bridget and resourceType = journal sortby title, publisher"));
  }

  @Test(expected = QueryValidationException.class)
  public void TitleParametersThrowsExceptionIfSortOnUnsupportedFieldTest() throws QueryValidationException {
    new TitleParameters(new CQLParameters("title = bridget and resourceType = journal sortby publisher"));
  }

  @Test(expected = QueryValidationException.class)
  public void TitleParametersThrowsExceptionIfUnsupportedBooleanOperatorIsPassedTest() throws QueryValidationException {
    new TitleParameters(new CQLParameters("title = bridget OR publisher = Ebsco sortby title"));
  }

  @Test
  public void CQLParserReturnsExpectedQueryStringIfValidQueryIsPassedTest()
      throws QueryValidationException, UnsupportedEncodingException {
    final CQLParserForRMAPI parser = new CQLParserForRMAPI(VALID_QUERY, 10, 10);
    assertEquals("search=bridget&searchfield=titlename&orderby=titlename&count=10&offset=2&searchtype=advanced", parser.getRMAPIQueries().get(0));
  }

  @Test(expected = QueryValidationException.class)
  public void TitleParametersThrowsExceptionIfSearchValueAndSearchFieldAreNullTest() throws QueryValidationException {
    new TitleParameters(new CQLParameters(Collections.emptyMap(), "title", false, null));
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
    assertEquals("search=bridget&searchfield=titlename&orderby=titlename&count=10&offset=1&searchtype=advanced",
      parser.buildRMAPIQuery(10, 1,
        new TitleParameters("titlename", "bridget", null, "filterValue", "titlename", null )));
  }

  @Test
  public void CQLParserDoesNotAppendResourceTypeIfFilterValueIsNullTest() throws QueryValidationException, UnsupportedEncodingException {
    final CQLParserForRMAPI parser = new CQLParserForRMAPI(VALID_FILTER_QUERY, 10, 10);
    assertEquals("search=bridget&searchfield=titlename&orderby=titlename&count=10&offset=1&searchtype=advanced", parser.buildRMAPIQuery(10, 1,
      new TitleParameters("titlename", "bridget", "resourceType", null, "titlename", null )));
  }

  @Test
  public void CQLParserAppendsResourceTypeIfFilterTypeAndFilterValueAreValidTest() throws QueryValidationException, UnsupportedEncodingException {
    final CQLParserForRMAPI parser = new CQLParserForRMAPI(VALID_FILTER_QUERY, 10, 10);
    assertEquals("search=bridget&searchfield=titlename&resourcetype=streamingvideo&orderby=titlename&count=10&offset=2&searchtype=advanced",
        parser.getRMAPIQueries().get(0));
  }

  @Test(expected = QueryValidationException.class)
  public void TitleParametersThrowsExceptionIfMultipleSearchFieldsAreProvidedTest() throws QueryValidationException {
    new TitleParameters(new CQLParameters("title=something AND publisher=EBSCO"));
  }

  @Test
  public void TitleParametersSetsExpectedValuesInCaseOfValidSearchTest() throws QueryValidationException {
    TitleParameters parameters = new TitleParameters(new CQLParameters("resourceType=reports AND title=bridget"));
    assertEquals("titlename", parameters.getSearchField());
    assertEquals("bridget", parameters.getSearchValue());
    assertEquals("resourceType", parameters.getFilterType());
    assertEquals("report", parameters.getFilterValue());
  }

  @Test
  public void TitleParametersSetsExpectedValuesInCaseOfValidISXNsearchTest() throws QueryValidationException {
    TitleParameters parameters = new TitleParameters(new CQLParameters("identifier=12345"));
    assertEquals("isxn", parameters.getSearchField());
    assertEquals("12345", parameters.getSearchValue());
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
  public void TitleParametersSetsExpectedValuesIfSourceIsAllTest() throws QueryValidationException {
    TitleParameters parameters = new TitleParameters(new CQLParameters("title=bridget and source=all"));
    assertEquals("titlename", parameters.getSearchField());
    assertEquals("bridget", parameters.getSearchValue());
  }

  @Test
  public void TitleParametersSetsExpectedValuesIfSourceIsKbTest() throws QueryValidationException {
    TitleParameters parameters = new TitleParameters(new CQLParameters("title=bridget and source=kb"));
    assertEquals("titlename", parameters.getSearchField());
    assertEquals("bridget", parameters.getSearchValue());
  }

  @Test(expected = QueryValidationException.class)
  public void CQLParserThrowsExceptionIfselectionIsNotSupportedTest() throws QueryValidationException, UnsupportedEncodingException {
    new CQLParserForRMAPI("title=bridget and ext.selected=yes", 0, 1);
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
      assertEquals("search=history&searchfield=subject&resourcetype=database&selection=notselected&orderby=titlename&count=100&offset=10&searchtype=advanced", query);
    }
  }

  @Test
  public void CQLParserReturnsExpectedQueriesIfSubjectIsPassedTest() throws QueryValidationException, UnsupportedEncodingException {
    final CQLParserForRMAPI parser = new CQLParserForRMAPI("subject=history and resourceType = video and ext.selected=false" , 900, 100);
    final ArrayList<String> queries = (ArrayList<String>) parser.getRMAPIQueries();
    assertEquals(1, queries.size());
    assertEquals(0, parser.getInstanceIndex());
    for (final String query: queries) {
      assertEquals("search=history&searchfield=subject&resourcetype=streamingvideo&selection=notselected&orderby=titlename&count=100&offset=10&searchtype=advanced", query);
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
