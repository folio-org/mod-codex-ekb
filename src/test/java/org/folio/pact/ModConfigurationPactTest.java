package org.folio.pact;

import static io.pactfoundation.consumer.dsl.LambdaDsl.newJsonBody;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

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
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.folio.holdingsiq.model.Configuration;
import org.folio.holdingsiq.model.OkapiData;
import org.folio.holdingsiq.service.impl.ConfigurationServiceImpl;
import org.folio.rest.tools.client.HttpClientFactory;

/**
 * This test must run with the VertxUnitRunner due to dependencies within
 * RMAPIConfiguration.
 *
 * @author mreno
 */
@RunWith(VertxUnitRunner.class)
public class ModConfigurationPactTest {

  // This Rule *MUST* be public or there is an initialization error in JUnit.
  @Rule
  public final PactProviderRuleMk2 mockProvider = new PactProviderRuleMk2("mod-kb-ebsco", this);

  private final Map<String, String> okapiHeaders = new HashMap<>();
  private Field mock = null;

  @Before
  public void setUp() throws NoSuchFieldException, IllegalAccessException {
    // Ugh... Because previous tests can enable mock mode in the
    // HttpClientFactory and setting that is static, i.e. first use binds it to
    // either mock or not mock, we need to do some magic to fix it for this
    // test.
    mock = HttpClientFactory.class.getDeclaredField("mock");
    mock.setAccessible(true);
    mock.set(null, Boolean.FALSE);

    okapiHeaders.put("x-okapi-tenant", "rmapiconfigurationtest");
    okapiHeaders.put("x-okapi-url", mockProvider.getUrl());
    okapiHeaders.put("x-okapi-token", "testToken");
  }

  @After
  public void tearDown() throws IllegalAccessException {
    if (mock != null) {
      // Set mock to TRUE, even if it hadn't been set to TRUE before so
      // subsequent tests will use the mock...
      mock.setAccessible(true);
      mock.set(null, Boolean.TRUE);
    }
  }

  @Pact(provider = "mod-kb-ebsco", consumer = "mod-codex-ekb")
  public RequestResponsePact createPact(PactDslWithProvider builder) {
    // Build the excepted JSON, this gives us much more flexibility when
    // dealing with contract requirements. For example, we don't care what
    // the value for "kb.ebsco.customerId" is, just that it is set to
    // something. Same with the metadata, etc. This gives the provider a bit
    // more flexibility when providing data for contract verification.
    final DslPart body = newJsonBody(o ->
      o.object("attributes", a -> a.stringValue("name", "TEST_NAME")
        .stringValue("apiKey", "TEST_KEY")
        .stringValue("customerId", "TEST_CUSTOMER")
        .stringValue("url", "http://api.test.com"))
    ).build();

    return builder
      .given("mod-codex-ekb configuration needed")
      .uponReceiving("RM API Configuration")
      .path("/eholdings/user-kb-credential")
      .method("GET")
      .willRespondWith()
      .headers(Collections.singletonMap("Content-Type", "application/vnd.api+json"))
      .status(200)
      .body(body)
      .toPact();
  }

  @Test
  @Ignore
  @PactVerification("mod-kb-ebsco")
  public void test(TestContext context) {
    final Async async = context.async();

    CompletableFuture<Configuration> future = new ConfigurationServiceImpl(Vertx.vertx())
      .retrieveConfiguration(new OkapiData(okapiHeaders));

    future.whenComplete((config, throwable) -> {
      // We don't care about the values, as long as the RMAPIConfiguration
      // has values for all the below properties, the contract terms are met.
      context.assertNotNull(config.getApiKey(), "API key is null");
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
