package org.folio.codex;

import java.util.concurrent.CompletableFuture;

import org.folio.config.RMAPIConfiguration;
import org.folio.cql2rmapi.CQLParserForRMAPI;
import org.folio.rest.jaxrs.model.Instance;
import org.folio.rest.jaxrs.model.InstanceCollection;
import org.folio.rmapi.RMAPIService;

import io.vertx.core.Context;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * @author mreno
 *
 */
public final class RMAPIToCodex {
  private static final Logger log = LoggerFactory.getLogger(RMAPIToCodex.class);

  private RMAPIToCodex() {
    super();
  }

  public static CompletableFuture<Instance> getInstance(String id,
      Context vertxContext,
      RMAPIConfiguration rmAPIConfig) {
    log.info("Calling getInstance");

    final RMAPIService rmAPIService = new RMAPIService(rmAPIConfig.getCustomerId(),
        rmAPIConfig.getAPIKey(), RMAPIService.getBaseURI(), vertxContext.owner());
    final CompletableFuture<Instance> cf = new CompletableFuture<>();

    rmAPIService.getTileById(id).setHandler(rmapiResult -> {
      if (rmapiResult.failed()) {
        cf.completeExceptionally(rmapiResult.cause());
      } else {
        final Instance codexInstance = rmapiResult.result();
        cf.complete(codexInstance);
      }
    });

    return cf;
  }

  public static CompletableFuture<InstanceCollection> getInstances(CQLParserForRMAPI cql,
      Context vertxContext,
      RMAPIConfiguration rmAPIConfig) {
    log.info("Calling getInstances");

    final RMAPIService rmAPIService = new RMAPIService(rmAPIConfig.getCustomerId(),
        rmAPIConfig.getAPIKey(), RMAPIService.getBaseURI(), vertxContext.owner());
    final String query = cql.getRMAPIQuery();
    final CompletableFuture<InstanceCollection> cf = new CompletableFuture<>();

    rmAPIService.getTitleList(query).setHandler(rmapiResult -> {
      if (rmapiResult.failed()) {
        cf.completeExceptionally(rmapiResult.cause());
      } else {
        final InstanceCollection codexInstances = rmapiResult.result();
        cf.complete(codexInstances);
      }
    });

    return cf;
  }
}
