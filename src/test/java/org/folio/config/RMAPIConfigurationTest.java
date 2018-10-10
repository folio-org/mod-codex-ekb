package org.folio.config;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javax.ws.rs.NotAuthorizedException;

import org.folio.rest.RestVerticle;
import org.folio.rest.tools.client.test.HttpClientMock2;
import org.folio.utils.Utils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

/**
 * Tests the RMAPIConfiguration class.
 *
 * Note: RMAPIConfiguration uses the same URL path when called, so to use the
 * mock content we need to supply different mock files. However, the
 * HttpClientInterface is local to the RMAPIConfiguration class and setting the
 * mock file is a method in HttpClientMock2. Since we don't have access to this
 * from RMAPIConfiguration and given setMockJsonContent sets a static variable,
 * we create a local HttpClientMock2 and before each call to RMAPIConfiguration
 * we set the mock file via the local HttpClientMock2 object. This sets the
 * static mockJson object that will be used by RMAPIConfiguration's local
 * HttpClientInterface (which will be a HttpClientMock2 instance) and the tests
 * function. *phew*! There is likely a better way to do this or at least there
 * should be.
 *
 * TODO: figure out a better way to set the mock files.
 *
 * @author mreno
 *
 */
@RunWith(VertxUnitRunner.class)
public class RMAPIConfigurationTest {
  private static final String MOCK_CONTENT_SUCCESS_FILE = "RMAPIConfiguration/mock_content_success.json";
  private static final String MOCK_CONTENT_FAIL_EMPTY_FILE = "RMAPIConfiguration/mock_content_fail_empty.json";
  private static final String MOCK_CONTENT_FAIL_MISSING_CUSTOMER_ID_FILE = "RMAPIConfiguration/mock_content_fail_missing_customer_id.json";
  private static final String MOCK_CONTENT_FAIL_MISSING_API_KEY_FILE = "RMAPIConfiguration/mock_content_fail_missing_api_key.json";
  private static final String MOCK_CONTENT_FAIL_MISSING_URL_FILE = "RMAPIConfiguration/mock_content_fail_missing_url.json";
  private static final String MOCK_CONTENT_SUCCESS_MULTIPLE_FILE = "RMAPIConfiguration/mock_content_success_multiple.json";
  private static final String MOCK_CONTENT_SUCCESS_TO_STRING_FILE = "RMAPIConfiguration/mock_content_success_to_string.json";
  private static final String MOCK_CONTENT_FAIL_404_FILE = "RMAPIConfiguration/mock_content_fail_404.json";
  private static final String MOCK_CONTENT_FAIL_401_FILE = "RMAPIConfiguration/mock_content_fail_401.json";
  private static final String MOCK_CONTENT_FAIL_INTERNAL_ERROR_FILE = "RMAPIConfiguration/mock_content_fail_internal_error.json";

  private final Logger logger = LoggerFactory.getLogger("okapi");

  private Vertx vertx;
  private Map<String, String> okapiHeaders = new HashMap<>();
  // HACK ALERT! This object is needed to modify RMAPIConfiguration's local
  // object as a side effect.
  private HttpClientMock2 httpClientMock;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp(TestContext context) throws Exception {
    final int port = Utils.getRandomPort();

    vertx = Vertx.vertx();

    JsonObject conf = new JsonObject()
        .put("http.port", port)
        .put(HttpClientMock2.MOCK_MODE, "true");

    logger.info("RM API Configuration Test: Deploying "
        + RestVerticle.class.getName() + ' ' + Json.encode(conf));

    DeploymentOptions opt = new DeploymentOptions().setConfig(conf);
    vertx.deployVerticle(RestVerticle.class.getName(), opt,
        context.asyncAssertSuccess());

    okapiHeaders.put("x-okapi-tenant", "rmapiconfigurationtest");
    okapiHeaders.put("x-okapi-url", "http://localhost:" + Integer.toString(port));

    // HACK ALERT! See above for the reason this is being created.
    httpClientMock = new HttpClientMock2(okapiHeaders.get("x-okapi-tenant"), okapiHeaders.get("x-okapi-url"));
  }

  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown(TestContext context) throws Exception {
    logger.info("Test complete, cleaning up...");
    vertx.close(context.asyncAssertSuccess());
  }

