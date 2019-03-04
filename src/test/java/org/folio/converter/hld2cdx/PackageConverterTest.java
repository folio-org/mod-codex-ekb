package org.folio.converter.hld2cdx;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.when;

import org.folio.holdingsiq.model.CoverageDates;
import org.folio.holdingsiq.model.PackageData;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.convert.converter.Converter;

import org.folio.rest.jaxrs.model.Coverage;
import org.folio.rest.jaxrs.model.Package;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class PackageConverterTest {

  private static final Integer PACKAGE_ID = 111;
  private static final Integer VENDOR_ID = 222;
  private static final String PACKAGE_NAME = "Package name";
  private static final String PACKAGE_TYPE_EBOOK = "EBook";
  private static final Integer TITLE_COUNT = 100;
  private static final String VENDOR_NAME = "Vendor";
  private static final CoverageDates COVERAGE_DATES = CoverageDates.builder().build();
  private static final Coverage COVERAGE = new Coverage();

  private static final String PACKAGE_UUID = VENDOR_ID + "-" + PACKAGE_ID;
  private static final String SOURCE = "kb";

  @Mock
  private Converter<CoverageDates, Coverage> coverageConverter;
  @InjectMocks
  private PackageConverter converter;


  @Test
  public void shouldConvertPackageData() {
    when(coverageConverter.convert(COVERAGE_DATES)).thenReturn(COVERAGE);

    PackageData input = createBasePackageData().build();

    Package converted = converter.convert(input);

    assertNotNull(converted);
    assertEquals(PACKAGE_UUID, converted.getId());
    assertEquals(Package.IsSelected.NO, converted.getIsSelected());
    assertEquals(TITLE_COUNT, converted.getItemCount());
    assertEquals(PACKAGE_NAME, converted.getName());
    assertEquals(VENDOR_NAME, converted.getProvider());
    assertEquals(VENDOR_ID.toString(), converted.getProviderId());
    assertEquals(SOURCE, converted.getSource());
    assertEquals(Package.Type.EBOOK, converted.getType());
    assertSame(COVERAGE, converted.getCoverage());
  }

  @Test
  public void shouldIgnoreEmptyCoverage() {
    PackageData input = createBasePackageData().customCoverage(null).build();

    Package converted = converter.convert(input);

    assertNotNull(converted);
    assertNull(converted.getCoverage());
  }

  @Test
  public void shouldSetSelectedToNotSpecifiedIfNull() {
    PackageData input = createBasePackageData().isSelected(null).build();

    Package converted = converter.convert(input);

    assertNotNull(converted);
    assertEquals(Package.IsSelected.NOT_SPECIFIED, converted.getIsSelected());
  }

  @Test
  public void shouldSetSelectedToYesIfTrue() {
    PackageData input = createBasePackageData().isSelected(true).build();

    Package converted = converter.convert(input);

    assertNotNull(converted);
    assertEquals(Package.IsSelected.YES, converted.getIsSelected());
  }

  @Test(expected = NullPointerException.class)
  public void shouldThrowNPEIfInputIsNull() {
    converter.convert(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowIllegalArgExcIfVendorIdIsNull() {
    PackageData input = createBasePackageData().vendorId(null).build();

    converter.convert(input);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowIllegalArgExcIfPackageIdIsNull() {
    PackageData input = createBasePackageData().packageId(null).build();

    converter.convert(input);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowIllegalArgExcIfContentTypeInvalid() {
    PackageData input = createBasePackageData().contentType("ABCD").build();

    converter.convert(input);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowIllegalArgExcIfContentTypeNull() {
    PackageData input = createBasePackageData()
      .contentType(null).build();

    converter.convert(input);
  }

  private PackageData.PackageDataBuilder createBasePackageData() {

    return PackageData.builder()
      .packageId(PACKAGE_ID)
      .vendorId(VENDOR_ID)
      .vendorName(VENDOR_NAME)
      .isSelected(false)
      .packageName(PACKAGE_NAME)
      .contentType(PACKAGE_TYPE_EBOOK)
      .titleCount(TITLE_COUNT)
      .customCoverage(COVERAGE_DATES);
  }
}
