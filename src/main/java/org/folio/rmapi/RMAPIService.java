package org.folio.rmapi;

import java.util.concurrent.CompletableFuture;

import org.folio.rmapi.model.Title;
import org.folio.rmapi.model.Titles;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * @author cgodfrey
 *
 */
public class RMAPIService {

  private static final Logger LOG = LoggerFactory.getLogger(RMAPIService.class);
  private static final String HTTP_HEADER_CONTENT_TYPE = "Content-type";
  private static final String APPLICATION_JSON = "application/json";
  private static final String HTTP_HEADER_ACCEPT = "Accept";
  private static final String RMAPI_API_KEY = "X-Api-Key";

  private static final String JSON_RESPONSE_ERROR = "Error processing RMAPI Response";
  private static final String INVALID_RMAPI_RESPONSE = "Invalid RMAPI response";

  private String customerId;
  private String apiKey;
  private String baseURI;

  private Vertx vertx;

  /**
   * Constructs an RMAPI Service object which is used to make pass through
   * requests to rmapi
   *
   * @param customerId
   * @param apiKey
   * @param baseURI
   * @param vertx
   */
  public RMAPIService(String customerId, String apiKey, String baseURI, Vertx vertx) {
    this.customerId = customerId;
    this.apiKey = apiKey;
    this.baseURI = baseURI;
    this.vertx = vertx;
  }

  /**
   * Issues a get request to RMAPI Service and returns a completablefuture for the
   * target type
   * 
   * @param query
   * @param clazz
   * @return
   */
  private <T> CompletableFuture<T> getRequest(String query, Class<T> clazz) {

    CompletableFuture<T> future = new CompletableFuture<>();

    HttpClient httpClient = vertx.createHttpClient();

    final HttpClientRequest request = httpClient.getAbs(query);

    request.headers().add(HTTP_HEADER_ACCEPT, APPLICATION_JSON);
    request.headers().add(HTTP_HEADER_CONTENT_TYPE, APPLICATION_JSON);
    request.headers().add(RMAPI_API_KEY, apiKey);

    LOG.info("RMAPI Service absolute URL is" + request.absoluteURI());

    request.handler(response -> response.bodyHandler(body -> {
      if (response.statusCode() == 200) {
        try {
          final JsonObject instanceJSON = new JsonObject(body.toString());
          T results = instanceJSON.mapTo(clazz);
          future.complete((T) results);
        } catch (Exception e) {
          LOG.error(
              String.format("%s - Response = [%s] Target Type = [%s]", JSON_RESPONSE_ERROR, body.toString(), clazz));
          future.completeExceptionally(
              new RMAPIResultsProcessingException(String.format("%s for query = %s", JSON_RESPONSE_ERROR, query), e));
        } finally {
          httpClient.close();
        }
      } else {
        httpClient.close();
        LOG.error(String.format("%s for JSON [%s] into type [%s]", INVALID_RMAPI_RESPONSE, body.toString(), clazz));

        RMAPIServiceException rmException = new RMAPIServiceException(String.format("%s Code = %s Message = %s",
            INVALID_RMAPI_RESPONSE, response.statusCode(), response.statusMessage()), response.statusCode(),
            response.statusMessage(), body.toString(), query);

        future.completeExceptionally(rmException);
      }

    }));

    request.end();

    return future;

  }

  /**
   *
   * Retrieve title by id from rmapi service
   *
   * @param titleId
   * @return
   */
  public CompletableFuture<Title> getTitleById(String titleId) {
    return this.<Title>getRequest(constructURL(String.format("titles/%s", titleId)), Title.class);
  }

  /**
   *
   * Retrieve list of titles from rmapi service (based on supplied rmapi query)
   *
   * @param rmapiQuery
   * @return
   */

  public CompletableFuture<Titles> getTitleList(String rmapiQuery) {
    return this.<Titles>getRequest(constructURL(String.format("titles?%s", rmapiQuery)), Titles.class);
  }

  /**
   * Constructs full rmapi path
   *
   * @param path
   * @return
   */
  private String constructURL(String path) {
    String fullPath = String.format("%s/rm/rmaccounts/%s/%s", baseURI, customerId, path);

    LOG.info("constructurl - path=" + fullPath);
    return fullPath;
  }
}
