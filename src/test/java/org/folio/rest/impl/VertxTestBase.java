package org.folio.rest.impl;

import org.folio.rest.RestVerticle;
import org.folio.rest.tools.PomReader;
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
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;

public class VertxTestBase {
  protected final Logger logger = LoggerFactory.getLogger("CodexInstancesResourceImplTest");
  protected final int okapiPort = Utils.getRandomPort();
  protected final Header tenantHeader = new Header("x-okapi-tenant", "codexinstancesresourceimpltest");
  protected final Header tokenHeader = new Header("x-okapi-token", "codexinstancesresourceimpltest");
  protected final String url = "https://localhost:" + okapiPort;
  protected final Header urlHeader = new Header("x-okapi-url", url);
  protected final Header contentTypeHeader = new Header("Content-Type", "application/json");
  protected Vertx vertx;

  @Before
  public void setUp(TestContext context) {
    Async async = context.async();
    vertx = Vertx.vertx();

    String moduleName = PomReader.INSTANCE.getModuleName().replaceAll("_", "-");
    String moduleVersion = PomReader.INSTANCE.getVersion();
    String moduleId = moduleName + "-" + moduleVersion;
    logger.info("Test setup starting for " + moduleId);

    final JsonObject conf = new JsonObject();
    conf.put("http.port", okapiPort);
    conf.put("spring.configuration", "org.folio.spring.TestConfig");

    final DeploymentOptions opt = new DeploymentOptions().setConfig(conf);
    vertx.deployVerticle(RestVerticle.class.getName(), opt, event -> {
      context.assertTrue(event.succeeded());
      async.complete();
    });
    RestAssured.port = okapiPort;
    logger.info("Codex Instances Resource Test Setup Done using port " + okapiPort);

    async.awaitSuccess();
  }

  @After
  public void tearDown(TestContext context) {
    logger.info("Codex Instances Resource Testing Complete");
    vertx.close(context.asyncAssertSuccess());
  }
}
