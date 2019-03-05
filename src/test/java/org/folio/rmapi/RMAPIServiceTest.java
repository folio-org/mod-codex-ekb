package org.folio.rmapi;

import static org.folio.utils.Utils.readMockFile;

import org.folio.holdingsiq.model.Configuration;
import org.folio.holdingsiq.service.TitlesHoldingsIQService;
import org.folio.holdingsiq.service.exception.ResourceNotFoundException;
import org.folio.holdingsiq.service.exception.ResultsProcessingException;
import org.folio.holdingsiq.service.exception.ServiceResponseException;
import org.folio.holdingsiq.service.exception.UnAuthorizedException;
import org.folio.holdingsiq.service.impl.TitlesHoldingsIQServiceImpl;
import org.folio.rest.RestVerticle;
import org.folio.utils.Utils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

/**
 * @author cgodfrey
 *
 */
@RunWith(VertxUnitRunner.class)
public class RMAPIServiceTest {

  private final static String TEST_CUST_ID = "TESTCUSTID";
  private final static String TEST_API_KEY = "TESTAPIKEY";
  private final static String TEST_RMAPIHOST = "localhost";
  private final static int SUCCESS_TITLE_ID = 99999;
  private final static int TITLE_NOT_FOUND_TITLE_ID = 1;
  private final static int BAD_JSON_TITLE_ID = 88888;

  private final static String SUCCESS_TITLE_LIST_QUERY = "searchfield=titlename&selection=0&search=autism&orderby=titlename&count=5&offset=1";
  private final static String GATEWAY_TIMEOUT_TITLE_LIST_QUERY = "searchfield=relevance&selection=0&search=muslim%journal&orderby=titlename&count=5&offset=1";
  private final static String NO_RESULTS_TITLE_LIST_QUERY = "searchfield=relevance&selection=0&search=nnnnnnn&orderby=titlename&count=5&offset=1";
  private final static String FORBIDDEN_TITLE_LIST_QUERY = "searchfield=relevance&selection=0&search=moby%20dick&orderby=titlename&count=5&offset=1";

  private final static int UN_AUTHORIZED_TITLE_ID = 77777;

  private static Vertx vertx;
  private static int okapiPort = Integer.parseInt(System.getProperty("port", Integer.toString(Utils.getRandomPort())));
  private static int rmapiPort = Integer
      .parseInt(System.getProperty("rmapiport", Integer.toString(Utils.getRandomPort())));

  private static Configuration configuration = Configuration.builder()
    .customerId(TEST_CUST_ID)
    .apiKey(TEST_API_KEY)
    .url(String.format("http://%s:%s", TEST_RMAPIHOST, rmapiPort))
    .build();

  private static final String MOCK_CONTENT_SUCCESS_GET_TITLE_BY_ID = "RMAPIService/SuccessGetTitleById.json";
  private static final String MOCK_CONTENT_SUCCESS_GET_TITLELIST = "RMAPIService/SuccessGetTitleList.json";
  private static final String MOCK_CONTENT_TITLE_NOT_FOUND = "RMAPIService/TitleNotFound.json";
  private static final String MOCK_CONTENT_BAD_JSON = "RMAPIService/BadJson.json";
  private static final String MOCK_CONTENT_GATEWAY_TIMEOUT_JSON = "RMAPIService/GatewayTimeout.json";
  private static final String MOCK_CONTENT_SEARCH_NO_RESULTS_JSON = "RMAPIService/SearchNoResults.json";
  private static final String MOCK_CONTENT_FORBIDDEN_JSON = "RMAPIService/Forbidden.json";
  private static final String MOCK_CONTENT_UNAUTHORIZED_JSON = "RMAPIService/UnAuthorized.json";

