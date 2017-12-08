package org.folio.rest.impl;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.folio.codex.RMAPIToCodex;
import org.folio.config.RMAPIConfiguration;
import org.folio.cql2rmapi.CQLParserForRMAPI;
import org.folio.cql2rmapi.QueryValidationException;
import org.folio.rest.annotations.Validate;
import org.folio.rest.jaxrs.resource.CodexInstancesResource;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * Instance related codex APIs.
 *
 * @author mreno
 *
 */
public final class CodexInstancesResourceImpl implements CodexInstancesResource {
  private final Logger log = LoggerFactory.getLogger(CodexInstancesResourceImpl.class);

  public CodexInstancesResourceImpl(Vertx vertx, String tenantId) {
    super();
  }

  /* (non-Javadoc)
   * @see org.folio.rest.jaxrs.resource.InstancesResource#getCodexInstances(java.lang.String, int, int, java.lang.String, java.util.Map, io.vertx.core.Handler, io.vertx.core.Context)
   */
  @Override
  @Validate
  public void getCodexInstances(String query, int offset, int limit, String lang,
      Map<String, String> okapiHeaders,
      Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext)
      throws Exception {
    log.info("method call: getInstances");

    log.info("Calling CQL Parser");
    final CQLParserForRMAPI parserForRMAPI;
    try {
        parserForRMAPI = new CQLParserForRMAPI(query, offset, limit);
    } catch (final QueryValidationException e) {
      asyncResultHandler.handle(Future.succeededFuture(CodexInstancesResource.GetCodexInstancesResponse.withPlainBadRequest(e.getMessage())));
      return;
    } catch (final UnsupportedEncodingException e) {
      // Since a URL encoding error is server side and not something the
      // client can fix, we return a 500.
      asyncResultHandler.handle(Future.succeededFuture(CodexInstancesResource.GetCodexInstancesResponse.withPlainInternalServerError(e.getMessage())));
      return;
    }

    RMAPIConfiguration.getConfiguration(okapiHeaders)
      .thenComposeAsync(rmAPIConfig -> {
        log.info("RM API Config: " + rmAPIConfig);
        return RMAPIToCodex.getInstances(parserForRMAPI, vertxContext, rmAPIConfig);
      }).thenApplyAsync(instances -> {
        asyncResultHandler.handle(Future.succeededFuture(CodexInstancesResource.GetCodexInstancesResponse.withJsonOK(instances)));
        return instances;
      }).exceptionally(throwable -> {
        log.error("getCodexInstances failed!", throwable);
        asyncResultHandler.handle(Future.succeededFuture(CodexInstancesResource.GetCodexInstancesResponse.withPlainInternalServerError(throwable.getCause().getMessage())));
        return null;
      });
  }

  @Override
  @Validate
  public void getCodexInstancesById(String id, String lang,
      Map<String, String> okapiHeaders,
      Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext)
      throws Exception {
    log.info("method call: getInstancesById");

    RMAPIConfiguration.getConfiguration(okapiHeaders)
      .thenComposeAsync(rmAPIConfig -> {
        log.info("RM API Config: " + rmAPIConfig);
        return RMAPIToCodex.getInstance(id, vertxContext, rmAPIConfig);
      }).thenApplyAsync(instance -> {
        asyncResultHandler.handle(Future.succeededFuture(
            instance == null ?
                CodexInstancesResource.GetCodexInstancesByIdResponse.withPlainNotFound(id) :
                  CodexInstancesResource.GetCodexInstancesByIdResponse.withJsonOK(instance)));
        return instance;
      }).exceptionally(throwable -> {
        log.error("getCodexInstancesById failed!", throwable);
        asyncResultHandler.handle(Future.succeededFuture(CodexInstancesResource.GetCodexInstancesByIdResponse.withPlainInternalServerError(throwable.getCause().getMessage())));
        return null;
      });
  }
}
