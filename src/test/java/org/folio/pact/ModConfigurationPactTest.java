package org.folio.pact;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.junit.Rule;
import org.junit.Test;

import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.PactProviderRuleMk2;
import au.com.dius.pact.consumer.PactVerification;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.model.RequestResponsePact;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;

/**
 * @author mreno
 *
 */
public class ModConfigurationPactTest {
  private static final String MOD_CONFIGURATION_JSON_FILE = "pact/mod-configuration.json";

  @Rule
  public PactProviderRuleMk2 mockProvider = new PactProviderRuleMk2("mod-configuration", this);

  @Pact(provider="mod-configuration", consumer="mod-codex-ekb")
  public RequestResponsePact createPact(PactDslWithProvider builder) throws IOException {
    final String body = IOUtils.toString(ModConfigurationPactTest.class.getClassLoader().getResourceAsStream(MOD_CONFIGURATION_JSON_FILE));
    return builder
        .given("configuration needed")
        .uponReceiving("RM API Configuration")
          .path("/configurations/entries")
          .query("query=%28module%3D%3DEKB%20AND%20configName%3D%3Dapi_access%29")
          .method("GET")
        .willRespondWith()
          .status(200)
          .body(body, org.apache.http.entity.ContentType.APPLICATION_JSON)
        .toPact();
  }

  @Test
  @PactVerification("mod-configuration")
  public void test() {
    RestAssured
      .given()
        .port(mockProvider.getPort().intValue())
      .when()
        .get("/configurations/entries?query=%28module%3D%3DEKB%20AND%20configName%3D%3Dapi_access%29")
      .then()
        .statusCode(200)
        .contentType(ContentType.JSON)
        .body("totalRecords", equalTo(3),
            "configs.code", hasItems("kb.ebsco.customerId", "kb.ebsco.apiKey", "kb.ebsco.url"),
            "configs.value", hasItems("examplecorp", "8675309", "https://rmapi.example.com"));
  }
}
