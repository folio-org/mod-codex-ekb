package org.folio.pact;

import static io.pactfoundation.consumer.dsl.LambdaDsl.newJsonBody;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.folio.config.RMAPIConfiguration;
import org.folio.rest.tools.client.HttpClientFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.PactProviderRuleMk2;
import au.com.dius.pact.consumer.PactVerification;
import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.model.RequestResponsePact;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

/**
 * This test must run with the VertxUnitRunner due to dependencies within
 * RMAPIConfiguration.
 *
 * @author mreno
 *
 */
@RunWith(VertxUnitRunner.class)
public class ModConfigurationPactTest {
  private Map<String, String> okapiHeaders = new HashMap<>();

  // This Rule *MUST* be public or there is an initialization error in JUnit.
  @Rule
  public final PactProviderRuleMk2 mockProvider = new PactProviderRuleMk2("mod-configuration", this);

  private Field mock = null;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp(TestContext context) throws Exception {
    // Ugh... Because previous tests can enable mock mode in the
    // HttpClientFactory and setting that is static, i.e. first use binds it to
    // either mock or not mock, we need to do some magic to fix it for this
    // test.
    mock = HttpClientFactory.class.getDeclaredField("mock");
    mock.setAccessible(true);
    mock.set(null, Boolean.FALSE);

    okapiHeaders.put("x-okapi-tenant", "rmapiconfigurationtest");
    okapiHeaders.put("x-okapi-url", mockProvider.getUrl());
  }

  @After
  public void tearDown(TestContext context) throws Exception {
    if (mock != null) {
      // Set mock to TRUE, even if it hadn't been set to TRUE before so
      // subsequent tests will use the mock...
      mock.setAccessible(true);
      mock.set(null, Boolean.TRUE);
    }
  }

  @Pact(provider="mod-configuration", consumer="mod-codex-ekb")
  public RequestResponsePact createPact(PactDslWithProvider builder) throws IOException {
    final String dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    // Build the excepted JSON, this gives us much more flexibility when
    // dealing with contract requirements. For example, we don't care what
    // the value for "kb.ebsco.customerId" is, just that it is set to
    // something. Same with the metadata, etc. This gives the provider a bit
    // more flexibility when providing data for contract verification.
    final DslPart body = newJsonBody(o -> {
      o.array("configs", a -> {
        a.object(prop -> {
          prop.uuid("id")
            .stringValue("module", "EKB")
            .stringValue("configName", "api_access")
            .stringValue("code", "kb.ebsco.customerId")
            .stringType("description")
            .booleanValue("enabled", Boolean.TRUE)
            .stringType("value")
            .object("metadata", md -> {
              md.date("createdDate", dateFormat)
                .uuid("createdByUserId")
                .date("updatedDate", dateFormat)
                .uuid("updatedByUserId");
            });
        });
        a.object(prop -> {
          prop.uuid("id")
            .stringValue("module", "EKB")
            .stringValue("configName", "api_access")
            .stringValue("code", "kb.ebsco.apiKey")
            .stringType("description")
            .booleanValue("enabled", Boolean.TRUE)
            .stringType("value")
            .object("metadata", md -> {
              md.date("createdDate", dateFormat)
                .uuid("createdByUserId")
                .date("updatedDate", dateFormat)
                .uuid("updatedByUserId");
            });
        });
        a.object(prop -> {
          prop.uuid("id")
            .stringValue("module", "EKB")
            .stringValue("configName", "api_access")
            .stringValue("code", "kb.ebsco.url")
            .stringType("description")
            .booleanValue("enabled", Boolean.TRUE)
            .stringType("value")
            .object("metadata", md -> {
              md.date("createdDate", dateFormat)
                .uuid("createdByUserId")
                .date("updatedDate", dateFormat)
                .uuid("updatedByUserId");
            });
        });
      });
      o.numberValue("totalRecords", 3);
      o.object("resultInfo", ri -> {
        ri.numberValue("totalRecords", 3);
        ri.array("facets", a -> {
          // empty right now...
        });
      });
    }).build();

    return builder
        .given("mod-codex-ekb configuration needed")
        .uponReceiving("RM API Configuration")
          .path("/configurations/entries")
          .query("query=(module==EKB AND configName==api_access)")
          .method("GET")
        .willRespondWith()
          .status(200)
          .body(body)
        .toPact();
  }

  @Test
  @PactVerification("mod-configuration")
  public void test(TestContext context) {
    final Async async = context.async();
    CompletableFuture<RMAPIConfiguration> cf = RMAPIConfiguration.getConfiguration(okapiHeaders);

    cf.whenComplete((config, throwable) -> {
      // We don't care about the values, as long as the RMAPIConfiguration
      // has values for all the below properties, the contract terms are met.
      context.assertNotNull(config.getAPIKey(), "API key is null");
      context.assertNotNull(config.getCustomerId(), "Customer ID is null");
      context.assertNotNull(config.getUrl(), "RM API URL is null");
      async.complete();
    }).exceptionally(throwable -> {
      context.fail(throwable);
      async.complete();
      return null;
    });
  }
}
