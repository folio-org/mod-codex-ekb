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

  private String customerId;
  private String apiKey;
  private String baseURI;

  private HttpClient httpClient;

  /**
   * Constructs an RMAPI Service object which is used to make passthru requests to
   * rmapi
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
    httpClient = vertx.createHttpClient();
  }

  /**
   *
   * Retrieve title by id from rmapi service
   *
   * @param titleId
   * @return
   */
  public CompletableFuture<Title> getTitleById(String titleId) {

    CompletableFuture<Title> future = new CompletableFuture<>();

    final HttpClientRequest request = httpClient.getAbs(constructURL(String.format("titles/%s", titleId)));

    request.headers().add(HTTP_HEADER_ACCEPT, APPLICATION_JSON);
    request.headers().add(HTTP_HEADER_CONTENT_TYPE, APPLICATION_JSON);
    request.headers().add(RMAPI_API_KEY, apiKey);

    LOG.info("absolute URL is" + request.absoluteURI());

    request.handler(response ->

    response.bodyHandler(body -> {

      LOG.info("rmapi request status code =" + response.statusCode());

      // need to only handle status code = 200
      // other status codes should return and throw an error
      if (response.statusCode() == 200) {
        try {
          LOG.info(body.toString());
          final JsonObject instanceJSON = new JsonObject(body.toString());

          Title rmapiTitles = instanceJSON.mapTo(Title.class);

          future.complete(rmapiTitles);
        } catch (Exception e) {
          LOG.info("failure  " + e.getMessage());
          future.completeExceptionally(e);
        } finally {
          httpClient.close();
        }
      } else {
        httpClient.close();
        future
            .completeExceptionally(new RMAPIServiceException("Invalid status code from RMAPI" + response.statusCode()));
      }
    }));
    request.end();

    return future;
  }

  /**
   *
   * Retrieve list of titles from rmapi service (based on rmapi query that is
   * passed in)
   *
   * @param rmapiQuery
   * @return
   */

  public CompletableFuture<Titles> getTitleList(String rmapiQuery) {

    CompletableFuture<Titles> future = new CompletableFuture<>();

    final HttpClientRequest request = httpClient.getAbs(constructURL(String.format("titles?%s", rmapiQuery)));

    request.headers().add(HTTP_HEADER_ACCEPT, APPLICATION_JSON);
    request.headers().add(HTTP_HEADER_CONTENT_TYPE, APPLICATION_JSON);
    request.headers().add(RMAPI_API_KEY, apiKey);

    LOG.info("absolute URL is" + request.absoluteURI());

    request.handler(response ->

    response.bodyHandler(body -> {

      LOG.info("rmapi request status code =" + response.statusCode());

      if (response.statusCode() == 200) {
        try {
          LOG.info(body.toString());
          final JsonObject instanceJSON = new JsonObject(body.toString());
          Titles rmapiTitles = instanceJSON.mapTo(Titles.class);
          future.complete(rmapiTitles);

        } catch (Exception e) {
          LOG.info("failure  " + e.getMessage());
          future.completeExceptionally(e);
        } finally {
          httpClient.close();
        }
      } else {
        httpClient.close();
        future
            .completeExceptionally(new RMAPIServiceException("Invalid status code from RMAPI" + response.statusCode()));
      }
    }));
    request.end();

    return future;

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
