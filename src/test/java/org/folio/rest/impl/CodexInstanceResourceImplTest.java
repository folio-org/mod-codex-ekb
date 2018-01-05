package org.folio.rest.impl;

import static org.folio.utils.Utils.readMockFile;
import static org.hamcrest.Matchers.containsString;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.folio.rest.RestVerticle;
import org.folio.rest.tools.PomReader;
import org.folio.rest.tools.client.test.HttpClientMock2;
import org.folio.utils.Utils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import io.restassured.response.Response;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(VertxUnitRunner.class)
public class CodexInstanceResourceImplTest {
  private static final String MOCK_RMAPI_CONFIG_SUCCESS_FILE = "RMAPIToCodex/mock_content.json";
  private static final String MOCK_RMAPI_INSTANCE_TITLE_200_RESPONSE_WHEN_FOUND = "RMAPIService/SuccessGetTitleById.json";
  private static final String MOCK_RMAPI_INSTANCE_TITLE_404_RESPONSE_WHEN_NOT_FOUND = "RMAPIConfiguration/mock_content_fail_404.json";
  private static final String MOCK_CODEX_INSTANCE_TITLE_COLLECTION_200_RESPONSE_WHEN_FOUND = "RMAPIService/SuccessGetTitleList.json";

  private static final String SEARCH_TITLE_COLLECTION_WHEN_SEARCH_FIELD_NOT_GIVEN_SUCCESS_QUERY = "Bridget Jones";
  private static final String SEARCH_TITLE_COLLECTION_FAILS_UNSUPPORTED_QUERY = "title = Bridget Jones or publisher = xyz";

  private final Logger logger = LoggerFactory.getLogger("CodexInstancesResourceImplTest");

  private final int okapiPort = Utils.getRandomPort();

  private final Header tenantHeader = new Header("X-Okapi-Tenant", "codexinstancesresourceimpltest");
  private final Header urlHeader = new Header("X-Okapi-Url", "https://localhost:" + okapiPort);
  private final Header contentTypeHeader = new Header("Content-Type", "application/json");

  private String moduleName;
  private String moduleVersion;
  private String moduleId;
  private Vertx vertx;
  // This object is needed to modify RMAPIConfiguration's local
  // object as a side effect.
  private HttpClientMock2 httpClientMock;

  @Before
  public void setUp(TestContext context) {
    vertx = Vertx.vertx();

    moduleName = PomReader.INSTANCE.getModuleName().replaceAll("_", "-");
    moduleVersion = PomReader.INSTANCE.getVersion();
    moduleId = moduleName + "-" + moduleVersion;
    logger.info("Test setup starting for " + moduleId);

    final JsonObject conf = new JsonObject();
    conf.put("http.port", okapiPort);
    conf.put(HttpClientMock2.MOCK_MODE, "true");

    final DeploymentOptions opt = new DeploymentOptions().setConfig(conf);
    vertx.deployVerticle(RestVerticle.class.getName(), opt, context.asyncAssertSuccess());
    RestAssured.port = okapiPort;
    logger.info("Codex Instances Resource Test Setup Done using port " + okapiPort);

    final int serverPort = Integer.parseInt(System.getProperty("serverPort", Integer.toString(51234)));
    final String host = "localhost";

    final Async async = context.async();
    final HttpServer server = vertx.createHttpServer();
    server.requestHandler(req -> {
      if (req.path().equals(String.format("/rm/rmaccounts/test/titles/%s", "99999"))) {
        req.response().setStatusCode(200).putHeader("content-type", "application/json")
            .end(readMockFile(MOCK_RMAPI_INSTANCE_TITLE_200_RESPONSE_WHEN_FOUND));
      } else if (req.path().equals(String.format("/rm/rmaccounts/test/titles/%s", "1"))) {
        req.response().setStatusCode(404).putHeader("content-type", "text/plain")
            .end(readMockFile(MOCK_RMAPI_INSTANCE_TITLE_404_RESPONSE_WHEN_NOT_FOUND));
      } else if (req.path().equals("/rm/rmaccounts/test/titles")) {
        if (req.uri().contains("search=Bridget+Jones&searchfield=titlename&orderby=titlename&count=10&offset=1")) {
          req.response().setStatusCode(200).putHeader("content-type", "application/json")
          .end(readMockFile(MOCK_CODEX_INSTANCE_TITLE_COLLECTION_200_RESPONSE_WHEN_FOUND));
        }
      }
    });

    server.listen(serverPort, host, ar -> {
      async.complete();
    });

    final Map<String, String> okapiHeaders = new HashMap<>();
    okapiHeaders.put("X-Okapi-Tenant", "codexinstancesresourceimpltest");
    okapiHeaders.put("X-Okapi-Url", "https://localhost:" + Integer.toString(okapiPort));
    httpClientMock = new HttpClientMock2(okapiHeaders.get("X-Okapi-Tenant"), okapiHeaders.get("X-Okapi-Url"));
    try {
      // Mocking the RM API Configuration response
      httpClientMock.setMockJsonContent(MOCK_RMAPI_CONFIG_SUCCESS_FILE);
    } catch (final IOException e) {
      context.fail("Cannot read mock file: " + MOCK_RMAPI_CONFIG_SUCCESS_FILE + " - reason: " + e.getMessage());
    }
  }

  @After
  public void tearDown(TestContext context) {
    logger.info("Codex Instances Resource Testing Complete");
    vertx.close(context.asyncAssertSuccess());
  }