  @BeforeClass
  public static void setUpBeforeClass(TestContext context) {
    vertx = Vertx.vertx();
    DeploymentOptions options = new DeploymentOptions().setConfig(new JsonObject().put("http.port", okapiPort));

    vertx.deployVerticle(RestVerticle.class.getName(), options, context.asyncAssertSuccess());

    HttpServer server = vertx.createHttpServer();
    server.requestHandler(req -> {
      if (req.path().equals(String.format("/rm/rmaccounts/TESTCUSTID/titles/%d", SUCCESS_TITLE_ID))) {
        req.response().setStatusCode(200).putHeader("content-type", "application/json")
          .end(readMockFile(MOCK_CONTENT_SUCCESS_GET_TITLE_BY_ID));
      } else if (req.path().equals(String.format("/rm/rmaccounts/TESTCUSTID/titles/%d", TITLE_NOT_FOUND_TITLE_ID))) {
        req.response().setStatusCode(404).putHeader("content-type", "application/json")
          .end(readMockFile(MOCK_CONTENT_TITLE_NOT_FOUND));
      } else if (req.path().equals(String.format("/rm/rmaccounts/TESTCUSTID/titles/%d", BAD_JSON_TITLE_ID))) {
        req.response().setStatusCode(200).putHeader("content-type", "application/json")
          .end(readMockFile(MOCK_CONTENT_BAD_JSON));
      } else if (req.path().equals(String.format("/rm/rmaccounts/TESTCUSTID/titles/%d", UN_AUTHORIZED_TITLE_ID))) {
        req.response().setStatusCode(401).putHeader("content-type", "application/json")
          .end(readMockFile(MOCK_CONTENT_UNAUTHORIZED_JSON));
      } else if (req.path().equals("/rm/rmaccounts/TESTCUSTID/titles")) {
        if (SUCCESS_TITLE_LIST_QUERY.equals(req.query())) {
          req.response().setStatusCode(200).putHeader("content-type", "application/json")
            .end(readMockFile(MOCK_CONTENT_SUCCESS_GET_TITLELIST));
        } else if (GATEWAY_TIMEOUT_TITLE_LIST_QUERY.equals(req.query())) {
          req.response().setStatusCode(504).putHeader("content-type", "application/json")
            .end(readMockFile(MOCK_CONTENT_GATEWAY_TIMEOUT_JSON));
        } else if (NO_RESULTS_TITLE_LIST_QUERY.equals(req.query())) {
          req.response().setStatusCode(200).putHeader("content-type", "application/json")
            .end(readMockFile(MOCK_CONTENT_SEARCH_NO_RESULTS_JSON));
        } else if (FORBIDDEN_TITLE_LIST_QUERY.equals(req.query())) {
          req.response().setStatusCode(403).putHeader("content-type", "application/json")
            .end(readMockFile(MOCK_CONTENT_FORBIDDEN_JSON));
        }
      }
    });

    server.listen(rmapiPort, TEST_RMAPIHOST, context.asyncAssertSuccess());
  }

  @AfterClass
  public static void tearDownAfterClass(TestContext context) {
    vertx.close(context.asyncAssertSuccess());
  }

