package org.folio.cql2rmapi;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.easymock.EasyMock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.z3950.zing.cql.CQLNode;

/**
 * Unit tests for CQLParserForRMAPI class.
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(CQLParserForRMAPI.class)
public class CQLParserForRMAPITest {

  private static final String VALID_QUERY = "title=bridget";
  private static final String VALID_ISBN_QUERY = "identifier.type = isbn and identifier.value = 12345 sortby title";
  private static final String VALID_ISSN_QUERY = "identifier.type = issn and identifier.value = 12345 sortby title";
  private static final String VALID_FILTER_QUERY = "title = bridget and type = journal sortby title";

  @Test(expected = QueryValidationException.class)
  public void initCQLParserThrowsExceptionIfQueryInvalidTest() throws QueryValidationException {
		final String invalidQuery = "";
		new CQLParserForRMAPI(invalidQuery, 1, 10);
	}

	@Test
	public void initCQLParserReturnsInstanceOfCQLNodeIfQueryValidTest() throws QueryValidationException {
		final CQLParserForRMAPI parser = new CQLParserForRMAPI(VALID_QUERY, 1, 10);
		final Object returnValue = parser.initCQLParser(VALID_QUERY);
		assertThat(returnValue, instanceOf(CQLNode.class));
	}

	@Test(expected = QueryValidationException.class)
	public void CQLParserThrowsExceptionInCaseOfUnsupportedOperatorTest() throws QueryValidationException {
		final String invalidOperator = "title<>bridget";
		new CQLParserForRMAPI(invalidOperator, 1, 10);
	}

	@Test
	public void CQLParserSetsExpectedValuesIfSearchFieldNotProvidedTest() throws QueryValidationException {
		final String validQuery = "bridget";
		final CQLParserForRMAPI parser = new CQLParserForRMAPI(validQuery, 1, 10);
		assertEquals("titlename", parser.searchField);
		assertEquals("bridget", parser.searchValue);
	}

	@Test
	public void CQLParserSetsExpectedValuesIfValidSearchFieldIsProvidedTest() throws QueryValidationException {
		final CQLParserForRMAPI parser = new CQLParserForRMAPI(VALID_ISBN_QUERY, 1, 10);
		assertEquals("isbn", parser.searchField);
		assertEquals("12345", parser.searchValue);
		assertEquals("titlename", parser.sortOrder);
	}

	@Test(expected = QueryValidationException.class)
	public void CQLParserThrowsExceptionInCaseOfUnsupportedIdentifierFieldTest() throws QueryValidationException {
		final String invalidIdentifierTypeQuery = "identifier.type = xyz and identifier.value = 12345 sortby title";
		new CQLParserForRMAPI(invalidIdentifierTypeQuery, 1, 10);
	}

	@Test(expected = QueryValidationException.class)
	public void CQLParserThrowsExceptionInCaseOfUnsupportedSearchFieldTest() throws QueryValidationException {
		final String invalidOperator = "author=bridget";
		new CQLParserForRMAPI(invalidOperator, 1, 10);
	}

	@Test
	public void CQLParserSetsExpectedValuesIfTypeFilterIsPassedTest() throws QueryValidationException {
		final CQLParserForRMAPI parser = new CQLParserForRMAPI(VALID_FILTER_QUERY, 1, 10);
		assertEquals("title", parser.searchField);
		assertEquals("bridget", parser.searchValue);
		assertEquals("titlename", parser.sortOrder);
		assertEquals("type", parser.filterType);
		assertEquals("journal", parser.filterValue);
	}

	@Test(expected = QueryValidationException.class)
	public void CQLParserThrowsExceptionIfInvalidFilterValueIsPassedTest() throws QueryValidationException {
		final String invalidFilterQuery = "title = bridget and type = hardcopy sortby title";
		new CQLParserForRMAPI(invalidFilterQuery, 1, 10);
	}

	@Test(expected = QueryValidationException.class)
	public void CQLParserThrowsExceptionIfNullFilterValueIsPassedTest() throws QueryValidationException {
		final String invalidFilterQuery = "title = bridget and type = null sortby title";
		final CQLParserForRMAPI parser = new CQLParserForRMAPI(invalidFilterQuery, 1, 10);
		parser.filterValue = null;
	}

	@Test(expected = QueryValidationException.class)
	public void CQLParserThrowsExceptionIfMultipleSortKeysAreProvidedTest() throws QueryValidationException {
		final String multipleSortQuery = "title = bridget and type = journal sortby title, publisher";
		new CQLParserForRMAPI(multipleSortQuery, 1, 10);
	}

	@Test(expected = QueryValidationException.class)
	public void CQLParserThrowsExceptionIfSortOnUnsupportedFieldTest() throws QueryValidationException {
		final String multipleSortQuery = "title = bridget and type = journal sortby publisher";
		new CQLParserForRMAPI(multipleSortQuery, 1, 10);
	}

	@Test(expected = QueryValidationException.class)
	public void CQLParserThrowsExceptionIfUnsupportedBooleanOperatorIsPassedTest() throws QueryValidationException {
		final String multipleSortQuery = "title = bridget OR publisher = Ebsco sortby title";
		new CQLParserForRMAPI(multipleSortQuery, 1, 10);
	}

	@Test
	public void CQLParserReturnsExpectedQueryStringIfValidQueryIsPassedTest() throws QueryValidationException, UnsupportedEncodingException {
		final CQLParserForRMAPI parser = new CQLParserForRMAPI(VALID_QUERY, 0, 0);
		assertEquals("search=bridget&searchfield=titlename&orderby=titlename&count=0&offset=1", parser.getRMAPIQuery());
	}

	@Test(expected = QueryValidationException.class)
	public void CQLParserThrowsExceptionIfsearchValueAndSearchFieldAreNullTest() throws QueryValidationException {
		final CQLParserForRMAPI parser = new CQLParserForRMAPI(VALID_QUERY, 0, 0);
		parser.searchField = null;
		parser.searchValue = null;
		parser.getRMAPIQuery();
	}

	@Test(expected = QueryValidationException.class)
	public void CQLParserThrowsExceptionIfsearchValueIsNullTest() throws QueryValidationException {
		final CQLParserForRMAPI parser = new CQLParserForRMAPI(VALID_QUERY, 0, 0);
		parser.searchValue = null;
		parser.getRMAPIQuery();
	}

	@Test(expected = QueryValidationException.class)
	public void CQLParserThrowsExceptionIfsearchFieldIsNullTest() throws QueryValidationException {
		final CQLParserForRMAPI parser = new CQLParserForRMAPI(VALID_QUERY, 0, 0);
		parser.searchField = null;
		parser.getRMAPIQuery();
	}

	@Test
	public void CQLParserMapsISBNToISXNTest() throws QueryValidationException {
		final CQLParserForRMAPI parser = new CQLParserForRMAPI(VALID_ISBN_QUERY, 0, 0);
		assertEquals("search=12345&searchfield=isxn&orderby=titlename&count=0&offset=1", parser.getRMAPIQuery());
	}

	@Test
	public void CQLParserMapsISSNToISXNTest() throws QueryValidationException {
		final CQLParserForRMAPI parser = new CQLParserForRMAPI(VALID_ISSN_QUERY, 0, 0);
		assertEquals("search=12345&searchfield=isxn&orderby=titlename&count=0&offset=1", parser.getRMAPIQuery());
	}

	@Test
	public void CQLParserPassesAlongOffsetIfNotEqualToZeroTest() throws QueryValidationException {
		final CQLParserForRMAPI parser = new CQLParserForRMAPI(VALID_ISSN_QUERY, 10, 0);
		assertEquals("search=12345&searchfield=isxn&orderby=titlename&count=0&offset=10", parser.getRMAPIQuery());
	}

	@Test
	public void CQLParserDoesNotAppendResourceTypeIfTypeNotPassedAlongTest() throws QueryValidationException {
		final CQLParserForRMAPI parser = new CQLParserForRMAPI(VALID_ISSN_QUERY, 10, 0);
		assertEquals("search=12345&searchfield=isxn&orderby=titlename&count=0&offset=10", parser.getRMAPIQuery());
	}

	@Test
	public void CQLParserDoesNotAppendResourceTypeIfFilterTypeIsNullTest() throws QueryValidationException {
		final CQLParserForRMAPI parser = new CQLParserForRMAPI(VALID_FILTER_QUERY, 10, 0);
		parser.filterType = null;
		assertEquals("search=bridget&searchfield=titlename&orderby=titlename&count=0&offset=10", parser.getRMAPIQuery());
	}

	@Test
	public void CQLParserDoesNotAppendResourceTypeIfFilTerValueIsNullTest() throws QueryValidationException {
		final CQLParserForRMAPI parser = new CQLParserForRMAPI(VALID_FILTER_QUERY, 10, 0);
		parser.filterValue = null;
		assertEquals("search=bridget&searchfield=titlename&orderby=titlename&count=0&offset=10", parser.getRMAPIQuery());
	}

	@Test
	public void CQLParserAppendsResourceTypeIfFilterTypeAndFilTerValueAreValidTest() throws QueryValidationException {
		final CQLParserForRMAPI parser = new CQLParserForRMAPI(VALID_FILTER_QUERY, 10, 0);
		assertEquals("search=bridget&searchfield=titlename&resourcetype=journal&orderby=titlename&count=0&offset=10", parser.getRMAPIQuery());
	}

	@Test(expected = QueryValidationException.class)
	public void CQLParserThrowsExceptionWhenURLEncodingFails() throws QueryValidationException, UnsupportedEncodingException {
		final CQLParserForRMAPI parser = new CQLParserForRMAPI(VALID_FILTER_QUERY, 10, 0);
		mockStatic(URLEncoder.class);
		EasyMock.expect(URLEncoder.encode("bridget", "UTF-8")).andThrow(new UnsupportedEncodingException());
		replayAll();
		parser.getRMAPIQuery();
	}
}
