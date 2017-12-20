package org.folio.rest.impl;

import static org.hamcrest.Matchers.containsString;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.folio.rest.RestVerticle;
import org.folio.rest.tools.PomReader;
import org.folio.rest.tools.client.test.HttpClientMock2;
import org.folio.utils.Utils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.restassured.RestAssured;
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
            .end(readMockJsonFile(MOCK_RMAPI_INSTANCE_TITLE_200_RESPONSE_WHEN_FOUND));
      } else if (req.path().equals(String.format("/rm/rmaccounts/test/titles/%s", "1"))) {
        req.response().setStatusCode(404).putHeader("content-type", "text/plain")
            .end(readMockJsonFile(MOCK_RMAPI_INSTANCE_TITLE_404_RESPONSE_WHEN_NOT_FOUND));
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
  public void getInstanceByIdSuccessTest(TestContext context) {
    final Async asyncLocal = context.async();
    logger.info("Testing for successful instance id");
    final Response r = RestAssured.given().header(tenantHeader).header(urlHeader).header(contentTypeHeader)
        .get("/codex-instances/99999").then().log().ifValidationFails().statusCode(200).extract().response();
    final String body = r.getBody().asString();
    final JsonObject json = new JsonObject(body);
    context.assertTrue("99999".equals(json.getString("id")), body);
    // Test done
    logger.info("Test done");
    asyncLocal.complete();
  }

  @Test
  public void getInstancesByIdThrowsExceptionWhenOkapiURLIsEmptyTest(TestContext context) {
    final Async asyncLocal = context.async();
    logger.info("Test when Okapi URL is null is starting");
    RestAssured.given().header(tenantHeader).header(contentTypeHeader).get("/codex-instances/396805").then().log()
        .ifValidationFails().statusCode(500).body(containsString("Okapi URL cannot be null"));
    // Test done
    logger.info("Test done");
    asyncLocal.complete();
  }

  @Test
  public void getInstanceByIdTitleNotFoundTest(TestContext context) {
    final Async asyncLocal = context.async();
    logger.info("Testing for response when title not found");
    RestAssured.given().header(tenantHeader).header(urlHeader).header(contentTypeHeader).get("/codex-instances/1")
        .then().log().ifValidationFails().statusCode(500); // This test should be changed to check for 404 instead of
                                                           // 500 after Carole's code is checked in
    // Test done
    logger.info("Test done");
    asyncLocal.complete();
  }

  private String readMockJsonFile(String path) {
    try {
      final InputStream is = CodexInstanceResourceImplTest.class.getClassLoader().getResourceAsStream(path);
      if (is != null) {
        return IOUtils.toString(is, "UTF-8");
      } else {
        return "";
      }
    } catch (final Throwable e) {
      logger.error(String.format("Unable to read mock configuration in %s file", path));
    }
    return "";
  }
}