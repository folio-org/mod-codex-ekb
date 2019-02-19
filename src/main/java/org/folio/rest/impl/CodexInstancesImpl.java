package org.folio.rest.impl;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;

import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.core.Response;

import org.folio.codex.RMAPIToCodex;
import org.folio.config.RMAPIConfiguration;
import org.folio.cql2rmapi.CQLParameters;
import org.folio.cql2rmapi.CQLParserForRMAPI;
import org.folio.cql2rmapi.QueryValidationException;
import org.folio.cql2rmapi.TitleParameters;
import org.folio.rest.annotations.Validate;
import org.folio.rest.jaxrs.model.InstanceCollection;
import org.folio.rest.jaxrs.resource.CodexInstances;
import org.folio.rmapi.RMAPIResourceNotFoundException;

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
      .thenCompose(rmAPIConfig -> getCodexInstances(query, offset, limit, vertxContext, rmAPIConfig)).thenAccept(instances ->
         asyncResultHandler.handle(Future.succeededFuture(CodexInstances.GetCodexInstancesResponse.respond200WithApplicationJson(instances)))
      ).exceptionally(throwable -> {
        log.error("getCodexInstances failed!", throwable);
        if (throwable.getCause() instanceof QueryValidationException) {
          asyncResultHandler.handle(Future.succeededFuture(CodexInstances.GetCodexInstancesResponse.respond400WithTextPlain(throwable.getCause().getMessage())));
        } else if (throwable.getCause() instanceof NotAuthorizedException) {
          asyncResultHandler.handle(Future.succeededFuture(CodexInstances.GetCodexInstancesResponse.respond401WithTextPlain(throwable.getCause().getMessage())));
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
        } else if (throwable.getCause() instanceof NotAuthorizedException) {
        	asyncResultHandler.handle(Future.succeededFuture(CodexInstances.GetCodexInstancesResponse.respond401WithTextPlain(throwable.getCause().getMessage())));
        } else {
        	asyncResultHandler.handle(Future.succeededFuture(CodexInstances.GetCodexInstancesByIdResponse.respond500WithTextPlain(throwable.getCause().getMessage())));
        }
        return null;
      });
  }

  private CompletionStage<InstanceCollection> getCodexInstances(String query, int offset, int limit, Context vertxContext, RMAPIConfiguration rmAPIConfig){
    try {
      if (limit == 0 || query == null) {
        throw new QueryValidationException("Unsupported Query Format : Limit/Query suggests that no results need to be returned.");
      }
      final CQLParserForRMAPI parserForRMAPI;

      CQLParameters cqlParameters = new CQLParameters(query);
      if (cqlParameters.isIdSearch()) {
        return RMAPIToCodex.getInstanceById(vertxContext, rmAPIConfig, cqlParameters.getIdSearchValue());
      }
      parserForRMAPI = new CQLParserForRMAPI(new TitleParameters(cqlParameters), offset, limit);
      return RMAPIToCodex.getInstances(parserForRMAPI, vertxContext, rmAPIConfig);
    } catch (UnsupportedEncodingException | QueryValidationException e) {
      throw new CompletionException(e);
    }
  }
}
