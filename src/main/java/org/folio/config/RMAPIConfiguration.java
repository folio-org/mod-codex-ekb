package org.folio.config;

import java.util.Map;
import java.util.stream.Collector;

import org.folio.rest.RestVerticle;
import org.folio.rest.tools.client.HttpClientFactory;
import org.folio.rest.tools.client.Response;
import org.folio.rest.tools.client.interfaces.HttpClientInterface;
import org.folio.rest.tools.utils.TenantTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Retrieves the RM API connection details from mod-configuration.
 * 
 * @author mreno
 *
 * TODO: Store a Map (Cluster wide?) of tenant to RMIConfiguration objects.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class RMAPIConfiguration {
  private static final Logger LOG = LoggerFactory.getLogger(RMAPIConfiguration.class);
  private static final String CONFIGURATIONS_ENTRIES_ENDPOINT_PATH = "/configurations/entries?query=%28module%3D%3DKB_EBSCO%20AND%20configName%3D%3Dapi_credentials%29";

  private String customerId;
  private String apiKey;

  /**
   * Constructs a new RMAPIConfiguration. Keeping this private for now, only
   * this class should be able to build one.
   * 
   * @param customerId The customer ID.
   * @param apiKey the API Key.
   */
  @JsonCreator
  private RMAPIConfiguration(
      @JsonProperty("customer-id") final String customerId,
      @JsonProperty("api-key") final String apiKey) {
    this.customerId = customerId;
    this.apiKey = apiKey;
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

  @Override
  public String toString() {
    return "RMAPIConfiguration [customerId=" + customerId
        + ", apiKey=" + apiKey + ']';
  }

  /**
   * Returns the RM API configuration for the tenant specified in the original
   * request.
   * 
   * @param okapiHeaders The headers for the current API call.
   * @return The RMI API configuration for the tenant.
   */
  public static Future<RMAPIConfiguration> getConfiguration(final Map<String, String> okapiHeaders) {
    final String tenantId = TenantTool.calculateTenantId(okapiHeaders.get(RestVerticle.OKAPI_HEADER_TENANT));
    final String okapiURL = okapiHeaders.get("X-Okapi-Url");

    Future<RMAPIConfiguration> future = Future.future();

    try {
      final HttpClientInterface httpClient = HttpClientFactory.getHttpClient(okapiURL, tenantId);

      httpClient.request(CONFIGURATIONS_ENTRIES_ENDPOINT_PATH, okapiHeaders)
        .whenComplete((response, throwable) -> {
          if (Response.isSuccess(response.getCode())) {
            final JsonObject responseBody = response.getBody();
            final JsonArray configs = responseBody.getJsonArray("configs");

            mapResults(configs, future);
          } else {
            LOG.error("Cannot get configuration data: " + response.getError().toString(), response.getError());
            future.fail(response.getException());
          }
        });
    } catch (Exception e) {
      LOG.error("Cannot get configuration data: " + e.getMessage(), e);
      future.fail(e);
    }

    return future;
  }

  /**
   * Simple mapper for the results of mod-configuration to RMAPIConfiguration.
   * 
   * @param configs All the RM API related configurations returned by
   *        mod-configuration.
   * @param future The future that will store the RMAPIConfiguration object or
   *        the reason for failure.
   */
  private static void mapResults(JsonArray configs, Future<RMAPIConfiguration> future) {
    try {
      RMAPIConfiguration mappedValue = configs.stream()
          .filter(JsonObject.class::isInstance)
          .map(JsonObject.class::cast)
          .collect(Collector.of(JsonObject::new,
              (result, entry) -> {
                // This seems kind of fragile, but any failure will fail the
                // request, which is what we want. However,
                // ArrayIndexOutOfBoundsException or NPE are not ideal failure
                // messages. :)
                final String value = entry.getString("value");
                final String [] values = value.split("&");
                for (String v : values) {
                  final String [] kv = v.split("=");
                  result.put(kv[0], kv[1]);
                }
              },
              JsonObject::mergeIn,
              result -> result.mapTo(RMAPIConfiguration.class)));
      future.complete(mappedValue);
    } catch (Exception ex) {
      future.fail(ex);
    }
  }
}
