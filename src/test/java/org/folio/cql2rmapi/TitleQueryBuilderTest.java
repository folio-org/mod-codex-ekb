package org.folio.cql2rmapi;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.folio.cql2rmapi.query.RMAPIQueries;
import org.folio.cql2rmapi.query.TitlesQueryBuilder;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Unit tests for CQLParserForRMAPI class.
 */

public class TitleQueryBuilderTest {

  private static final String VALID_QUERY = "title=bridget";
  private static final String VALID_ISBN_QUERY = "identifier = 12345 sortby title";
  private static final String VALID_FILTER_QUERY = "title = bridget and resourceType = video sortby title";
  private static final String VALID_ADVANCED_BOOLEAN_QUERY = "(title = \"spring OR summer\") and (ext.selected = true) sortby title";
  private static final String VALID_ADVANCED_PHRASE_QUERY = "(title = \"\"great gatsby\"\") and (ext.selected = true) sortby title";
  private static final String VALID_ADVANCED_WILDCARD_QUERY = "(title = comput*) and (ext.selected = true) sortby title";
  private static final String VALID_ADVANCED_NESTED_QUERY = "(title = \"(company AND business) NOT report*\") and (ext.selected = true) sortby title";
  private static final String SEARCH_VALUE = "bridget";
  private static final String SEARCH_FIELD = "titlename";
  private static final String IDENTIFIER_VALUE = "12345";

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

  @Test
  public void cqlParametersParsesQueryIfQueryValidTest() throws QueryValidationException {
    CQLParameters parameters = new CQLParameters(VALID_QUERY);
    assertFalse(parameters.getParameters().isEmpty());
  }

  @Test(expected = QueryValidationException.class)
  public void cqlParametersThrowsExceptionInCaseOfUnsupportedOperatorTest() throws QueryValidationException {
    final String invalidOperator = "title<>bridget";
    new CQLParameters(invalidOperator);
  }

  @Test
  public void titleParametersSetsExpectedValuesIfSearchFieldNotProvidedTest() throws QueryValidationException {
    TitleParameters parameters = new TitleParameters(new CQLParameters(SEARCH_VALUE));
    assertEquals(SEARCH_FIELD, parameters.getSearchField());
    assertEquals(SEARCH_VALUE, parameters.getSearchValue());
  }

  @Test
  public void titleParametersSetsExpectedValuesIfValidSearchFieldIsProvidedTest() throws QueryValidationException {
    TitleParameters parameters = new TitleParameters(new CQLParameters(VALID_ISBN_QUERY));
    assertEquals("isxn", parameters.getSearchField());
    assertEquals(IDENTIFIER_VALUE, parameters.getSearchValue());
    assertEquals(SEARCH_FIELD, parameters.getSortType());
  }

  @Test(expected = QueryValidationException.class)
  public void titleParametersThrowsExceptionInCaseOfUnsupportedSearchFieldTest() throws QueryValidationException {
    final String invalidOperator = "author=bridget";
    new TitleParameters(new CQLParameters(invalidOperator));
  }

  @Test
  public void titleParametersSetsExpectedValuesIfTypeFilterIsPassedTest() throws QueryValidationException {
    TitleParameters parameters = new TitleParameters(new CQLParameters(VALID_FILTER_QUERY));
    assertEquals(SEARCH_FIELD, parameters.getSearchField());
    assertEquals(SEARCH_VALUE, parameters.getSearchValue());
    assertEquals(SEARCH_FIELD, parameters.getSortType());
    assertEquals("streamingvideo", parameters.getFilterValue());
  }

  @Test
  public void titleParametersSetsExpectedValuesIfBooleanQueryIsPassedTest() throws QueryValidationException, UnsupportedEncodingException {
    TitleParameters parameters = new TitleParameters(new CQLParameters(VALID_ADVANCED_BOOLEAN_QUERY));
    assertEquals(SEARCH_FIELD, parameters.getSearchField());
    assertEquals("spring OR summer", parameters.getSearchValue());


    RMAPIQueries queries = new TitlesQueryBuilder().build(parameters, 1, 10);
    assertEquals("searchfield=titlename&selection=selected&search=spring+OR+summer&orderby=titlename&count=10&offset=1&searchtype=advanced", queries.getRMAPIQueries().get(0));
  }

