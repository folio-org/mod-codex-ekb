package org.folio.pact;

import static io.pactfoundation.consumer.dsl.LambdaDsl.newJsonBody;

import java.util.HashMap;
import java.util.Map;

import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.PactProviderRuleMk2;
import au.com.dius.pact.consumer.PactVerification;
import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.model.RequestResponsePact;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.folio.holdingsiq.model.Configuration;
import org.folio.holdingsiq.service.TitlesHoldingsIQService;
import org.folio.holdingsiq.service.impl.TitlesHoldingsIQServiceImpl;

/**
 * @author mreno
 */
@RunWith(VertxUnitRunner.class)
public class RMAPIPactTest {

  // This Rule *MUST* be public or there is an initialization error in JUnit.
  @Rule
  public final PactProviderRuleMk2 mockRMAPIProvider = new PactProviderRuleMk2("rm-api", this);

  private Vertx vertx;

  @Before
  public void setUp() {
    vertx = Vertx.vertx();
  }

  @After
  public void after(TestContext context) {
    vertx.close(context.asyncAssertSuccess());
  }

  @Pact(provider = "rm-api", consumer = "mod-codex-ekb")
  public RequestResponsePact createPact(PactDslWithProvider builder) {
    final Map<String, String> headers = new HashMap<>();
    headers.put("x-api-key", "123456789");

    // List Titles Pact
    final DslPart listTitlesBody = newJsonBody(o -> o
      .array("titles", a -> a.object(prop -> {
        prop.numberType("titleId")
          .stringType("titleName")
          .stringType("publisherName")
          .array("identifierList", il -> il.object(i -> {
            i.stringType("id", "source")
              .numberType("subType", "type");
          }))
          .array("subjectsList", sl -> sl
            .object(s -> {
              s.stringType("type", "subject");
            }))
          .booleanType("isTitleCustom")
          .stringType("pubType")
          .array("customerResourcesList", crl -> crl
            .object(cr -> {
              cr.numberType("titleId", "packageId", "vendorId", "locationId")
                .stringType("packageName", "packageType", "vendorName",
                  "coverageStatement", "url", "userDefinedField1",
                  "userDefinedField2", "userDefinedField3",
                  "userDefinedField4", "userDefinedField5")
                .booleanType("isCustomPackage", "isSelected", "isTokenNeeded")
                .object("visibilityData", vd -> vd
                  .booleanType("isHidden")
                  .stringType("reason"))
                .array("managedCoverageList", mcl -> mcl
                  .object(mc -> {
                    mc.stringType("beginCoverage", "endCoverage");
                  }))
                .array("customCoverageList", ccl -> ccl
                  .object(cc -> {
                    cc.stringType("beginCoverage", "endCoverage");
                  }))
                .object("managedEmbargoPeriod", mep -> mep
                  .stringType("embargoUnit")
                  .numberType("embargoValue"))
                .object("customEmbargoPeriod", cep -> cep.stringType("embargoUnit")
                  .numberType("embargoValue"));
            }));
      }))
      .numberValue("totalResults", 1)).build();

    // Get Title by ID Pact
    final DslPart getTitleByIdBbody = newJsonBody(o -> o
      .stringType("description", "edition", "titleName", "publisherName", "pubType")
      .booleanType("isPeerReviewed", "isTitleCustom")
      .numberValue("titleId", 1234567)
      .array("contributorsList", cl -> cl.object(c -> {
        c.stringType("type", "contributor");
      }))
      .array("identifiersList", il -> il.object(i -> {
        i.numberType("id", "subType", "type")
          .stringType("source");
      }))
      .array("subjectsList", sl -> sl.object(s -> {
        s.stringType("subject", "type");
      }))
      .array("customerResourcesList", crl -> {
        crl.object(cr -> cr
          .numberType("titleId", "packageId", "vendorId", "locationId")
          .stringType("packageName", "packageType", "vendorName",
            "coverageStatement", "url", "userDefinedField1",
            "userDefinedField2", "userDefinedField3",
            "userDefinedField4", "userDefinedField5")
          .booleanType("isCustomPackage", "isSelected", "isTokenNeeded")
          .object("visibilityData", vd -> vd
            .booleanType("isHidden")
            .stringType("reason"))
          .array("managedCoverageList", mcl -> mcl.object(mc -> {
            mc.stringType("beginCoverage", "endCoverage");
          }))
          .array("customCoverageList", ccl -> ccl.object(cc -> {
            cc.stringType("beginCoverage", "endCoverage");
          }))
          .object("managedEmbargoPeriod", mep -> mep.stringType("embargoUnit")
            .numberType("embargoValue"))
          .object("customEmbargoPeriod", cep -> {
            cep.stringType("embargoUnit")
              .numberType("embargoValue");
          }));
      })).build();

    return builder
      .given("List Titles")
      .uponReceiving("a request for titles with moby dick")
      .path("/rm/rmaccounts/testcust/titles")
      .query("searchfield=titlename&search=moby dick&orderby=titlename&count=10&offset=1")
      .headers(headers)
      .method("GET")
      .willRespondWith()
      .status(200)
      .body(listTitlesBody)
      .given("Get Title By ID")
      .uponReceiving("A title request for ID 1234567")
      .path("/rm/rmaccounts/testcust/titles/1234567")
      .headers(headers)
      .method("GET")
      .willRespondWith()
      .status(200)
      .body(getTitleByIdBbody)
      .toPact();
  }

  @Test
  @PactVerification("rm-api")
  public void pactTest(TestContext context) {
    final Async listTitlesAsync = context.async();
    final Async getTitleByIdAsync = context.async();

    final TitlesHoldingsIQService titlesService = new TitlesHoldingsIQServiceImpl(
      Configuration.builder().customerId("testcust").apiKey("123456789").url(mockRMAPIProvider.getUrl()).build(), vertx);

    titlesService.retrieveTitles("searchfield=titlename&search=moby%20dick&orderby=titlename&count=10&offset=1")
      .whenComplete((titles, throwable) -> {
        context.assertNotNull(titles, "titles is null");
        context.assertEquals(1, titles.getTotalResults());
        listTitlesAsync.complete();
      })
      .exceptionally(throwable -> {
        context.fail(throwable);
        listTitlesAsync.complete();
        return null;
      });

    titlesService.retrieveTitle(1234567)
      .whenComplete((title, throwable) -> {
        context.assertNotNull(title, "title is null");
        context.assertEquals(1234567, title.getTitleId());
        getTitleByIdAsync.complete();
      })
      .exceptionally(throwable -> {
        context.fail(throwable);
        getTitleByIdAsync.complete();
        return null;
      });
  }
}
