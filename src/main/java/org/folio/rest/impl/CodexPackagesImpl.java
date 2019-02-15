package org.folio.rest.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import org.folio.rest.jaxrs.resource.CodexPackages;
import javax.ws.rs.core.Response;
import java.util.Map;

/**
 * Package related codex APIs.
 */
public final class CodexPackagesImpl implements CodexPackages {

  @Override
  public void getCodexPackages(String query, int offset, int limit, String lang, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    asyncResultHandler.handle(Future.succeededFuture(Response.status(Response.Status.NOT_IMPLEMENTED).build()));
  }

  @Override
  public void getCodexPackagesById(String id, String lang, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    asyncResultHandler.handle(Future.succeededFuture(Response.status(Response.Status.NOT_IMPLEMENTED).build()));
  }
}