  @Test
  public void successTest(TestContext context) {
    Async async = context.async();

    try {
      // HACK ALERT! See above for the reason this is here.
      httpClientMock.setMockJsonContent(MOCK_CONTENT_SUCCESS_FILE);
    } catch (IOException e) {
      context.fail("Cannot read mock file: " + MOCK_CONTENT_SUCCESS_FILE +
          " - reason: " + e.getMessage());
    }

    CompletableFuture<RMAPIConfiguration> config = RMAPIConfiguration.getConfiguration(okapiHeaders);

    config.whenComplete((result, exception) -> {
      context.assertNotNull(result);

      RMAPIConfiguration rmAPIConfig = result;

      context.assertEquals("examplecorp", rmAPIConfig.getCustomerId());
      context.assertEquals("8675309", rmAPIConfig.getAPIKey());
      context.assertEquals("https://rmapi.example.com", rmAPIConfig.getUrl());

      async.complete();
    }).exceptionally(throwable -> {
      context.fail(throwable);
      async.complete();
      return null;
    });
  }

  @Test
  public void emptyTest(TestContext context) {
    Async async = context.async();

    try {
      // HACK ALERT! See above for the reason this is here.
      httpClientMock.setMockJsonContent(MOCK_CONTENT_FAIL_EMPTY_FILE);
    } catch (IOException e) {
      context.fail("Cannot read mock file: " + MOCK_CONTENT_FAIL_EMPTY_FILE +
          " - reason: " + e.getMessage());
    }

    CompletableFuture<RMAPIConfiguration> config = RMAPIConfiguration.getConfiguration(okapiHeaders);

    config.whenComplete((result, exception) -> {
      context.assertNull(result);
      async.complete();
    });
  }

  @Test
  public void missingCustomerIdTest(TestContext context) {
    Async async = context.async();

    try {
      // HACK ALERT! See above for the reason this is here.
      httpClientMock.setMockJsonContent(MOCK_CONTENT_FAIL_MISSING_CUSTOMER_ID_FILE);
    } catch (IOException e) {
      context.fail("Cannot read mock file: " +
          MOCK_CONTENT_FAIL_MISSING_CUSTOMER_ID_FILE + " - reason: " +
          e.getMessage());
    }

    CompletableFuture<RMAPIConfiguration> config = RMAPIConfiguration.getConfiguration(okapiHeaders);

    config.whenComplete((result, exception) -> {
      context.assertNull(result);
      async.complete();
    });
  }

  @Test
  public void missingApiKeyTest(TestContext context) {
    Async async = context.async();

    try {
      // HACK ALERT! See above for the reason this is here.
      httpClientMock.setMockJsonContent(MOCK_CONTENT_FAIL_MISSING_API_KEY_FILE);
    } catch (IOException e) {
      context.fail("Cannot read mock file: " +
          MOCK_CONTENT_FAIL_MISSING_API_KEY_FILE + " - reason: " +
          e.getMessage());
    }

    CompletableFuture<RMAPIConfiguration> config = RMAPIConfiguration.getConfiguration(okapiHeaders);

    config.whenComplete((result, exception) -> {
      context.assertNull(result);
      async.complete();
    });
  }

  @Test
  public void missingUrlTest(TestContext context) {
    Async async = context.async();

    try {
      // HACK ALERT! See above for the reason this is here.
      httpClientMock.setMockJsonContent(MOCK_CONTENT_FAIL_MISSING_URL_FILE);
    } catch (IOException e) {
      context.fail("Cannot read mock file: " +
          MOCK_CONTENT_FAIL_MISSING_URL_FILE + " - reason: " +
          e.getMessage());
    }

    CompletableFuture<RMAPIConfiguration> config = RMAPIConfiguration.getConfiguration(okapiHeaders);

    config.whenComplete((result, exception) -> {
      context.assertNull(result);
      async.complete();
    });
  }

