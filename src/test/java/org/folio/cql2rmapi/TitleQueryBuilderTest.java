package org.folio.cql2rmapi;

import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import java.util.Collections;

import javax.validation.ValidationException;

import org.folio.holdingsiq.model.Sort;
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
  private static final String IDENTIFIER_VALUE = "12345";

  @Test(expected = QueryValidationException.class)
  public void cqlParametersThrowsExceptionIfQueryInvalidTest() throws QueryValidationException {
    final String invalidQuery = "";
    new CQLParameters(invalidQuery);
  }

  @Test(expected = ValidationException.class)
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
    assertEquals(SEARCH_VALUE, parameters.getFilterQuery().getName());
  }

  @Test
  public void titleParametersSetsExpectedValuesIfValidSearchFieldIsProvidedTest() throws QueryValidationException {
    TitleParameters parameters = new TitleParameters(new CQLParameters(VALID_ISBN_QUERY));
    assertEquals(IDENTIFIER_VALUE, parameters.getFilterQuery().getIsxn());
    assertEquals(Sort.NAME, parameters.getSortType());
  }

  @Test(expected = ValidationException.class)
  public void titleParametersThrowsExceptionInCaseOfUnsupportedSearchFieldTest() throws QueryValidationException {
    final String invalidOperator = "author=bridget";
    new TitleParameters(new CQLParameters(invalidOperator));
  }

  @Test
  public void titleParametersSetsExpectedValuesIfTypeFilterIsPassedTest() throws QueryValidationException {
    TitleParameters parameters = new TitleParameters(new CQLParameters(VALID_FILTER_QUERY));
    assertEquals(SEARCH_VALUE, parameters.getFilterQuery().getName());
    assertEquals("streamingvideo", parameters.getFilterQuery().getType());
  }

  @Test
  public void titleParametersSetsExpectedValuesIfBooleanQueryIsPassedTest() throws QueryValidationException {
    TitleParameters parameters = new TitleParameters(new CQLParameters(VALID_ADVANCED_BOOLEAN_QUERY));
    assertEquals("spring OR summer", parameters.getFilterQuery().getName());
  }

  @Ignore("Test Pending - CQL parser handling of quoted query")
  @Test
  public void titleParametersSetsExpectedValuesIfPhraseQuery2IsPassedTest() throws QueryValidationException {
    TitleParameters parameters = new TitleParameters(new CQLParameters(VALID_ADVANCED_PHRASE_QUERY));
    assertEquals("\"great gatsby\"", parameters.getFilterQuery().getName());
   }

  @Test
  public void titleParametersSetsExpectedValuesIfWildcardQueryIsPassedTest() throws QueryValidationException {
    TitleParameters parameters = new TitleParameters(new CQLParameters(VALID_ADVANCED_WILDCARD_QUERY));
    assertEquals("comput*", parameters.getFilterQuery().getName());
  }

  @Test
  public void titleParametersSetsExpectedValuesIfNestedQueryIsPassedTest() throws QueryValidationException {
    TitleParameters parameters = new TitleParameters(new CQLParameters(VALID_ADVANCED_NESTED_QUERY));
    assertEquals("(company AND business) NOT report*", parameters.getFilterQuery().getName());
  }

  @Test(expected = ValidationException.class)
  public void titleParametersThrowsExceptionIfInvalidFilterValueIsPassedTest() throws QueryValidationException {
    new TitleParameters(new CQLParameters("title = bridget and resourceType = hardcopy sortby title"));
  }

  @Test(expected = ValidationException.class)
  public void titleParametersThrowsExceptionIfNullFilterValueIsPassedTest() throws QueryValidationException {
    new TitleParameters(new CQLParameters("title = bridget and resourceType = null sortby title"));
  }

  @Test(expected = QueryValidationException.class)
  public void titleParametersThrowsExceptionIfMultipleFiltersSepartedByAndAreProvidedTest() throws QueryValidationException {
    new TitleParameters(new CQLParameters("title = bridget and resourceType = databases and resourceType = audiobooks sortby title"));
  }

  @Test(expected = ValidationException.class)
  public void titleParametersThrowsExceptionIfMultipleFiltersSepartedByCommaAreProvidedTest() throws QueryValidationException {
    new TitleParameters(new CQLParameters("title = bridget and resourceType = databases, audiobooks sortby title"));
  }

  @Test(expected = ValidationException.class)
  public void titleParametersThrowsExceptionIfMultipleFiltersSepartedBySpaceAreProvidedTest() throws QueryValidationException {
    new TitleParameters(new CQLParameters("title = bridget and resourceType = databases audiobooks sortby title"));
  }

  @Test(expected = QueryValidationException.class)
  public void titleParametersThrowsExceptionIfMultipleSortKeysAreProvidedTest() throws QueryValidationException {
    new TitleParameters(new CQLParameters("title = bridget and resourceType = journal sortby title, publisher"));
  }

  @Test(expected = ValidationException.class)
  public void titleParametersThrowsExceptionIfSortOnUnsupportedFieldTest() throws QueryValidationException {
    new TitleParameters(new CQLParameters("title = bridget and resourceType = journal sortby publisher"));
  }

  @Test(expected = QueryValidationException.class)
  public void titleParametersThrowsExceptionIfUnsupportedBooleanOperatorIsPassedTest() throws QueryValidationException {
    new TitleParameters(new CQLParameters("title = bridget OR publisher = Ebsco sortby title"));
  }


  @Test(expected = ValidationException.class)
  public void titleParametersThrowsExceptionIfSearchValueAndSearchFieldAreNullTest() {
    new TitleParameters(new CQLParameters(Collections.emptyMap(), "title", false, null));
  }

  @Test(expected = ValidationException.class)
  public void titleParametersThrowsExceptionIfMultipleSearchFieldsAreProvidedTest() throws QueryValidationException {
    new TitleParameters(new CQLParameters("title=something AND publisher=EBSCO"));
  }

  @Test
  public void titleParametersSetsExpectedValuesInCaseOfValidSearchTest() throws QueryValidationException {
    TitleParameters parameters = new TitleParameters(new CQLParameters("resourceType=reports AND title=bridget"));
    assertEquals(SEARCH_VALUE, parameters.getFilterQuery().getName());
    assertEquals("report", parameters.getFilterQuery().getType());
  }

  @Test
  public void titleParametersSetsExpectedValuesInCaseOfValidISXNsearchTest() throws QueryValidationException {
    TitleParameters parameters = new TitleParameters(new CQLParameters("identifier=12345"));
    assertEquals(IDENTIFIER_VALUE, parameters.getFilterQuery().getIsxn());
  }

  @Test(expected = ValidationException.class)
  public void cqlParserThrowsExceptionIfsourceIsNotSupportedTest() throws QueryValidationException {
    new TitleParameters(new CQLParameters("title=bridget and source=local"));
  }

  @Test
  public void titleParametersSetsExpectedValuesIfSourceIsAllTest() throws QueryValidationException {
    TitleParameters parameters = new TitleParameters(new CQLParameters("title=bridget and source=all"));
    assertEquals(SEARCH_VALUE, parameters.getFilterQuery().getName());
  }

  @Test
  public void titleParametersSetsExpectedValuesIfSourceIsKbTest() throws QueryValidationException {
    TitleParameters parameters = new TitleParameters(new CQLParameters("title=bridget and source=kb"));
    assertEquals(SEARCH_VALUE, parameters.getFilterQuery().getName());
  }

  @Test(expected = ValidationException.class)
  public void cqlParserThrowsExceptionIfselectionIsNotSupportedTest() throws QueryValidationException {
    new TitleParameters(new CQLParameters("title=bridget and ext.selected=yes"));
  }

  @Test(expected = ValidationException.class)
  public void cqlParserThrowsExceptionIfSelectionIsInvalidTest() throws QueryValidationException {
    new TitleParameters(new CQLParameters("title=bridget and resourceType = databases and ext.selected=yes"));
  }

  @Test(expected = ValidationException.class)
  public void cqlParserThrowsExceptionIfAllRecordsAreRequestedTest() throws QueryValidationException{
    new TitleParameters(new CQLParameters("cql.allRecords=all"));
  }

  @Test(expected = ValidationException.class)
  public void cqlParserThrowsExceptionIfInvalidSearchFieldWithPrefixIsPassedTest() throws QueryValidationException{
    new TitleParameters(new CQLParameters("codex.resourceType=12345"));
  }

  @Test
  public void cqlParserReturnsCorrectIdValueIfPassed() throws QueryValidationException {
    CQLParameters parameters = new CQLParameters("id=12345");
    assertTrue(parameters.isIdSearch());
    assertEquals(IDENTIFIER_VALUE, parameters.getIdSearchValue());
  }

  @Test
  public void cqlParserUnknownExt() throws QueryValidationException {
    TitleParameters titleParameters = new TitleParameters(new CQLParameters("title=xyzzy and ext.available=all"));
    assertNotNull(titleParameters.getFilterQuery().getName());
  }

  @Test
  public void cqlParserSelectedNotPresent() throws QueryValidationException {
    TitleParameters titleParameters = new TitleParameters(new CQLParameters("title=xyzzy"));
    assertNull(titleParameters.getFilterQuery().getSelected());
  }
}
