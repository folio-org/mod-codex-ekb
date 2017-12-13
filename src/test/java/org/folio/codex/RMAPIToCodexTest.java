package org.folio.codex;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletionException;

import org.folio.config.RMAPIConfiguration;
import org.folio.cql2rmapi.CQLParserForRMAPI;
import org.folio.cql2rmapi.QueryValidationException;
import org.folio.rest.RestVerticle;
import org.folio.rest.tools.client.test.HttpClientMock2;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

/**
 * @author mreno
 *
 */
@RunWith(VertxUnitRunner.class)
public class RMAPIToCodexTest {
  private static final String MOCK_CONTENT_FILE = "RMAPIToCodex/mock_content.json";

  private final Logger logger = LoggerFactory.getLogger("okapi");

  private Vertx vertx;
  // Use a random ephemeral port if not defined via a system property
  private final int port = Integer.parseInt(System.getProperty("port",
      Integer.toString(new Random().nextInt(16_384) + 49_152)));

  private Map<String, String> okapiHeaders = new HashMap<>();
  // HACK ALERT! This object is needed to modify RMAPIConfiguration's local
  // object as a side effect.
  private HttpClientMock2 httpClientMock;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp(TestContext context) throws Exception {
    vertx = Vertx.vertx();

    JsonObject conf = new JsonObject()
        .put("http.port", port)
        .put(HttpClientMock2.MOCK_MODE, "true");

    logger.info("RM API to Codex Test: Deploying "
        + RestVerticle.class.getName() + ' ' + Json.encode(conf));

    DeploymentOptions opt = new DeploymentOptions().setConfig(conf);
    vertx.deployVerticle(RestVerticle.class.getName(), opt,
        context.asyncAssertSuccess());

    final int serverPort = Integer.parseInt(System.getProperty("serverPort",
        Integer.toString(51234)));
    String host = "localhost";

    Async async = context.async();
    HttpServer server = vertx.createHttpServer();
    server.requestHandler(req -> {
      if (req.path().equals("/rm/rmaccounts/test/titles")) {
        req.response().setStatusCode(200).end("{\"totalResults\":524,\"titles\":[{\"titleId\":9950115,\"titleName\":\"[Resolute Dick]\",\"publisherName\":\"Unspecified\",\"identifiersList\":[{\"id\":\"4667067\",\"source\":\"AtoZ\",\"subtype\":0,\"type\":9}],\"subjectsList\":null,\"isTitleCustom\":false,\"pubType\":\"Journal\",\"customerResourcesList\":[]},{\"titleId\":1816494,\"titleName\":\"1851 - Herman Melville's Moby Dick is Published\",\"publisherName\":\"Unspecified\",\"identifiersList\":[{\"id\":\"1816494\",\"source\":\"AtoZ\",\"subtype\":0,\"type\":9}],\"subjectsList\":null,\"isTitleCustom\":false,\"pubType\":\"StreamingVideo\",\"customerResourcesList\":[{\"titleId\":1816494,\"packageId\":7349,\"packageName\":\"Ambrose Video 2.0\",\"packageType\":\"Variable\",\"isPackageCustom\":false,\"vendorId\":933,\"vendorName\":\"Ambrose Video Publishing, Inc.\",\"locationId\":5735819,\"isSelected\":false,\"isTokenNeeded\":true,\"visibilityData\":{\"isHidden\":false,\"reason\":\"\"},\"managedCoverageList\":[],\"customCoverageList\":[],\"coverageStatement\":null,\"managedEmbargoPeriod\":{\"embargoUnit\":null,\"embargoValue\":0},\"customEmbargoPeriod\":{\"embargoUnit\":null,\"embargoValue\":0},\"url\":\"http://dma.iriseducation.org/?license=[[license code]]&DMA2&src=XLS&segment=300496&pid=2037&sku=GAA-002-03\",\"userDefinedField1\":null,\"userDefinedField2\":null,\"userDefinedField3\":null,\"userDefinedField4\":null,\"userDefinedField5\":null}]},{\"titleId\":4634949,\"titleName\":\"60 minutes. Dick Clarke\",\"publisherName\":\"Unspecified\",\"identifiersList\":[{\"id\":\"3140739\",\"source\":\"AtoZ\",\"subtype\":0,\"type\":9}],\"subjectsList\":null,\"isTitleCustom\":false,\"pubType\":\"Journal\",\"customerResourcesList\":[{\"titleId\":4634949,\"packageId\":1367800,\"packageName\":\"60 Minutes 1997-2014\",\"packageType\":\"Complete\",\"isPackageCustom\":false,\"vendorId\":413,\"vendorName\":\"Alexander Street Press\",\"locationId\":12526676,\"isSelected\":false,\"isTokenNeeded\":false,\"visibilityData\":{\"isHidden\":false,\"reason\":\"\"},\"managedCoverageList\":[{\"beginCoverage\":\"2008-01-01\",\"endCoverage\":\"2008-12-31\"}],\"customCoverageList\":[],\"coverageStatement\":null,\"managedEmbargoPeriod\":{\"embargoUnit\":null,\"embargoValue\":0},\"customEmbargoPeriod\":{\"embargoUnit\":null,\"embargoValue\":0},\"url\":\"http://www.aspresolver.com/aspresolver.asp?CBSV;2774880\",\"userDefinedField1\":null,\"userDefinedField2\":null,\"userDefinedField3\":null,\"userDefinedField4\":null,\"userDefinedField5\":null}]},{\"titleId\":4635334,\"titleName\":\"60 minutes. The vice president [Dick Cheney]\",\"publisherName\":\"Unspecified\",\"identifiersList\":[{\"id\":\"3141124\",\"source\":\"AtoZ\",\"subtype\":0,\"type\":9}],\"subjectsList\":null,\"isTitleCustom\":false,\"pubType\":\"Journal\",\"customerResourcesList\":[{\"titleId\":4635334,\"packageId\":1367800,\"packageName\":\"60 Minutes 1997-2014\",\"packageType\":\"Complete\",\"isPackageCustom\":false,\"vendorId\":413,\"vendorName\":\"Alexander Street Press\",\"locationId\":12527074,\"isSelected\":false,\"isTokenNeeded\":false,\"visibilityData\":{\"isHidden\":false,\"reason\":\"\"},\"managedCoverageList\":[{\"beginCoverage\":\"2001-01-01\",\"endCoverage\":\"2001-12-31\"}],\"customCoverageList\":[],\"coverageStatement\":null,\"managedEmbargoPeriod\":{\"embargoUnit\":null,\"embargoValue\":0},\"customEmbargoPeriod\":{\"embargoUnit\":null,\"embargoValue\":0},\"url\":\"http://www.aspresolver.com/aspresolver.asp?CBSV;2774149\",\"userDefinedField1\":null,\"userDefinedField2\":null,\"userDefinedField3\":null,\"userDefinedField4\":null,\"userDefinedField5\":null}]},{\"titleId\":9948551,\"titleName\":\"A catalogue of books in several faculties and languages, Consisting of a choice collection in divinity, philosophy, philology, phisick, cosmography, history, mathematicks and chronology. Together with\",\"publisherName\":\"Unspecified\",\"identifiersList\":[{\"id\":\"4665550\",\"source\":\"AtoZ\",\"subtype\":0,\"type\":9}],\"subjectsList\":null,\"isTitleCustom\":false,\"pubType\":\"Journal\",\"customerResourcesList\":[]}]}");
      } else if (req.path().equals("/rm/rmaccounts/test/titles/1619585")) {
        req.response().setStatusCode(200).end("{\"titleId\":1619585,\"titleName\":\"Tom, Dick and Harry\",\"publisherName\":\"Project Gutenberg Literary Archive Foundation\",\"identifiersList\":[{\"id\":\"1619585\",\"source\":\"AtoZ\",\"subtype\":0,\"type\":9}],\"subjectsList\":[],\"isTitleCustom\":false,\"pubType\":\"Book\",\"customerResourcesList\":[{\"titleId\":1619585,\"packageId\":6750,\"packageName\":\"Project Gutenberg eBooks\",\"packageType\":\"Variable\",\"proxy\":{\"id\":\"<n>\",\"inherited\":true},\"isPackageCustom\":false,\"vendorId\":953,\"vendorName\":\"Project Gutenberg\",\"locationId\":5137360,\"isSelected\":false,\"isTokenNeeded\":false,\"visibilityData\":{\"isHidden\":false,\"reason\":\"\"},\"managedCoverageList\":[],\"customCoverageList\":[],\"coverageStatement\":null,\"managedEmbargoPeriod\":{\"embargoUnit\":null,\"embargoValue\":0},\"customEmbargoPeriod\":{\"embargoUnit\":null,\"embargoValue\":0},\"url\":\"http://www.gutenberg.org/ebooks/20992\",\"userDefinedField1\":null,\"userDefinedField2\":null,\"userDefinedField3\":null,\"userDefinedField4\":null,\"userDefinedField5\":null},{\"titleId\":1619585,\"packageId\":19153,\"packageName\":\"Project Gutenberg eBooks Archive Collection\",\"packageType\":\"Variable\",\"proxy\":{\"id\":\"<n>\",\"inherited\":true},\"isPackageCustom\":false,\"vendorId\":953,\"vendorName\":\"Project Gutenberg\",\"locationId\":7435416,\"isSelected\":false,\"isTokenNeeded\":false,\"visibilityData\":{\"isHidden\":false,\"reason\":\"\"},\"managedCoverageList\":[],\"customCoverageList\":[],\"coverageStatement\":null,\"managedEmbargoPeriod\":{\"embargoUnit\":null,\"embargoValue\":0},\"customEmbargoPeriod\":{\"embargoUnit\":null,\"embargoValue\":0},\"url\":\"https://archive.org/details/tomdickandharry20992gut\",\"userDefinedField1\":null,\"userDefinedField2\":null,\"userDefinedField3\":null,\"userDefinedField4\":null,\"userDefinedField5\":null}],\"description\":null,\"edition\":null,\"isPeerReviewed\":false,\"contributorsList\":[{\"type\":\"author\",\"contributor\":\"Reed, Talbot Baines\"}]}");
      }
    });
    server.listen(serverPort, host, ar -> {
      context.assertTrue(ar.succeeded());
      context.put("port", serverPort);
      async.complete();
    });

    okapiHeaders.put("x-okapi-tenant", "rmapiconfigurationtest");
    okapiHeaders.put("x-okapi-url", "http://localhost:" + Integer.toString(port));

    // HACK ALERT! See above for the reason this is being created.
    httpClientMock = new HttpClientMock2(okapiHeaders.get("x-okapi-tenant"), okapiHeaders.get("x-okapi-url"));
    try {
      // HACK ALERT! See above for the reason this is here.
      httpClientMock.setMockJsonContent(MOCK_CONTENT_FILE);
    } catch (IOException e) {
      context.fail("Cannot read mock file: " + MOCK_CONTENT_FILE +
          " - reason: " + e.getMessage());
    }
  }

  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown(TestContext context) throws Exception {
    logger.info("Test complete, cleaning up...");
    vertx.close(context.asyncAssertSuccess());
  }

