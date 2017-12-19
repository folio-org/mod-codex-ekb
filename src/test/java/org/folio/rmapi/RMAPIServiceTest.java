package org.folio.rmapi;

import java.io.InputStream;
import java.util.Random;

import org.apache.commons.io.IOUtils;
import org.folio.rest.RestVerticle;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

/**
 * @author cgodfrey
 *
 */
@RunWith(VertxUnitRunner.class)
public class RMAPIServiceTest {

  private final Logger logger = LoggerFactory.getLogger("okapi");
  private final String testCustId = "TESTCUSTID";
  private final String testAPIKey = "TESTAPIKEY";
  private final String testRMAPIHOST = "localhost";
  private final String SuccessTitleId = "99999";
  private final String TitleNotFoundTitleId = "1";
  private final String BadJSONTitleId = "88888";
  private final String SuccessTitleListQuery = "search=autism&searchfield=titlename&selection=0&orderby=titlename&count=5&offset=1";

  private Vertx vertx;
  private final int okapiPort = Integer.parseInt(System.getProperty("port", getRandomPort()));
  private final int rmapiPort = Integer.parseInt(System.getProperty("rmapiport", getRandomPort()));

  private static final String MOCK_CONTENT_SUCCESS_GET_TITLE_BY_ID = "RMAPIService/SuccessGetTitleById.json";
  private static final String MOCK_CONTENT_SUCCESS_GET_TITLELIST = "RMAPIService/SuccessGetTitleList.json";
  private static final String MOCK_CONTENT_TITLE_NOT_FOUND = "RMAPIService/TitleNotFound.json";
  private static final String MOCK_CONTENT_BAD_JSON = "RMAPIService/BadJson.json";

  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
  }

  /**
   * @throws java.lang.Exception
   */
  @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp(TestContext context) throws Exception {
    vertx = Vertx.vertx();
    DeploymentOptions options = new DeploymentOptions().setConfig(new JsonObject().put("http.port", okapiPort));
    vertx.deployVerticle(RestVerticle.class.getName(), options, context.asyncAssertSuccess());

    final Async async = context.async();

    HttpServer server = vertx.createHttpServer();
    server.requestHandler(req -> {
      if (req.path().equals(String.format("/rm/rmaccounts/TESTCUSTID/titles/%s", SuccessTitleId))) {
        req.response().setStatusCode(200).putHeader("content-type", "application/json")
            .end(readMockFile(MOCK_CONTENT_SUCCESS_GET_TITLE_BY_ID));
      } else if (req.path().equals(String.format("/rm/rmaccounts/TESTCUSTID/titles/%s", TitleNotFoundTitleId))) {
        req.response().setStatusCode(404).putHeader("content-type", "application/json")
            .end(readMockFile(MOCK_CONTENT_TITLE_NOT_FOUND));
      } else if (req.path().equals(String.format("/rm/rmaccounts/TESTCUSTID/titles/%s", BadJSONTitleId))) {
        req.response().setStatusCode(200).putHeader("content-type", "application/json")
            .end(readMockFile(MOCK_CONTENT_BAD_JSON));
      } else if (req.path().equals("/rm/rmaccounts/TESTCUSTID/titles")) {
        if (SuccessTitleListQuery.equals(req.query())) {
          req.response().setStatusCode(200).putHeader("content-type", "application/json")
              .end(readMockFile(MOCK_CONTENT_SUCCESS_GET_TITLELIST));
        }
      }
    });

    server.listen(rmapiPort, testRMAPIHOST, ar -> {
      context.assertTrue(ar.succeeded());

      async.complete();
    });

  }

  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown(TestContext context) throws Exception {
    vertx.close(context.asyncAssertSuccess());
  }

  @Test
  public final void testSuccessGetTitleById(TestContext context) {

    final Async async = context.async();

    RMAPIService svc = new RMAPIService(testCustId, testAPIKey, String.format("http://%s:%s", testRMAPIHOST, rmapiPort),
        vertx);

    svc.getTitleById(SuccessTitleId).whenCompleteAsync((rmapiResult, throwable) -> {
      context.assertNull(throwable);
      context.assertNotNull(rmapiResult);
      context.assertEquals(99999, rmapiResult.titleId);
      context.assertEquals("Test Title", rmapiResult.titleName);
      context.assertEquals("Test Publisher", rmapiResult.publisherName);
      context.assertEquals("Book", rmapiResult.pubType);
      context.assertNull(rmapiResult.edition);
      context.assertNotNull(rmapiResult.contributorsList);
      context.assertEquals(1, rmapiResult.contributorsList.size());
      context.assertEquals("Quinn, Harper", rmapiResult.contributorsList.get(0).titleContributor);
      context.assertEquals("author", rmapiResult.contributorsList.get(0).type);
      async.complete();
    }).exceptionally(throwable -> {
      context.fail(throwable);
      async.complete();
      return null;
    });
  }

  @Test
  public final void testSuccessGetTitleList(TestContext context) {
    final Async async = context.async();

    RMAPIService svc = new RMAPIService(testCustId, testAPIKey, String.format("http://%s:%s", testRMAPIHOST, rmapiPort),
        vertx);

    svc.getTitleList(SuccessTitleListQuery).whenCompleteAsync((rmapiResult, throwable) -> {
      context.assertNull(throwable);
      context.assertNotNull(rmapiResult);
      context.assertEquals(1385, rmapiResult.totalResults);
      context.assertNotNull(rmapiResult.titleList);
      context.assertEquals(2, rmapiResult.titleList.size());
      context.assertEquals(999999, rmapiResult.titleList.get(0).titleId);
      context.assertEquals("Test Title 1", rmapiResult.titleList.get(0).titleName);
      async.complete();
    });
  }

  @Test
  public final void testTitleNotFound(TestContext context) {

    final Async async = context.async();

    RMAPIService svc = new RMAPIService(testCustId, testAPIKey, String.format("http://%s:%s", testRMAPIHOST, rmapiPort),
        vertx);

    svc.getTitleById(TitleNotFoundTitleId).whenCompleteAsync((rmapiResult, throwable) -> {
      context.assertNotNull(throwable);
      context.assertEquals("org.folio.rmapi.RMAPIServiceException", throwable.getClass().getName());
      RMAPIServiceException ex = (RMAPIServiceException) throwable;
      context.assertEquals(404, ex.getRMAPICode());
      context.assertEquals("Not Found", ex.getRMAPIMessage());
      async.complete();
    });

  }

  @Test
  public final void testResultsBadJSON(TestContext context) {

    final Async async = context.async();

    RMAPIService svc = new RMAPIService(testCustId, testAPIKey, String.format("http://%s:%s", testRMAPIHOST, rmapiPort),
        vertx);

    svc.getTitleById(BadJSONTitleId).whenCompleteAsync((rmapiResult, throwable) -> {
      context.assertNotNull(throwable);
      context.assertEquals("org.folio.rmapi.RMAPIResultsProcessingException", throwable.getClass().getName());
      async.complete();
    });

  }

  private String readMockFile(String path) {
    try {
      InputStream is = RMAPIServiceTest.class.getClassLoader().getResourceAsStream(path);
      if (is != null) {
        return IOUtils.toString(is, "UTF-8");
      } else {
        return "";
      }
    } catch (Throwable e) {
      logger.error(String.format("Unable to read mock configuration in %s file", path));
    }
    return "";
  }

  private String getRandomPort() {
    return Integer.toString(new Random().nextInt(16_384) + 49_152);
  }
}
