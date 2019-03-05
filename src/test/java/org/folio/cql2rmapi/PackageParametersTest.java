package org.folio.cql2rmapi;

import static org.junit.Assert.assertEquals;

import javax.validation.ValidationException;

import org.folio.holdingsiq.model.Sort;
import org.junit.Test;

public class PackageParametersTest {
  private static final String SEARCH_VALUE = "bridget";
  private static final String VALID_NAME_QUERY = "name = bridget sortby name";
  private static final String VALID_FILTER_QUERY = "name = bridget and type = aggregatedfulltext sortby name";
  private static final String VALID_SELECTED_QUERY = "(name = \"bridget\") and (ext.selected = true) sortby name";

  @Test(expected = ValidationException.class)
  public void packageParametersThrowsExceptionWhenSearchNameIsNotSet() throws QueryValidationException {
    CQLParameters parameters = new CQLParameters("ext.selected = true sortby name");
    new PackageParameters(parameters);
  }

  @Test
  public void packageParametersSetsNameSearchWhenSearchFieldNotProvided() throws QueryValidationException {
    String searchValue = SEARCH_VALUE;
    PackageParameters parameters = new PackageParameters(new CQLParameters(searchValue));
    assertEquals(searchValue, parameters.getSearchValue());
  }

  @Test
  public void packageParametersSetsNameSearch() throws QueryValidationException {
    PackageParameters parameters = new PackageParameters(new CQLParameters(VALID_NAME_QUERY));
    assertEquals(SEARCH_VALUE, parameters.getSearchValue());
  }

  @Test(expected = ValidationException.class)
  public void packageParametersThrowsExceptionInCaseOfUnsupportedSearchField() throws QueryValidationException {
    new PackageParameters(new CQLParameters("provider=abc"));
  }

  @Test
  public void packageParametersSetsExpectedValuesIfTypeFilterIsPassedTest() throws QueryValidationException {
    PackageParameters parameters = new PackageParameters(new CQLParameters(VALID_FILTER_QUERY));
    assertEquals(SEARCH_VALUE, parameters.getSearchValue());
    assertEquals(Sort.NAME, parameters.getSortType());
    assertEquals("aggregatedfulltext", parameters.getFilterType());
  }

  @Test
  public void packageParametersSetsExpectedValuesIfSelectedIsTrue() throws QueryValidationException {
    PackageParameters parameters = new PackageParameters(new CQLParameters(VALID_SELECTED_QUERY));
    assertEquals(SEARCH_VALUE, parameters.getSearchValue());
    assertEquals(Sort.NAME, parameters.getSortType());
    assertEquals("selected", parameters.getSelection());
  }

  @Test(expected = ValidationException.class)
  public void packageParametersThrowsExceptionIfInvalidFilterValueIsPassedTest() throws QueryValidationException {
    new PackageParameters(new CQLParameters("name = bridget and type = Unknown123 sortby name"));
  }

  @Test(expected = ValidationException.class)
  public void packageParametersThrowsExceptionIfMultipleFiltersSepartedByAndAreProvidedTest() throws QueryValidationException {
    new PackageParameters(new CQLParameters("name = bridget and type = Unknown and codex.type = E-Journal sortby name"));
  }

  @Test(expected = ValidationException.class)
  public void packageParametersThrowsExceptionIfMultipleFiltersSepartedByCommaAreProvidedTest() throws QueryValidationException {
    new PackageParameters(new CQLParameters("name = bridget and type = Unknown, E-Journal sortby name"));
  }

  @Test(expected = QueryValidationException.class)
  public void packageParametersThrowsExceptionIfMultipleSortKeysAreProvidedTest() throws QueryValidationException {
    new PackageParameters(new CQLParameters("name = bridget and type = Unknown sortby name, identifier"));
  }

  @Test(expected = ValidationException.class)
  public void packageParametersThrowsExceptionIfSortOnUnsupportedFieldTest() throws QueryValidationException {
    new PackageParameters(new CQLParameters("name = bridget and type = Unknown sortby identifier"));
  }

  @Test(expected = QueryValidationException.class)
  public void packageParametersThrowsExceptionIfUnsupportedBooleanOperatorIsPassedTest() throws QueryValidationException {
    new PackageParameters(new CQLParameters("name = bridget OR type = Unknown sortby name"));
  }

  @Test(expected = ValidationException.class)
  public void packageParametersThrowsExceptionIfSourceIsInvalid() throws QueryValidationException {
    new PackageParameters(new CQLParameters("name = bridget and source = local sortby name"));
  }

  @Test(expected = ValidationException.class)
  public void packageParametersThrowsExceptionIfSelectedValueIsInvalid() throws QueryValidationException {
    new PackageParameters(new CQLParameters("name = bridget and ext.selected = yes sortby name"));
  }
}
