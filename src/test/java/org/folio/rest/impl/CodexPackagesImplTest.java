package org.folio.rest.impl;

import static org.folio.utils.Utils.readMockFile;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.folio.holdingsiq.model.Configuration;
import org.folio.holdingsiq.service.ConfigurationService;
import org.folio.holdingsiq.service.exception.ConfigurationServiceException;
import org.folio.rest.jaxrs.model.Package;
import org.folio.rest.jaxrs.model.PackageCollection;
import org.folio.rest.jaxrs.model.Source;
import org.folio.rest.jaxrs.model.SourceCollection;
import org.folio.spring.SpringContextUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(VertxUnitRunner.class)
public class CodexPackagesImplTest extends VertxTestBase {

  private static final String MOCK_RMAPI_INSTANCE_PACKAGE_200_RESPONSE_WHEN_FOUND = "RMAPIService/SuccessGetPackageById.json";
  private static final String MOCK_RMAPI_INSTANCE_PACKAGE_404_RESPONSE_WHEN_NOT_FOUND = "RMAPIConfiguration/mock_content_fail_404.json";
  private static final String MOCK_RMAPI_PACKAGES_200_RESPONSE = "RMAPIService/SuccessGetPackageList.json";

  private static final String CUSTOMER_ID = "test";
  private static final String VENDOR_ID = "111";
  private static final String PACKAGE_ID = "222222";
  private static final String INVALID_PACKAGE_ID = "999999";
  private static final String CODEX_PACKAGE_ID = VENDOR_ID + "-" + PACKAGE_ID;
  private static final String NOT_MOCKED_CODEX_PACKAGE_ID = "123-456";
  private static final String SEARCH_PACKAGES_QUERY = "name = Academy";
  private static final String NOT_MOCKED_QUERY = "name = abc";
  private static final String INVALID_SEARCH_PACKAGES_QUERY = "name = Academy or id = 22";
  private static final String GET_PACKAGES_SUCCESSFUL_RM_API_URL = "/rm/rmaccounts/test/packages?selection=all&contenttype=all&searchtype=advanced&search=Academy&offset=1&count=10&orderby=packagename";

  @Autowired
  private ConfigurationService configurationService;

  private static String packageByIdURL(String packageId) {
    return String.format("/rm/rmaccounts/%s/vendors/%s/packages/%s", CUSTOMER_ID, VENDOR_ID, packageId);
  }

  @Before
  public void setUp(TestContext context) {
    super.setUp(context);

    Async async = context.async();

    int serverPort = Integer.parseInt(System.getProperty("serverPort", Integer.toString(51234)));
    String host = "localhost";
    HttpServer server = vertx.createHttpServer();

    server.requestHandler(req -> {
      if (req.path().equals(packageByIdURL(PACKAGE_ID))) {
        req.response().setStatusCode(200).putHeader("content-type", "application/json")
          .end(readMockFile(MOCK_RMAPI_INSTANCE_PACKAGE_200_RESPONSE_WHEN_FOUND));
      } else if (req.path().equals(packageByIdURL(INVALID_PACKAGE_ID))) {
        req.response().setStatusCode(404).putHeader("content-type", "text/plain")
          .end(readMockFile(MOCK_RMAPI_INSTANCE_PACKAGE_404_RESPONSE_WHEN_NOT_FOUND));
      } else if (req.uri().equals(GET_PACKAGES_SUCCESSFUL_RM_API_URL)) {
        req.response().setStatusCode(200).putHeader("content-type", "application/json")
          .end(readMockFile(MOCK_RMAPI_PACKAGES_200_RESPONSE));
      } else {
        req.response().setStatusCode(500).end("Unexpected call: " + req.path());
      }
    });
    server.listen(serverPort, host, ar -> async.complete());

    SpringContextUtil.autowireDependenciesFromFirstContext(this, vertx);

    doReturn(CompletableFuture.completedFuture(
      Configuration.builder()
        .customerId("test")
        .apiKey("8675309")
        .url("http://localhost:" + serverPort)
        .configValid(true).build()))
      .when(configurationService).retrieveConfiguration(any());
  }