  @Ignore("Test Pending - CQL parser handling of quoted query")
  @Test
  public void titleParametersSetsExpectedValuesIfPhraseQuery2IsPassedTest() throws QueryValidationException, UnsupportedEncodingException {
    TitleParameters parameters = new TitleParameters(new CQLParameters(VALID_ADVANCED_PHRASE_QUERY));
    assertEquals(SEARCH_FIELD, parameters.getSearchField());
    assertEquals("\"great gatsby\"", parameters.getSearchValue());

    RMAPIQueries queries = new TitlesQueryBuilder().build(parameters, 1, 10);
    assertEquals("searchfield=titlename&selection=selected&search=%22great+gatsby%22&orderby=titlename&count=10&offset=1&searchtype=advanced", queries.getRMAPIQueries().get(0));
   }

  @Test
  public void titleParametersSetsExpectedValuesIfWildcardQueryIsPassedTest() throws QueryValidationException, UnsupportedEncodingException {
    TitleParameters parameters = new TitleParameters(new CQLParameters(VALID_ADVANCED_WILDCARD_QUERY));
    assertEquals(SEARCH_FIELD, parameters.getSearchField());
    assertEquals("comput*", parameters.getSearchValue());

    RMAPIQueries queries = new TitlesQueryBuilder().build(parameters, 1, 10);
    assertEquals("searchfield=titlename&selection=selected&search=comput*&orderby=titlename&count=10&offset=1&searchtype=advanced", queries.getRMAPIQueries().get(0));
  }

  @Test
  public void titleParametersSetsExpectedValuesIfNestedQueryIsPassedTest() throws QueryValidationException, UnsupportedEncodingException {
    TitleParameters parameters = new TitleParameters(new CQLParameters(VALID_ADVANCED_NESTED_QUERY));
    assertEquals(SEARCH_FIELD, parameters.getSearchField());
    assertEquals("(company AND business) NOT report*", parameters.getSearchValue());

    RMAPIQueries queries = new TitlesQueryBuilder().build(parameters, 1, 10);
    assertEquals("searchfield=titlename&selection=selected&search=%28company+AND+business%29+NOT+report*&orderby=titlename&count=10&offset=1&searchtype=advanced", queries.getRMAPIQueries().get(0));
  }

  @Test(expected = QueryValidationException.class)
  public void titleParametersThrowsExceptionIfInvalidFilterValueIsPassedTest() throws QueryValidationException {
    new TitleParameters(new CQLParameters("title = bridget and resourceType = hardcopy sortby title"));
  }

  @Test(expected = QueryValidationException.class)
  public void titleParametersThrowsExceptionIfNullFilterValueIsPassedTest() throws QueryValidationException {
    new TitleParameters(new CQLParameters("title = bridget and resourceType = null sortby title"));
  }

  @Test(expected = QueryValidationException.class)
  public void titleParametersThrowsExceptionIfMultipleFiltersSepartedByAndAreProvidedTest() throws QueryValidationException {
    new TitleParameters(new CQLParameters("title = bridget and resourceType = databases and resourceType = audiobooks sortby title"));
  }

  @Test(expected = QueryValidationException.class)
  public void titleParametersThrowsExceptionIfMultipleFiltersSepartedByCommaAreProvidedTest() throws QueryValidationException {
    new TitleParameters(new CQLParameters("title = bridget and resourceType = databases, audiobooks sortby title"));
  }

  @Test(expected = QueryValidationException.class)
  public void titleParametersThrowsExceptionIfMultipleFiltersSepartedBySpaceAreProvidedTest() throws QueryValidationException {
    new TitleParameters(new CQLParameters("title = bridget and resourceType = databases audiobooks sortby title"));
  }

  @Test(expected = QueryValidationException.class)
  public void titleParametersThrowsExceptionIfMultipleSortKeysAreProvidedTest() throws QueryValidationException {
    new TitleParameters(new CQLParameters("title = bridget and resourceType = journal sortby title, publisher"));
  }

  @Test(expected = QueryValidationException.class)
  public void titleParametersThrowsExceptionIfSortOnUnsupportedFieldTest() throws QueryValidationException {
    new TitleParameters(new CQLParameters("title = bridget and resourceType = journal sortby publisher"));
  }