  @Test
  public void multipleConfigsTest(TestContext context) {
    Async async = context.async();

    try {
      // HACK ALERT! See above for the reason this is here.
      httpClientMock.setMockJsonContent(MOCK_CONTENT_SUCCESS_MULTIPLE_FILE);
    } catch (IOException e) {
      context.fail("Cannot read mock file: " +
          MOCK_CONTENT_SUCCESS_MULTIPLE_FILE + " - reason: " + e.getMessage());
    }

    CompletableFuture<RMAPIConfiguration> config = RMAPIConfiguration.getConfiguration(okapiHeaders);
    config.whenComplete((result, exception) -> {
      context.assertNotNull(result);

      RMAPIConfiguration rmAPIConfig = result;

      context.assertEquals("examplecorp", rmAPIConfig.getCustomerId());
      context.assertEquals("8675309", rmAPIConfig.getAPIKey());
      context.assertEquals("https://rmapi.example.com", rmAPIConfig.getUrl());

      async.complete();
    }).exceptionally(throwable -> {
      context.fail(throwable);
      async.complete();
      return null;
    });
  }

  @Test
  public void toStringTest(TestContext context) {
    Async async = context.async();

    try {
      // HACK ALERT! See above for the reason this is here.
      httpClientMock.setMockJsonContent(MOCK_CONTENT_SUCCESS_TO_STRING_FILE);
    } catch (IOException e) {
      context.fail("Cannot read mock file: " +
          MOCK_CONTENT_SUCCESS_TO_STRING_FILE + " - reason: " + e.getMessage());
    }

    CompletableFuture<RMAPIConfiguration> config = RMAPIConfiguration.getConfiguration(okapiHeaders);
    config.whenComplete((result, exception) -> {
      context.assertNotNull(result);

      RMAPIConfiguration rmAPIConfig = result;

      context.assertEquals("RMAPIConfiguration [customerId=myid, apiKey=mykey, url=https://rmapi.example.com]", rmAPIConfig.toString());

      async.complete();
    }).exceptionally(throwable -> {
      context.fail(throwable);
      async.complete();
      return null;
    });
  }
  
  @Test
  public void return404Test(TestContext context) {
	Async async = context.async();
	
    try {
	  // HACK ALERT! See above for the reason this is here.
	  httpClientMock.setMockJsonContent(MOCK_CONTENT_FAIL_404_FILE);
    } catch (IOException e) {
	  context.fail("Cannot read mock file: " + MOCK_CONTENT_FAIL_404_FILE + " - reason: " + e.getMessage());
    }
    CompletableFuture<RMAPIConfiguration> config = RMAPIConfiguration.getConfiguration(okapiHeaders);
    config.whenComplete((result, exception) -> {
	  context.assertNull(result);
      async.complete();
   });
  }
  @Test
  public void return401Test(TestContext context) {
	Async async = context.async();
	
	try {
	  // HACK ALERT! See above for the reason this is here.
	  httpClientMock.setMockJsonContent(MOCK_CONTENT_FAIL_401_FILE);
	} catch (IOException e) {
	  context.fail("Cannot read mock file: " +
	  MOCK_CONTENT_FAIL_401_FILE + " - reason: " + e.getMessage());
	}
	
	CompletableFuture<RMAPIConfiguration> config = RMAPIConfiguration.getConfiguration(okapiHeaders);
	config.whenComplete((result, exception) -> {
	  context.assertNull(result);
	  context.assertTrue(exception.getCause() instanceof NotAuthorizedException);
	
	  async.complete();
	});
  }
  @Test
  public void cannotGetConfigDataTest(TestContext context) {
    Async async = context.async();

    try {
      // HACK ALERT! See above for the reason this is here.
      httpClientMock.setMockJsonContent(MOCK_CONTENT_FAIL_INTERNAL_ERROR_FILE);
    } catch (IOException e) {
      context.fail("Cannot read mock file: " +
          MOCK_CONTENT_FAIL_INTERNAL_ERROR_FILE + " - reason: " + e.getMessage());
    }

    CompletableFuture<RMAPIConfiguration> config = RMAPIConfiguration.getConfiguration(okapiHeaders);
    config.whenComplete((result, exception) -> {
      context.assertNull(result);
      context.assertTrue(exception instanceof NullPointerException);

      async.complete();
    });
  }

  @Test
  public void missingOkapiURLHeaderTest(TestContext context) {
    Async async = context.async();

    okapiHeaders.remove("x-okapi-url");

    CompletableFuture<RMAPIConfiguration> config = RMAPIConfiguration.getConfiguration(okapiHeaders);
    config.whenComplete((result, exception) -> {
      context.assertNull(result);
      context.assertTrue(exception instanceof IllegalArgumentException);

      async.complete();
    });
  }
}