  @Test
  public void getCodexPackagesSourcesSuccessTest() {
    logger.info("Running getCodexPackagesSourcesSuccessTest");

    final SourceCollection response = RestAssured
      .given()
      .header(tenantHeader)
      .header(urlHeader)
      .header(contentTypeHeader)
      .get("/codex-packages-sources")
      .then()
      .contentType(ContentType.JSON)
      .log()
      .ifValidationFails()
      .statusCode(200).extract().as(SourceCollection.class);

    List<Source> sources = response.getSources();
    assertEquals(1, sources.size());
    assertTrue(sources.get(0).getName().matches("mod-codex-ekb-.*"));
    assertTrue(sources.get(0).getId().matches("kb"));

    logger.info("Test done");
  }

  @Test
  public void getCodexPackagesByIdSuccessTest() {
    logger.info("Testing for successful package id");

    Package pkg = RestAssured
      .given()
      .header(tenantHeader)
      .header(urlHeader)
      .header(tokenHeader)
      .header(contentTypeHeader)
      .get("/codex-packages/" + CODEX_PACKAGE_ID)
      .then()
      .contentType(ContentType.JSON)
      .log()
      .ifValidationFails()
      .statusCode(200).extract().as(Package.class);

    assertEquals(CODEX_PACKAGE_ID, pkg.getId());
    assertEquals("EBSCO", pkg.getProvider());
    assertEquals(VENDOR_ID, pkg.getProviderId());
    assertEquals(Package.IsSelected.YES, pkg.getIsSelected());

    logger.info("Test done");
  }

  @Test
  public void getCodexPackagesByIdPackageNotFoundTest() {
    logger.info("Testing for response when package not found");

    RestAssured
      .given()
      .header(tenantHeader)
      .header(urlHeader)
      .header(tokenHeader)
      .header(contentTypeHeader)
      .get("/codex-packages/" + INVALID_PACKAGE_ID)
      .then()
      .log()
      .ifValidationFails()
      .statusCode(404);

    logger.info("Test done");
  }

  @Test
  public void getCodexPackagesByIdPackageNotAuth() {
    logger.info("Testing for response when not authorized");

    CompletableFuture<Object> future = new CompletableFuture<>();
    future.completeExceptionally(new ConfigurationServiceException("UnAuthorized to access RM API Configuration", 401));
    doReturn(future).when(configurationService).retrieveConfiguration(any());

    RestAssured
      .given()
      .header(tenantHeader)
      .header(urlHeader)
      .header(tokenHeader)
      .header(contentTypeHeader)
      .get("/codex-packages/" + CODEX_PACKAGE_ID)
      .then()
      .log()
      .ifValidationFails()
      .statusCode(401);

    logger.info("Test done");
  }

  @Test
  public void getCodexPackagesSuccessTest() {
    logger.info("Running getCodexPackagesSuccessTest");
    final PackageCollection response = RestAssured
      .given()
      .header(tenantHeader)
      .header(urlHeader)
      .header(contentTypeHeader)
      .get("/codex-packages?query=" + SEARCH_PACKAGES_QUERY)
      .then()
      .contentType(ContentType.JSON)
      .log()
      .ifValidationFails()
      .statusCode(200).extract().as(PackageCollection.class);

    Package firstPackage = response.getPackages().get(0);
    assertEquals("392-3007", firstPackage.getId());
    assertEquals("American Academy of Family Physicians", firstPackage.getName());
    assertEquals("American Academy of Family Physicians", firstPackage.getProvider());
    assertEquals("kb", firstPackage.getSource());
    assertEquals("2018-08-13", firstPackage.getCoverage().getBeginCoverage());
    assertEquals("2018-09-13", firstPackage.getCoverage().getEndCoverage());
    assertEquals(Package.IsSelected.NO, firstPackage.getIsSelected());
    assertEquals(3, (int) firstPackage.getItemCount());
    assertEquals(Package.Type.EJOURNAL, firstPackage.getType());

    logger.info("Test done");
  }