  /**
   * Test method for {@link org.folio.codex.RMAPIToCodex#getInstance(java.lang.String, io.vertx.core.Context, org.folio.config.RMAPIConfiguration)}.
   */
  @Test
  public void testGetInstance(TestContext context) {
    Async async = context.async();

    RMAPIConfiguration.getConfiguration(okapiHeaders).thenCompose(config -> {
      return RMAPIToCodex.getInstance("1619585", vertx.getOrCreateContext(), config);
    }).whenComplete((response, throwable) -> {
      context.assertEquals("1619585", response.getId());
      context.assertEquals("Tom, Dick and Harry", response.getTitle());
      context.assertEquals(1, response.getContributor().size());
      context.assertEquals("author", response.getContributor().iterator().next().getType());
      context.assertEquals("Reed, Talbot Baines", response.getContributor().iterator().next().getName());
      context.assertEquals("Project Gutenberg Literary Archive Foundation", response.getPublisher());
      context.assertEquals("Book", response.getType());
      context.assertEquals("Electronic Resource", response.getFormat());
      context.assertTrue(response.getIdentifier().isEmpty());
      context.assertEquals("kb", response.getSource());
      context.assertTrue(response.getLanguage().isEmpty());

      async.complete();
    });
  }

  /**
   * Test method for {@link org.folio.codex.RMAPIToCodex#getInstances(org.folio.cql2rmapi.CQLParserForRMAPI, io.vertx.core.Context, org.folio.config.RMAPIConfiguration)}.
   */
  @Test
  public void testGetInstances(TestContext context) {
    Async async = context.async();

    RMAPIConfiguration.getConfiguration(okapiHeaders).thenCompose(config -> {
      final CQLParserForRMAPI cql;
      try {
        cql = new CQLParserForRMAPI("title=moby%20dick", 0, 5);
      } catch (UnsupportedEncodingException | QueryValidationException e) {
        throw new CompletionException(e);
      }

      return RMAPIToCodex.getInstances(cql, vertx.getOrCreateContext(), config);
    }).whenComplete((response, throwable) -> {
      context.assertEquals(524, response.getTotalRecords());
      context.assertEquals(5, response.getInstances().size());

      async.complete();
    });
  }
}