  @Test(expected = QueryValidationException.class)
  public void titleParametersThrowsExceptionIfUnsupportedBooleanOperatorIsPassedTest() throws QueryValidationException {
    new TitleParameters(new CQLParameters("title = bridget OR publisher = Ebsco sortby title"));
  }

  @Test
  public void cqlParserReturnsExpectedQueryStringIfValidQueryIsPassedTest()
      throws QueryValidationException, UnsupportedEncodingException {
    RMAPIQueries queries = new TitlesQueryBuilder().build(new TitleParameters(new CQLParameters(VALID_QUERY)), 10, 10);
    assertEquals("searchfield=titlename&search=bridget&orderby=titlename&count=10&offset=2&searchtype=advanced", queries.getRMAPIQueries().get(0));
  }

  @Test(expected = QueryValidationException.class)
  public void titleParametersThrowsExceptionIfSearchValueAndSearchFieldAreNullTest() throws QueryValidationException {
    new TitleParameters(new CQLParameters(Collections.emptyMap(), "title", false, null));
  }

  @Test
  public void cqlParserMapsIdentifierToISXNTest() throws QueryValidationException, UnsupportedEncodingException {
    RMAPIQueries queries = new TitlesQueryBuilder().build(new TitleParameters(new CQLParameters(VALID_ISBN_QUERY)), 0, 10);
    assertEquals("searchfield=isxn&search=12345&orderby=titlename&count=10&offset=1&searchtype=advanced", queries.getRMAPIQueries().get(0));
  }

  @Test
  public void cqlParserPassesAlongOffsetIfNotEqualToZeroTest() throws QueryValidationException, UnsupportedEncodingException {
    RMAPIQueries queries = new TitlesQueryBuilder().build(new TitleParameters(new CQLParameters(VALID_ISBN_QUERY)), 10, 10);
    assertEquals("searchfield=isxn&search=12345&orderby=titlename&count=10&offset=2&searchtype=advanced", queries.getRMAPIQueries().get(0));
  }

  @Test
  public void cqlParserDoesNotAppendResourceTypeIfFilterValueIsNullTest() throws UnsupportedEncodingException {
    TitlesQueryBuilder builder = new TitlesQueryBuilder();
    assertEquals("searchfield=titlename&search=bridget&orderby=titlename&count=10&offset=1&searchtype=advanced",
      builder.build(new TitleParameters(SEARCH_FIELD, SEARCH_VALUE, null, SEARCH_FIELD, null), 1, 10).getRMAPIQueries().get(0));
  }

  @Test
  public void cqlParserAppendsResourceTypeIfFilterTypeAndFilterValueAreValidTest() throws QueryValidationException, UnsupportedEncodingException {
    RMAPIQueries queries = new TitlesQueryBuilder().build(new TitleParameters(new CQLParameters(VALID_FILTER_QUERY)), 10, 10);
    assertEquals("searchfield=titlename&resourcetype=streamingvideo&search=bridget&orderby=titlename&count=10&offset=2&searchtype=advanced",
        queries.getRMAPIQueries().get(0));
  }

  @Test(expected = QueryValidationException.class)
  public void titleParametersThrowsExceptionIfMultipleSearchFieldsAreProvidedTest() throws QueryValidationException {
    new TitleParameters(new CQLParameters("title=something AND publisher=EBSCO"));
  }

  @Test
  public void titleParametersSetsExpectedValuesInCaseOfValidSearchTest() throws QueryValidationException {
    TitleParameters parameters = new TitleParameters(new CQLParameters("resourceType=reports AND title=bridget"));
    assertEquals(SEARCH_FIELD, parameters.getSearchField());
    assertEquals(SEARCH_VALUE, parameters.getSearchValue());
    assertEquals("report", parameters.getFilterValue());
  }

  @Test
  public void titleParametersSetsExpectedValuesInCaseOfValidISXNsearchTest() throws QueryValidationException {
    TitleParameters parameters = new TitleParameters(new CQLParameters("identifier=12345"));
    assertEquals("isxn", parameters.getSearchField());
    assertEquals(IDENTIFIER_VALUE, parameters.getSearchValue());
  }


