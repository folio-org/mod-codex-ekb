package org.folio.rest.impl;

import static org.folio.utils.Utils.readMockFile;
import static org.hamcrest.Matchers.containsString;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(VertxUnitRunner.class)
public class CodexInstanceResourceImplTest extends VertxTestBase {

  private static final String MOCK_RMAPI_INSTANCE_TITLE_200_RESPONSE_WHEN_FOUND = "RMAPIService/SuccessGetTitleById.json";
  private static final String MOCK_RMAPI_INSTANCE_TITLE_404_RESPONSE_WHEN_NOT_FOUND = "RMAPIConfiguration/mock_content_fail_404.json";
  private static final String MOCK_CODEX_INSTANCE_TITLE_COLLECTION_200_RESPONSE_WHEN_FOUND = "RMAPIService/SuccessGetTitleList.json";
  private static final String MOCK_RMAPI_CONFIG_401_RESPONSE_WHEN_NOT_AUTH = "RMAPIConfiguration/mock_content_fail_401.json";

  private static final String SEARCH_TITLE_COLLECTION_WHEN_SEARCH_FIELD_NOT_GIVEN_SUCCESS_QUERY = "Bridget Jones";
  private static final String SEARCH_TITLE_COLLECTION_FAILS_UNSUPPORTED_QUERY = "title = Bridget Jones or publisher = xyz";

  @Before
  public void setUp(TestContext context) {
    super.setUp(context);
    final Async async = context.async();
    final int serverPort = Integer.parseInt(System.getProperty("serverPort", Integer.toString(51234)));
    final String host = "localhost";
    final HttpServer server = vertx.createHttpServer();
    server.requestHandler(req -> {
      if (req.path().equals(String.format("/rm/rmaccounts/test/titles/%s", "99999"))) {
        req.response().setStatusCode(200).putHeader("content-type", "application/json")
          .end(readMockFile(MOCK_RMAPI_INSTANCE_TITLE_200_RESPONSE_WHEN_FOUND));
      } else if (req.path().equals(String.format("/rm/rmaccounts/test/titles/%s", "1"))) {
        req.response().setStatusCode(404).putHeader("content-type", "text/plain")
          .end(readMockFile(MOCK_RMAPI_INSTANCE_TITLE_404_RESPONSE_WHEN_NOT_FOUND));
      } else if (req.path().equals("/rm/rmaccounts/test/titles")) {
        if (req.uri().contains("searchfield=titlename&search=Bridget+Jones&orderby=titlename&count=10&offset=1")) {
          req.response().setStatusCode(200).putHeader("content-type", "application/json")
            .end(readMockFile(MOCK_CODEX_INSTANCE_TITLE_COLLECTION_200_RESPONSE_WHEN_FOUND));
        } else {
          req.response().setStatusCode(500).end("Unexpected call: " + req.path());
        }
      } else {
        req.response().setStatusCode(500).end("Unexpected call: " + req.path());
      }
    });
    server.listen(serverPort, host, ar -> {
      async.complete();
    });
  }

  @Test
  public void getCodexInstancesByIdSuccessTest(TestContext context) {
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
  }

  @Test
  public void getCodexInstancesByIdThrowsExceptionWhenOkapiURLIsEmptyTest(TestContext context) {
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
  }

  @Test
  public void getCodexInstancesByIdTitleNotFoundTest(TestContext context) {
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
  }

  @Test
  public void getCodexInstancesByIdTitleNotAuth(TestContext context) {
    logger.info("Testing for response when not authorized");

    try {
        // Override default Mocking of RM API Configuration response
        httpClientMock.setMockJsonContent(MOCK_RMAPI_CONFIG_401_RESPONSE_WHEN_NOT_AUTH);
      } catch (final IOException e) {
        context.fail("Cannot read mock file: " + MOCK_RMAPI_CONFIG_401_RESPONSE_WHEN_NOT_AUTH + " - reason: " + e.getMessage());
      }

    RestAssured
    .given()
      .header(tenantHeader)
      .header(urlHeader)
      .header(contentTypeHeader)
    .get("/codex-instances/99999")
      .then()
        .log()
        .ifValidationFails()
        .statusCode(401);

    // Test done
    logger.info("Test done");
  }

  @Test
  public void getCodexInstancesSuccessTest(TestContext context) {
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
  }

  @Test
  public void getCodexInstancesIdSearchSuccessTest(TestContext context) {
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
      context.assertEquals(json.getJsonObject("resultInfo").getInteger("totalRecords"), Integer.valueOf(1));
      context.assertTrue(json.containsKey("instances"));
    }

    // Test done
    logger.info("Test done");
  }

  @Test
  public void getCodexInstancesIdSearchFailTest(TestContext context) {
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
      context.assertEquals(json.getJsonObject("resultInfo").getInteger("totalRecords"), Integer.valueOf(0));
      context.assertTrue(json.containsKey("instances"));
    }

    // Test done
    logger.info("Test done");
  }

  @Test
  public void getCodexInstancesHandlesInvalidQueryTest(TestContext context) {
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
          .statusCode(400);

    // Test done
    logger.info("Test done");
  }

  @Test
  public void getCodexInstancesHandlesUnsupportedQueryTest(TestContext context) {
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
  }
}
