package org.folio.rest.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.folio.rest.RestVerticle;
import org.folio.rest.tools.PomReader;
import org.folio.rest.tools.client.test.HttpClientMock2;
import org.folio.utils.Utils;
import org.junit.After;
import org.junit.Before;

import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.unit.TestContext;

public class VertxTestBase {

  private static final String MOCK_RMAPI_CONFIG_SUCCESS_FILE = "RMAPIToCodex/mock_content.json";


  protected final Logger logger = LoggerFactory.getLogger("CodexInstancesResourceImplTest");
  protected final int okapiPort = Utils.getRandomPort();
  protected final Header tenantHeader = new Header("X-Okapi-Tenant", "codexinstancesresourceimpltest");
  protected final Header urlHeader = new Header("X-Okapi-Url", "https://localhost:" + okapiPort);
  protected final Header contentTypeHeader = new Header("Content-Type", "application/json");
  protected Vertx vertx;
  // This object is needed to modify RMAPIConfiguration's local
  // object as a side effect.
  protected HttpClientMock2 httpClientMock;

  @Before
  public void setUp(TestContext context) {
    vertx = Vertx.vertx();

    String moduleName = PomReader.INSTANCE.getModuleName().replaceAll("_", "-");
    String moduleVersion = PomReader.INSTANCE.getVersion();
    String moduleId = moduleName + "-" + moduleVersion;
    logger.info("Test setup starting for " + moduleId);

    final JsonObject conf = new JsonObject();
    conf.put("http.port", okapiPort);
    conf.put(HttpClientMock2.MOCK_MODE, "true");

    final DeploymentOptions opt = new DeploymentOptions().setConfig(conf);
    vertx.deployVerticle(RestVerticle.class.getName(), opt, context.asyncAssertSuccess());
    RestAssured.port = okapiPort;
    logger.info("Codex Instances Resource Test Setup Done using port " + okapiPort);

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
}