  @Test
  public void cqlParserComputesQueriesAndIndexForRMAPICorrectlyTest() throws UnsupportedEncodingException, QueryValidationException {
    RMAPIQueries rmApiQueries = new TitlesQueryBuilder().build(new TitleParameters(new CQLParameters(VALID_QUERY)) , 900, 100);
    final ArrayList<String> queries = (ArrayList<String>) rmApiQueries.getRMAPIQueries();
    assertEquals(1, queries.size());
    assertEquals(0, rmApiQueries.getFirstObjectIndex());
    for (final String query: queries) {
      assertEquals("searchfield=titlename&search=bridget&orderby=titlename&count=100&offset=10&searchtype=advanced", query);
    }
  }

  @Test
  public void cqlParserComputesMultipleQueriesAndIndexForRMAPICorrectlyTest() throws UnsupportedEncodingException, QueryValidationException {
    RMAPIQueries rmapiQueries = new TitlesQueryBuilder().build(new TitleParameters(new CQLParameters(VALID_QUERY)) , 31, 15);
    final ArrayList<String> queries = (ArrayList<String>) rmapiQueries.getRMAPIQueries();
    assertEquals(2, queries.size());
    assertEquals(1, rmapiQueries.getFirstObjectIndex());
    assertEquals("searchfield=titlename&search=bridget&orderby=titlename&count=15&offset=3&searchtype=advanced", queries.get(0));
    assertEquals("searchfield=titlename&search=bridget&orderby=titlename&count=15&offset=4&searchtype=advanced", queries.get(1));
  }

  @Test(expected = QueryValidationException.class)
  public void cqlParserThrowsExceptionIfsourceIsNotSupportedTest() throws QueryValidationException, UnsupportedEncodingException {
    new TitlesQueryBuilder().build(new TitleParameters(new CQLParameters("title=bridget and source=local")), 0, 1);
  }

  @Test
  public void titleParametersSetsExpectedValuesIfSourceIsAllTest() throws QueryValidationException {
    TitleParameters parameters = new TitleParameters(new CQLParameters("title=bridget and source=all"));
    assertEquals(SEARCH_FIELD, parameters.getSearchField());
    assertEquals(SEARCH_VALUE, parameters.getSearchValue());
  }

  @Test
  public void titleParametersSetsExpectedValuesIfSourceIsKbTest() throws QueryValidationException {
    TitleParameters parameters = new TitleParameters(new CQLParameters("title=bridget and source=kb"));
    assertEquals(SEARCH_FIELD, parameters.getSearchField());
    assertEquals(SEARCH_VALUE, parameters.getSearchValue());
  }

  @Test(expected = QueryValidationException.class)
  public void cqlParserThrowsExceptionIfselectionIsNotSupportedTest() throws QueryValidationException, UnsupportedEncodingException {
    new TitlesQueryBuilder().build(new TitleParameters(new CQLParameters("title=bridget and ext.selected=yes")), 0, 1);
  }

  @Test
  public void cqlParserReturnsExpectedQueriesIfSelectionIsSetToTrueTest() throws QueryValidationException, UnsupportedEncodingException {
    RMAPIQueries rmapiQueries = new TitlesQueryBuilder().build(new TitleParameters(new CQLParameters("title=bridget and resourceType = video and ext.selected=true")) , 900, 100);
    final ArrayList<String> queries = (ArrayList<String>) rmapiQueries.getRMAPIQueries();
    assertEquals(1, queries.size());
    assertEquals(0, rmapiQueries.getFirstObjectIndex());
    for (final String query: queries) {
      assertEquals("searchfield=titlename&resourcetype=streamingvideo&selection=selected&search=bridget&orderby=titlename&count=100&offset=10&searchtype=advanced", query);
    }
  }

  @Test
  public void cqlParserReturnsExpectedQueriesIfSelectionIsSetToAllTest() throws QueryValidationException, UnsupportedEncodingException {
    RMAPIQueries rmapiQueries = new TitlesQueryBuilder().build(new TitleParameters(new CQLParameters("title=bridget and resourceType = databases and ext.selected=all")) , 900, 100);
    final ArrayList<String> queries = (ArrayList<String>) rmapiQueries.getRMAPIQueries();
    assertEquals(1, queries.size());
    assertEquals(0, rmapiQueries.getFirstObjectIndex());
    for (final String query: queries) {
      assertEquals("searchfield=titlename&resourcetype=database&selection=all&search=bridget&orderby=titlename&count=100&offset=10&searchtype=advanced", query);
    }
  }