  @Test
  public void getCodexPackagesHandlesInvalidQueryTest() {
    logger.info("Test getCodexPackagesHandlesInvalidQueryTest");

    RestAssured
      .given()
      .header(tenantHeader)
      .header(contentTypeHeader)
      .header(urlHeader)
      .header(tokenHeader)
      .get("/codex-packages?query=" + INVALID_SEARCH_PACKAGES_QUERY)
      .then()
      .log()
      .ifValidationFails()
      .statusCode(400);

    logger.info("Test done");
  }

  @Test
  public void getCodexPackagesReturns401WhenConfigurationServiceReturns401() {
    logger.info("Test getCodexPackagesReturns401WhenConfigurationServiceReturns401");

    CompletableFuture<Object> future = new CompletableFuture<>();
    future.completeExceptionally(new ConfigurationServiceException("UnAuthorized to access RM API Configuration", 401));
    doReturn(future).when(configurationService).retrieveConfiguration(any());

    RestAssured
      .given()
      .header(tenantHeader)
      .header(contentTypeHeader)
      .header(urlHeader)
      .header(tokenHeader)
      .get("/codex-packages?query=" + INVALID_SEARCH_PACKAGES_QUERY)
      .then()
      .log()
      .ifValidationFails()
      .statusCode(401);

    logger.info("Test done");
  }

  @Test
  public void getCodexPackagesReturns500WhenHoldingIQReturns500() {
    logger.info("Test getCodexPackagesReturns500WhenHoldingIQReturns500");

    RestAssured
      .given()
      .header(tenantHeader)
      .header(contentTypeHeader)
      .header(urlHeader)
      .header(tokenHeader)
      .get("/codex-packages?query=" + NOT_MOCKED_QUERY)
      .then()
      .log()
      .ifValidationFails()
      .statusCode(500);

    logger.info("Test done");
  }

  @Test
  public void getCodexPackagesReturns400WhenQueryIsMissing() {
    logger.info("Test getCodexPackagesReturns400WhenQueryIsMissing");

    RestAssured
      .given()
      .header(tenantHeader)
      .header(contentTypeHeader)
      .header(urlHeader)
      .header(tokenHeader)
      .get("/codex-packages")
      .then()
      .log()
      .ifValidationFails()
      .statusCode(400);

    logger.info("Test done");
  }

  @Test
  public void getCodexPackagesReturns400WhenLimitIsNotValid() {
    logger.info("Test getCodexPackagesReturns400WhenLimitIsNotValid");

    RestAssured
      .given()
      .header(tenantHeader)
      .header(contentTypeHeader)
      .header(urlHeader)
      .header(tokenHeader)
      .get("/codex-packages?query=abc&limit=-1")
      .then()
      .log()
      .ifValidationFails()
      .statusCode(400);

    logger.info("Test done");
  }


  @Test
  public void getCodexPackagesIdSearchSuccessTest() {
    logger.info("Testing getCodexPackagesIdSearchSuccessTest");

    final PackageCollection response = RestAssured
      .given()
      .header(tenantHeader)
      .header(urlHeader)
      .header(tokenHeader)
      .header(contentTypeHeader)
      .get(String.format("/codex-packages?query=id=%s", CODEX_PACKAGE_ID))
      .then()
      .contentType(ContentType.JSON)
      .log()
      .ifValidationFails()
      .statusCode(200).extract().as(PackageCollection.class);

    assertEquals(response.getResultInfo().getTotalRecords(), (Integer) 1);
    assertNotNull(response.getPackages());

    logger.info("Test done");
  }

  @Test
  public void getCodexPackagesIdSearchFailTest() {
    logger.info("Testing getCodexPackagesIdSearchFailTest");

    final PackageCollection response = RestAssured
      .given()
      .header(tenantHeader)
      .header(urlHeader)
      .header(tokenHeader)
      .header(contentTypeHeader)
      .get(String.format("/codex-packages?query=id=%s", NOT_MOCKED_CODEX_PACKAGE_ID))
      .then()
      .contentType(ContentType.JSON)
      .log()
      .ifValidationFails()
      .statusCode(200).extract().as(PackageCollection.class);

    assertEquals(response.getResultInfo().getTotalRecords(), (Integer) 0);
    assertNotNull(response.getPackages());

    logger.info("Test done");
  }
}