  @Test
  public void getCodexInstancesByIdSuccessTest(TestContext context) {
    final Async asyncLocal = context.async();
    logger.info("Testing for successful instance id");

    final Response r = RestAssured
        .given()
          .header(tenantHeader)
          .header(urlHeader)
          .header(contentTypeHeader)
        .get("/codex-instances/99999")
          .then()
            .contentType(ContentType.JSON)
            .log()
            .ifValidationFails()
            .statusCode(200).extract().response();

    final String body = r.getBody().asString();
    final JsonObject json = new JsonObject(body);

    context.assertTrue("99999".equals(json.getString("id")), body);

    // Test done
    logger.info("Test done");
    asyncLocal.complete();
  }

  @Test
  public void getCodexInstancesByIdThrowsExceptionWhenOkapiURLIsEmptyTest(TestContext context) {
    final Async asyncLocal = context.async();
    logger.info("Test when Okapi URL is null is starting");

    RestAssured
      .given()
        .header(tenantHeader)
        .header(contentTypeHeader)
      .get("/codex-instances/396805")
        .then()
          .log()
          .ifValidationFails()
          .statusCode(500).body(containsString("Okapi URL cannot be null"));

    // Test done
    logger.info("Test done");
    asyncLocal.complete();
  }

  @Test
  public void getCodexInstancesByIdTitleNotFoundTest(TestContext context) {
    final Async asyncLocal = context.async();
    logger.info("Testing for response when title not found");

    RestAssured
    .given()
      .header(tenantHeader)
      .header(urlHeader)
      .header(contentTypeHeader)
    .get("/codex-instances/1")
      .then()
        .log()
        .ifValidationFails()
        .statusCode(404);

    // Test done
    logger.info("Test done");
    asyncLocal.complete();
  }

  @Test
  public void getCodexInstancesSuccessTest(TestContext context) {
    final Async asyncLocal = context.async();
    logger.info("Testing for successful instance collection");

    final Response r = RestAssured
        .given()
          .header(tenantHeader)
          .header(urlHeader)
          .header(contentTypeHeader)
        .get(String.format("/codex-instances?query=%s", SEARCH_TITLE_COLLECTION_WHEN_SEARCH_FIELD_NOT_GIVEN_SUCCESS_QUERY))
          .then()
            .contentType(ContentType.JSON)
            .log()
            .ifValidationFails()
            .statusCode(200).extract().response();

    if(r != null) { //Ensure that the response is not null
      final String body = r.getBody().asString();
      final JsonObject json = new JsonObject(body);
      //Ensure that total records and instances keys are present in response
      context.assertTrue(json.containsKey("resultInfo"));
      context.assertTrue(json.containsKey("instances"));
    }

    // Test done
    logger.info("Test done");
    asyncLocal.complete();
  }

  @Test
  public void getCodexInstancesIdSearchSuccessTest(TestContext context) {
    final Async asyncLocal = context.async();
    logger.info("Testing for successful instance collection");

    final Response r = RestAssured
        .given()
          .header(tenantHeader)
          .header(urlHeader)
          .header(contentTypeHeader)
        .get(String.format("/codex-instances?query=id=%d", 99999))
          .then()
            .contentType(ContentType.JSON)
            .log()
            .ifValidationFails()
            .statusCode(200).extract().response();

    if(r != null) { //Ensure that the response is not null
      final String body = r.getBody().asString();
      final JsonObject json = new JsonObject(body);
      //Ensure that total records and instances keys are present in response
      context.assertEquals(json.getInteger("totalRecords"), Integer.valueOf(1));
      context.assertTrue(json.containsKey("instances"));
    }

    // Test done
    logger.info("Test done");
    asyncLocal.complete();
  }

  @Test
  public void getCodexInstancesIdSearchFailTest(TestContext context) {
    final Async asyncLocal = context.async();
    logger.info("Testing for successful instance collection");

    final Response r = RestAssured
        .given()
          .header(tenantHeader)
          .header(urlHeader)
          .header(contentTypeHeader)
        .get(String.format("/codex-instances?query=id=%d", 1))
          .then()
            .contentType(ContentType.JSON)
            .log()
            .ifValidationFails()
            .statusCode(200).extract().response();

    if(r != null) { //Ensure that the response is not null
      final String body = r.getBody().asString();
      final JsonObject json = new JsonObject(body);
      //Ensure that total records and instances keys are present in response
      context.assertEquals(json.getInteger("totalRecords"), Integer.valueOf(0));
      context.assertTrue(json.containsKey("instances"));
    }

    // Test done
    logger.info("Test done");
    asyncLocal.complete();
  }

  @Test
  public void getCodexInstancesHandlesInvalidQueryTest(TestContext context) {
    final Async asyncLocal = context.async();
    logger.info("Test when query is invalid, exception is thrown");

    RestAssured
      .given()
        .header(tenantHeader)
        .header(contentTypeHeader)
        .header(urlHeader)
      .get(String.format("/codex-instances?%s", SEARCH_TITLE_COLLECTION_WHEN_SEARCH_FIELD_NOT_GIVEN_SUCCESS_QUERY))
        .then()
          .log()
          .ifValidationFails()
          .statusCode(500);

    // Test done
    logger.info("Test done");
    asyncLocal.complete();
  }

  @Test
  public void getCodexInstancesHandlesUnsupportedQueryTest(TestContext context) {
    final Async asyncLocal = context.async();
    logger.info("Test when query is invalid, exception is thrown");

    RestAssured
      .given()
        .header(tenantHeader)
        .header(contentTypeHeader)
        .header(urlHeader)
      .get(String.format("/codex-instances?query=%s", SEARCH_TITLE_COLLECTION_FAILS_UNSUPPORTED_QUERY))
        .then()
          .log()
          .ifValidationFails()
          .statusCode(400);

    // Test done
    logger.info("Test done");
    asyncLocal.complete();
  }
}