  @Test
  public void cqlParserReturnsExpectedQueriesIfSelectionIsSetToFalseTest() throws QueryValidationException, UnsupportedEncodingException {
    RMAPIQueries rmapiQueries = new TitlesQueryBuilder().build(new TitleParameters(new CQLParameters("title=bridget and resourceType = video and ext.selected=false")) , 900, 100);
    final ArrayList<String> queries = (ArrayList<String>) rmapiQueries.getRMAPIQueries();
    assertEquals(1, queries.size());
    assertEquals(0, rmapiQueries.getFirstObjectIndex());
    for (final String query: queries) {
      assertEquals("searchfield=titlename&resourcetype=streamingvideo&selection=notselected&search=bridget&orderby=titlename&count=100&offset=10&searchtype=advanced", query);
    }
  }

  @Test(expected = QueryValidationException.class)
  public void cqlParserThrowsExceptionIfSelectionIsInvalidTest() throws QueryValidationException, UnsupportedEncodingException {
    new TitlesQueryBuilder().build(new TitleParameters(new CQLParameters("title=bridget and resourceType = databases and ext.selected=yes")) , 900, 100);
  }

  @Test(expected = QueryValidationException.class)
  public void cqlParserThrowsExceptionIfAllRecordsAreRequestedTest() throws QueryValidationException, UnsupportedEncodingException {
    new TitlesQueryBuilder().build(new TitleParameters(new CQLParameters("cql.allRecords=all")) , 900, 100);
  }

  @Test
  public void cqlParserReturnsExpectedQueriesIfCodexTitleIsPassedTest() throws QueryValidationException, UnsupportedEncodingException {
    RMAPIQueries rmapiQueries = new TitlesQueryBuilder().build(new TitleParameters(new CQLParameters("codex.title=bridget and resourceType = audiobooks and ext.selected=false")), 900, 100);
    final ArrayList<String> queries = (ArrayList<String>) rmapiQueries.getRMAPIQueries();
    assertEquals(1, queries.size());
    assertEquals(0, rmapiQueries.getFirstObjectIndex());
    for (final String query: queries) {
      assertEquals("searchfield=titlename&resourcetype=audiobook&selection=notselected&search=bridget&orderby=titlename&count=100&offset=10&searchtype=advanced", query);
    }
  }

  @Test
  public void cqlParserReturnsExpectedQueriesIfCodexIdentifierIsPassedTest() throws QueryValidationException, UnsupportedEncodingException {
    RMAPIQueries rmapiQueries = new TitlesQueryBuilder().build(new TitleParameters(new CQLParameters("codex.identifier=12345 and resourceType = databases and ext.selected=false")) , 900, 100);
    final ArrayList<String> queries = (ArrayList<String>) rmapiQueries.getRMAPIQueries();
    assertEquals(1, queries.size());
    assertEquals(0, rmapiQueries.getFirstObjectIndex());
    for (final String query: queries) {
      assertEquals("searchfield=isxn&resourcetype=database&selection=notselected&search=12345&orderby=titlename&count=100&offset=10&searchtype=advanced", query);
    }
  }

  @Test
  public void cqlParserReturnsExpectedQueriesIfCodexPublisherIsPassedTest() throws QueryValidationException, UnsupportedEncodingException {
    RMAPIQueries rmapiQueries = new TitlesQueryBuilder().build(new TitleParameters(new CQLParameters("codex.publisher=ebsco and resourceType = databases and ext.selected=false")) , 900, 100);
    final ArrayList<String> queries = (ArrayList<String>) rmapiQueries.getRMAPIQueries();
    assertEquals(1, queries.size());
    assertEquals(0, rmapiQueries.getFirstObjectIndex());
    for (final String query: queries) {
      assertEquals("searchfield=publisher&resourcetype=database&selection=notselected&search=ebsco&orderby=titlename&count=100&offset=10&searchtype=advanced", query);
    }
  }

