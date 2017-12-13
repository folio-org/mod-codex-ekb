package org.folio.config;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.folio.rest.RestVerticle;
import org.folio.rest.tools.client.HttpClientFactory;
import org.folio.rest.tools.client.Response;
import org.folio.rest.tools.client.interfaces.HttpClientInterface;
import org.folio.rest.tools.utils.TenantTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Retrieves the RM API connection details from mod-configuration.
 *
 * @author mreno
 *
 * TODO: Store a Map (Cluster wide?) of tenant to RMIConfiguration objects.
 */
public final class RMAPIConfiguration {
  private static final Logger LOG = LoggerFactory.getLogger(RMAPIConfiguration.class);
  private static final String CONFIGURATIONS_ENTRIES_ENDPOINT_PATH = "/configurations/entries?query=%28module%3D%3DEKB%20AND%20configName%3D%3Dapi_access%29";

  private String customerId;
  private String apiKey;
  private String url;

  private RMAPIConfiguration() {
    super();
  }

 /**
   * Returns the customer ID.
   *
   * @return The customer ID.
   */
  public String getCustomerId() {
    return customerId;
  }

  /**
   * Returns the API key.
   *
   * @return The API key.
   */
  public String getAPIKey() {
    return apiKey;
  }

  /**
   * Returns the PM API URL.
   *
   * @return The RM API URL.
   */
  public String getUrl() {
    return url;
  }

  @Override
  public String toString() {
    return "RMAPIConfiguration [customerId=" + customerId
        + ", apiKey=" + apiKey + ", url=" + url + ']';
  }

  /**
   * Returns the RM API configuration for the tenant specified in the original
   * request.
   *
   * @param okapiHeaders The headers for the current API call.
   * @return The RMI API configuration for the tenant.
   */
  public static CompletableFuture<RMAPIConfiguration> getConfiguration(final Map<String, String> okapiHeaders) {
    final Map<String, String> okapiHeadersLocal = new HashMap<>(okapiHeaders);
    final String tenantId = TenantTool.calculateTenantId(okapiHeadersLocal.get(RestVerticle.OKAPI_HEADER_TENANT));
    final String okapiURL = okapiHeadersLocal.get("x-okapi-url") != null ? okapiHeadersLocal.get("x-okapi-url") : System.getProperty("okapi.url");

    // We need to remove this header before calling another module or okapi
    // will not be able to find the route. Since we don't own the headers map,
    // we need to make a defensive copy so changes to the map are isolated to
    // this class.
    okapiHeadersLocal.remove("x-okapi-module-id");

    CompletableFuture<RMAPIConfiguration> future = new CompletableFuture<>();

    if (okapiURL == null) {
      future.completeExceptionally(new IllegalArgumentException("The Okapi URL cannot be null"));
      return future;
    }

    try {
      final HttpClientInterface httpClient = HttpClientFactory.getHttpClient(okapiURL, tenantId);

      future = httpClient.request(CONFIGURATIONS_ENTRIES_ENDPOINT_PATH, okapiHeadersLocal)
        .thenApply(response -> {
          try {
            if (Response.isSuccess(response.getCode())) {
              final JsonObject responseBody = response.getBody();
              final JsonArray configs = responseBody.getJsonArray("configs");

              return mapResults(configs);
            } else {
              LOG.error("Cannot get configuration data: " + response.getError().toString(), response.getException());
              throw new IllegalStateException(response.getError().toString());
            }
          } finally {
            httpClient.closeClient();
          }
        });
    } catch (Exception e) {
      LOG.error("Cannot get configuration data: " + e.getMessage(), e);
      future.completeExceptionally(e);
    }

    return future;
  }

  /**
   * Simple mapper for the results of mod-configuration to RMAPIConfiguration.
   *
   * @param configs All the RM API related configurations returned by
   *        mod-configuration.
   */
  private static RMAPIConfiguration mapResults(JsonArray configs) {
    RMAPIConfiguration config = new RMAPIConfiguration();

    configs.stream()
      .filter(JsonObject.class::isInstance)
      .map(JsonObject.class::cast)
      .forEach(entry -> {
        final String code = entry.getString("code");
        final String value = entry.getString("value");
        if ("kb.ebsco.customerId".equalsIgnoreCase(code)) {
          config.customerId = value;
        } else if ("kb.ebsco.apiKey".equalsIgnoreCase(code)) {
          config.apiKey = value;
        } else if ("kb.ebsco.url".equalsIgnoreCase(code)) {
          config.url = value;
        }
      });

    if (config.getCustomerId() == null || config.getAPIKey() == null ||
        config.getUrl() == null) {
      throw new IllegalStateException("Configuration data is invalid");
    }

    return config;
  }
}
