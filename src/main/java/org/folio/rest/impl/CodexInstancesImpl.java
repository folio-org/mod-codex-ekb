package org.folio.rest.impl;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.concurrent.CompletionException;

import javax.ws.rs.core.Response;

import org.folio.rest.annotations.Validate;
import org.folio.codex.RMAPIToCodex;
import org.folio.config.RMAPIConfiguration;
import org.folio.cql2rmapi.CQLParserForRMAPI;
import org.folio.cql2rmapi.QueryValidationException;
import org.folio.rmapi.RMAPIResourceNotFoundException;
import org.folio.rest.jaxrs.resource.CodexInstances;
import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * Instance related codex APIs.
 *
 * @author mreno
 *
 */
public final class CodexInstancesImpl implements CodexInstances {
  private final Logger log = LoggerFactory.getLogger(CodexInstancesImpl.class);

  /* (non-Javadoc)
   * @see org.folio.rest.jaxrs.resource.InstancesResource#getCodexInstances(java.lang.String, int, int, java.lang.String, java.util.Map, io.vertx.core.Handler, io.vertx.core.Context)
   */
  @Override
  @Validate
  public void getCodexInstances(String query, int offset, int limit, String lang,
      Map<String, String> okapiHeaders,
      Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    log.info("method call: getCodexInstances");

    RMAPIConfiguration.getConfiguration(okapiHeaders)
      .thenCompose(rmAPIConfig -> {
        final CQLParserForRMAPI parserForRMAPI;
        try {
          parserForRMAPI = new CQLParserForRMAPI(query, offset, limit);
        } catch (UnsupportedEncodingException | QueryValidationException e) {
          throw new CompletionException(e);
        }
        return RMAPIToCodex.getInstances(parserForRMAPI, vertxContext, rmAPIConfig);
      }).thenAccept(instances ->
         asyncResultHandler.handle(Future.succeededFuture(CodexInstances.GetCodexInstancesResponse.respond200WithApplicationJson(instances)))
      ).exceptionally(throwable -> {
        log.error("getCodexInstances failed!", throwable);
        if (throwable.getCause() instanceof QueryValidationException) {
          asyncResultHandler.handle(Future.succeededFuture(CodexInstances.GetCodexInstancesResponse.respond400WithTextPlain(throwable.getCause().getMessage())));
        } else {
          asyncResultHandler.handle(Future.succeededFuture(CodexInstances.GetCodexInstancesResponse.respond500WithTextPlain(throwable.getCause().getMessage())));
        }
        return null;
      });
  }

  @Override
  @Validate
  public void getCodexInstancesById(String id, String lang,
      Map<String, String> okapiHeaders,
      Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    log.info("method call: getCodexInstancesById");

    RMAPIConfiguration.getConfiguration(okapiHeaders)
      .thenCompose(rmAPIConfig ->
        RMAPIToCodex.getInstance(id, vertxContext, rmAPIConfig)
      ).thenApply(instance -> {
        asyncResultHandler.handle(
            Future.succeededFuture(CodexInstances.GetCodexInstancesByIdResponse.respond200WithApplicationJson(instance)));
        return instance;
      }).exceptionally(throwable -> {
        log.error("getCodexInstancesById failed!", throwable);
        if (throwable.getCause() instanceof RMAPIResourceNotFoundException) {
          asyncResultHandler.handle(
              Future.succeededFuture(CodexInstances.GetCodexInstancesByIdResponse.respond404WithTextPlain(id)));
        } else {
        asyncResultHandler.handle(Future.succeededFuture(CodexInstances.GetCodexInstancesByIdResponse.respond500WithTextPlain(throwable.getCause().getMessage())));
        }
        return null;
      });
  }
}