  @Test
  public void cqlParserReturnsExpectedQueriesIfPublisherIsPassedTest() throws QueryValidationException, UnsupportedEncodingException {
    RMAPIQueries rmapiQueries = new TitlesQueryBuilder().build(new TitleParameters(new CQLParameters("publisher=ebsco and resourceType = video and ext.selected=false")) , 900, 100);
    final ArrayList<String> queries = (ArrayList<String>) rmapiQueries.getRMAPIQueries();
    assertEquals(1, queries.size());
    assertEquals(0, rmapiQueries.getFirstObjectIndex());
    for (final String query: queries) {
      assertEquals("searchfield=publisher&resourcetype=streamingvideo&selection=notselected&search=ebsco&orderby=titlename&count=100&offset=10&searchtype=advanced", query);
    }
  }

  @Test
  public void cqlParserReturnsExpectedQueriesIfCodexSubjectIsPassedTest() throws QueryValidationException, UnsupportedEncodingException {
    RMAPIQueries rmapiQueries = new TitlesQueryBuilder().build(new TitleParameters(new CQLParameters("codex.subject=history and resourceType = databases and ext.selected=false")) , 900, 100);
    final ArrayList<String> queries = (ArrayList<String>) rmapiQueries.getRMAPIQueries();
    assertEquals(1, queries.size());
    assertEquals(0, rmapiQueries.getFirstObjectIndex());
    for (final String query: queries) {
      assertEquals("searchfield=subject&resourcetype=database&selection=notselected&search=history&orderby=titlename&count=100&offset=10&searchtype=advanced", query);
    }
  }

  @Test
  public void cqlParserReturnsExpectedQueriesIfSubjectIsPassedTest() throws QueryValidationException, UnsupportedEncodingException {
    RMAPIQueries rmapiQueries = new TitlesQueryBuilder().build(new TitleParameters(new CQLParameters("subject=history and resourceType = video and ext.selected=false")) , 900, 100);
    final ArrayList<String> queries = (ArrayList<String>) rmapiQueries.getRMAPIQueries();
    assertEquals(1, queries.size());
    assertEquals(0, rmapiQueries.getFirstObjectIndex());
    for (final String query: queries) {
      assertEquals("searchfield=subject&resourcetype=streamingvideo&selection=notselected&search=history&orderby=titlename&count=100&offset=10&searchtype=advanced", query);
    }
  }
  @Test(expected = QueryValidationException.class)
  public void cqlParserThrowsExceptionIfInvalidSearchFieldWithPrefixIsPassedTest() throws QueryValidationException, UnsupportedEncodingException {
    new TitlesQueryBuilder().build(new TitleParameters(new CQLParameters("codex.resourceType=12345")) , 900, 100);
  }

  @Test
  public void cqlParserReturnsCorrectIdValueIfPassed() throws QueryValidationException {
    CQLParameters parameters = new CQLParameters("id=12345");
    assertTrue(parameters.isIdSearch());
    assertEquals(IDENTIFIER_VALUE, parameters.getIdSearchValue());
  }

  @Test
  public void cqlParserUnknownExt() throws UnsupportedEncodingException, QueryValidationException {
    RMAPIQueries rmapiQueries = new TitlesQueryBuilder().build(new TitleParameters(new CQLParameters("title=xyzzy and ext.available=all")), 0, 10);
    final List<String> queries = rmapiQueries.getRMAPIQueries();
    assertEquals(1, queries.size());
    String query = queries.get(0);
    assertNotNull(query);
    assertThat(query, stringContainsInOrder(Arrays.asList("searchfield=title", "search=xyzzy")));
  }

  @Test
  public void cqlParserSelectedNotPresent() throws UnsupportedEncodingException, QueryValidationException {
    RMAPIQueries rmapiQueries = new TitlesQueryBuilder().build(new TitleParameters(new CQLParameters("title=xyzzy")), 0, 10);
    final List<String> queries = rmapiQueries.getRMAPIQueries();
    assertEquals(1, queries.size());
    String query = queries.get(0);
    assertNotNull(query);
    assertThat(query, not(containsString("selection=")));
  }
}