  @Test
  public final void testSuccessGetTitleById(TestContext context) {
    final Async async = context.async();

    TitlesHoldingsIQService svc = new TitlesHoldingsIQServiceImpl(configuration, vertx);

    svc.retrieveTitle(SUCCESS_TITLE_ID).whenCompleteAsync((rmapiResult, throwable) -> {
      context.assertNull(throwable);
      context.assertNotNull(rmapiResult);
      context.assertEquals(99999, rmapiResult.getTitleId());
      context.assertEquals("Test Title", rmapiResult.getTitleName());
      context.assertEquals("Test Publisher", rmapiResult.getPublisherName());
      context.assertEquals("Book", rmapiResult.getPubType());
      context.assertNull(rmapiResult.getEdition());
      context.assertNotNull(rmapiResult.getContributorsList());
      context.assertEquals(1, rmapiResult.getContributorsList().size());
      context.assertEquals("Quinn, Harper", rmapiResult.getContributorsList().get(0).getTitleContributor());
      context.assertEquals("author", rmapiResult.getContributorsList().get(0).getType());
      context.assertNotNull(rmapiResult.getSubjectsList());
      context.assertEquals(1, rmapiResult.getSubjectsList().size());
      context.assertEquals("MEDICAL / Physician & Patient", rmapiResult.getSubjectsList().get(0).getValue());
      context.assertEquals("BISAC", rmapiResult.getSubjectsList().get(0).getType());
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

    TitlesHoldingsIQService svc = new TitlesHoldingsIQServiceImpl(configuration, vertx);

    svc.retrieveTitles(SUCCESS_TITLE_LIST_QUERY).whenCompleteAsync((rmapiResult, throwable) -> {
      context.assertNull(throwable);
      context.assertNotNull(rmapiResult);
      context.assertEquals(1385, rmapiResult.getTotalResults());
      context.assertNotNull(rmapiResult.getTitleList());
      context.assertEquals(2, rmapiResult.getTitleList().size());
      context.assertEquals(999999, rmapiResult.getTitleList().get(0).getTitleId());
      context.assertEquals("Test Title 1", rmapiResult.getTitleList().get(0).getTitleName());
      async.complete();
    });
  }

  @Test
  public final void testTitleNotFound(TestContext context) {
    final Async async = context.async();

    TitlesHoldingsIQService svc = new TitlesHoldingsIQServiceImpl(configuration, vertx);

    svc.retrieveTitle(TITLE_NOT_FOUND_TITLE_ID).whenCompleteAsync((rmapiResult, throwable) -> {
      context.assertNotNull(throwable);
      context.assertTrue(throwable instanceof ResourceNotFoundException);
      async.complete();
    });

  }

  @Test
  public final void testResultsBadJSON(TestContext context) {
    final Async async = context.async();

    TitlesHoldingsIQService svc = new TitlesHoldingsIQServiceImpl(configuration, vertx);

    svc.retrieveTitle(BAD_JSON_TITLE_ID).whenCompleteAsync((rmapiResult, throwable) -> {
      context.assertNotNull(throwable);
      context.assertTrue(throwable instanceof ResultsProcessingException);
      async.complete();
    });

  }

  @Test
  public final void testGatewayTimeout(TestContext context) {
    final Async async = context.async();

    TitlesHoldingsIQService svc = new TitlesHoldingsIQServiceImpl(configuration, vertx);

    svc.retrieveTitles(GATEWAY_TIMEOUT_TITLE_LIST_QUERY).whenCompleteAsync((rmapiResult, throwable) -> {
      context.assertNotNull(throwable);
      context.assertTrue(throwable instanceof ServiceResponseException);
      ServiceResponseException ex = (ServiceResponseException) throwable;
      context.assertEquals(504, ex.getCode());
      context.assertEquals("Gateway Timeout", ex.getResponseMessage());
      async.complete();
    });
  }

  @Test
  public final void testUnAuthorizedTitleId(TestContext context) {
    final Async async = context.async();

    TitlesHoldingsIQService svc = new TitlesHoldingsIQServiceImpl(configuration, vertx);

    svc.retrieveTitle(UN_AUTHORIZED_TITLE_ID).whenCompleteAsync((rmapiResult, throwable) -> {
      context.assertNotNull(throwable);
      context.assertTrue(throwable instanceof UnAuthorizedException);
      async.complete();
    });
  }

  @Test
  public final void testForbiddenResultList(TestContext context) {
    final Async async = context.async();

    TitlesHoldingsIQService svc = new TitlesHoldingsIQServiceImpl(configuration, vertx);

    svc.retrieveTitles(FORBIDDEN_TITLE_LIST_QUERY).whenCompleteAsync((rmapiResult, throwable) -> {
      context.assertNotNull(throwable);
      context.assertTrue(throwable instanceof UnAuthorizedException);
      async.complete();
    });
  }

  @Test
  public final void testNoResultsGetTitleList(TestContext context) {
    final Async async = context.async();

    TitlesHoldingsIQService svc = new TitlesHoldingsIQServiceImpl(configuration, vertx);

    svc.retrieveTitles(NO_RESULTS_TITLE_LIST_QUERY).whenCompleteAsync((rmapiResult, throwable) -> {
      context.assertNull(throwable);
      context.assertNotNull(rmapiResult);
      context.assertEquals(0, rmapiResult.getTotalResults());
      context.assertNotNull(rmapiResult.getTitleList());
      context.assertEquals(0, rmapiResult.getTitleList().size());
      async.complete();
    });
  }
}
