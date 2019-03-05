package org.folio.rest.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import static org.folio.utils.Utils.readMockFile;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;

import org.folio.holdingsiq.model.Configuration;
import org.folio.holdingsiq.service.ConfigurationService;
import org.folio.holdingsiq.service.exception.ConfigurationServiceException;
import org.folio.rest.jaxrs.model.Package;
import org.folio.rest.jaxrs.model.Source;
import org.folio.rest.jaxrs.model.SourceCollection;
import org.folio.spring.SpringContextUtil;

@RunWith(VertxUnitRunner.class)
public class CodexPackagesImplTest extends VertxTestBase {

  private static final String MOCK_RMAPI_INSTANCE_PACKAGE_200_RESPONSE_WHEN_FOUND = "RMAPIService/SuccessGetPackageById.json";
  private static final String MOCK_RMAPI_INSTANCE_PACKAGE_404_RESPONSE_WHEN_NOT_FOUND = "RMAPIConfiguration/mock_content_fail_404.json";
  
  private static final String CUSTOMER_ID = "test";
  private static final String VENDOR_ID = "111";
  private static final String PACKAGE_ID = "222222";
  private static final String INVALID_PACKAGE_ID = "999999";
  private static final String CODEX_PACKAGE_ID = VENDOR_ID + "-" + PACKAGE_ID;


  @Autowired
  private ConfigurationService configurationService;

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

  private static String packageByIdURL(String packageId) {
    return String.format("/rm/rmaccounts/%s/vendors/%s/packages/%s", CUSTOMER_ID, VENDOR_ID, packageId);
  }

  @Test
  public void getCodexPackagesSourcesSuccessTest() {
    logger.info("Testing for successful instance id");

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

    // Test done
    logger.info("Test done");
  }

  @Test
  public void getCodexPackagesByIdTitleNotFoundTest() {
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

    // Test done
    logger.info("Test done");
  }

  @Test
  public void getCodexPackagesByIdTitleNotAuth() {
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

    // Test done
    logger.info("Test done");
  }

}
